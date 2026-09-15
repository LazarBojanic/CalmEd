import StoreKit

@MainActor
final class StoreKitManager: ObservableObject {
    static let shared = StoreKitManager()
    
    @Published var purchasedProductIDs: Set<String> = []
    
    private var transactionListener: Task<Void, Error>? = nil
    private nonisolated(unsafe) var restoreObserver: NSObjectProtocol? = nil
    private nonisolated(unsafe) var finishObserver: NSObjectProtocol? = nil
    
    init() {
        transactionListener = listenForTransactions()
        restoreObserver = NotificationCenter.default.addObserver(
            forName: NSNotification.Name("TriggerAppleRestore"),
            object: nil,
            queue: .main
        ) { [weak self] _ in
            guard let self = self else { return }
            Task {
                try? await self.restorePurchases()
            }
        }
        finishObserver = NotificationCenter.default.addObserver(
            forName: NSNotification.Name("TriggerAppleFinish"),
            object: nil,
            queue: .main
        ) { [weak self] note in
            guard let self = self else { return }
            guard let id = note.userInfo?["transactionId"] as? String else { return }
            Task {
                await self.finishTransaction(originalID: id)
            }
        }
    }
    
    deinit {
        transactionListener?.cancel()
        if let restoreObserver = restoreObserver {
            NotificationCenter.default.removeObserver(restoreObserver)
        }
        if let finishObserver = finishObserver {
            NotificationCenter.default.removeObserver(finishObserver)
        }
    }
    
    func finishTransaction(originalID: String) async {
        for await result in Transaction.unfinished {
            guard let transaction = try? self.checkVerified(result) else { continue }
            if transaction.originalID.description == originalID {
                await transaction.finish()
            }
        }
    }
    
    func listenForTransactions() -> Task<Void, Error> {
        Task.detached { [weak self] in
            for await result in Transaction.updates {
                do {
                    guard let self = self else { return }
                    let transaction = try self.checkVerified(result)
                    
                    await self.updatePurchasedProducts(transaction)
                    
                    await MainActor.run {
                        NotificationCenter.default.post(
                            name: NSNotification.Name("OnApplePurchaseSuccess"),
                            object: nil,
                            userInfo: [
                                "transactionId": transaction.originalID.description,
                                "productId": transaction.productID
                            ]
                        )
                    }
                } catch {
                    print("Transaction verification failed")
                }
            }
        }
    }
    
    func updatePurchasedProducts(_ transaction: Transaction) {
        if transaction.revocationDate == nil {
            purchasedProductIDs.insert(transaction.productID)
        } else {
            purchasedProductIDs.remove(transaction.productID)
        }
    }
    
    nonisolated func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified:
            throw StoreError.failedVerification
        case .verified(let safe):
            return safe
        }
    }
    
    func purchase(productID: String) async throws {
        let products = try await Product.products(for: [productID])
        guard let product = products.first else {
            throw StoreError.productNotFound
        }
        
        let result = try await product.purchase()
        
        switch result {
        case .success(let verification):
            let transaction = try checkVerified(verification)
            updatePurchasedProducts(transaction)
            
            NotificationCenter.default.post(
                name: NSNotification.Name("OnApplePurchaseSuccess"),
                object: nil,
                userInfo: [
                    "transactionId": transaction.originalID.description,
                    "productId": transaction.productID
                ]
            )

        case .userCancelled:
            NotificationCenter.default.post(
                name: NSNotification.Name("OnApplePurchaseFailure"),
                object: nil,
                userInfo: ["error": "User cancelled"]
            )
        case .pending:
            break
        @unknown default:
            break
        }
    }

    func restorePurchases() async throws {
        try? await AppStore.sync()
        var found = 0
        for await result in Transaction.currentEntitlements {
            guard let transaction = try? self.checkVerified(result) else { continue }
            found += 1
            self.updatePurchasedProducts(transaction)
            NotificationCenter.default.post(
                name: NSNotification.Name("OnApplePurchaseSuccess"),
                object: nil,
                userInfo: [
                    "transactionId": transaction.originalID.description,
                    "productId": transaction.productID
                ]
            )
        }

        NotificationCenter.default.post(
            name: NSNotification.Name("OnAppleRestoreComplete"),
            object: nil,
            userInfo: ["count": found]
        )
    }
}

enum StoreError: Error {
    case failedVerification
    case productNotFound
}

