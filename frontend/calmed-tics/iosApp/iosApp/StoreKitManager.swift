import StoreKit

class StoreKitManager: ObservableObject {
    static let shared = StoreKitManager()
    
    @Published var purchasedProductIDs: Set<String> = []
    
    private var transactionListener: Task<Void, Error>? = nil
    private var restoreObserver: NSObjectProtocol? = nil
    
    init() {
        transactionListener = listenForTransactions()
        restoreObserver = NotificationCenter.default.addObserver(
            forName: NSNotification.Name("TriggerAppleRestore"),
                        object: nil,
            queue: .main
        ) { [weak self] _ in
            guard let self = self else { return }
            // Calling AppStore.sync() makes any owned (restored) transactions flow through
            // Transaction.updates, which listenForTransactions posts back to Kotlin.
            Task {
                try? await self.restorePurchases()
            }
        }
    }
    
    deinit {
        transactionListener?.cancel()
        if let restoreObserver = restoreObserver {
            NotificationCenter.default.removeObserver(restoreObserver)
        }
    }
    
    func listenForTransactions() -> Task<Void, Error> {
        Task.detached {
            for await result in Transaction.updates {
                do {
                    let transaction = try self.checkVerified(result)
                    
                    // Deliver content to user
                    await self.updatePurchasedProducts(transaction)
                    
                    // Always finish a transaction
                    await transaction.finish()
                    
                    // Notify Kotlin layer
                NotificationCenter.default.post(
                    name: NSNotification.Name("OnApplePurchaseSuccess"),
                    object: nil,
                    userInfo: [
                        "transactionId": transaction.originalID.description,
                        "productId": transaction.productID
                    ]
                )
                } catch {
                    print("Transaction verification failed")
                }
            }
        }
    }
    
    @MainActor
    func updatePurchasedProducts(_ transaction: Transaction) async {
        if transaction.revocationDate == nil {
            purchasedProductIDs.insert(transaction.productID)
        } else {
            purchasedProductIDs.remove(transaction.productID)
        }
    }
    
    func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
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
            await updatePurchasedProducts(transaction)
            await transaction.finish()
            
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: NSNotification.Name("OnApplePurchaseSuccess"),
                    object: nil,
                    userInfo: [
                        "transactionId": transaction.originalID.description,
                        "productId": transaction.productID
                    ]
                )
        }

        case .userCancelled:
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: NSNotification.Name("OnApplePurchaseFailure"),
                    object: nil,
                    userInfo: ["error": "User cancelled"]
                )
            }
        case .pending:
            break
        @unknown default:
            break
    }
}

    func restorePurchases() async throws {
        try await AppStore.sync()
}
}

enum StoreError: Error {
    case failedVerification
    case productNotFound
}

