import SwiftUI
import ComposeApp
import GoogleSignIn
import UserNotifications
#if canImport(StripePaymentSheet)
import StripePaymentSheet
#endif

struct GoogleSignInHelper {
    static func signIn() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootViewController = windowScene.windows.first?.rootViewController else {
            return
        }
        
        GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
            if let error = error {
                GoogleAuthBridge.shared.onIdTokenFailure(message: error.localizedDescription)
            } else if let result = result {
                let idToken = result.user.idToken?.tokenString ?? ""
                GoogleAuthBridge.shared.onIdTokenSuccess(token: idToken)
            }
        }
    }
}

final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
	static let shared = NotificationDelegate()

	func userNotificationCenter(
		_ center: UNUserNotificationCenter,
		willPresent notification: UNNotification,
		withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
	) {
		completionHandler([.banner, .sound, .badge])
	}
}

final class StripePaymentCoordinator: ObservableObject {
    static let shared = StripePaymentCoordinator()
#if canImport(StripePaymentSheet)
    private var paymentSheet: PaymentSheet?
    private var pendingPaymentIntentId: String?
#endif

    func start(from notification: Notification) {
#if canImport(StripePaymentSheet)
        guard
            let payload = notification.userInfo,
            let paymentIntentId = payload["paymentIntentId"] as? String,
            let paymentIntentClientSecret = payload["paymentIntentClientSecret"] as? String,
            let customerId = payload["customerId"] as? String,
            let customerEphemeralKeySecret = payload["customerEphemeralKeySecret"] as? String,
            let publishableKey = payload["publishableKey"] as? String,
            let merchantDisplayName = payload["merchantDisplayName"] as? String,
            let merchantCountryCode = payload["merchantCountryCode"] as? String
        else {
            StripePaymentResultBridge.shared.onFailure(error: "Missing Stripe payment payload")
            return
        }

        StripeAPI.defaultPublishableKey = publishableKey
        pendingPaymentIntentId = paymentIntentId
        var config = PaymentSheet.Configuration()
        config.merchantDisplayName = merchantDisplayName
        config.customer = .init(id: customerId, ephemeralKeySecret: customerEphemeralKeySecret)
        if let appleMerchantId = payload["appleMerchantId"] as? String, !appleMerchantId.isEmpty {
            config.applePay = .init(merchantId: appleMerchantId, merchantCountryCode: merchantCountryCode)
        }

        paymentSheet = PaymentSheet(paymentIntentClientSecret: paymentIntentClientSecret, configuration: config)

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController,
              let paymentSheet = paymentSheet else {
            StripePaymentResultBridge.shared.onFailure(error: "Unable to present PaymentSheet")
            return
        }

        paymentSheet.present(from: rootVC) { [weak self] result in
            switch result {
            case .completed:
                if let id = self?.pendingPaymentIntentId {
                    StripePaymentResultBridge.shared.onSuccess(paymentIntentId: id)
                } else {
                    StripePaymentResultBridge.shared.onFailure(error: "Missing payment intent id")
                }
            case .canceled:
                StripePaymentResultBridge.shared.onFailure(error: "Payment canceled")
            case .failed(let error):
                StripePaymentResultBridge.shared.onFailure(error: error.localizedDescription)
            }
            self?.paymentSheet = nil
            self?.pendingPaymentIntentId = nil
        }
#else
        StripePaymentResultBridge.shared.onFailure(error: "Stripe iOS SDK not linked. Add stripe-ios package.")
#endif
    }
}

@main
struct iOSApp: App {
	init(){
		Frameworks_iosKt.doInitKoinIos()
		UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name("StartStripePayment"),
            object: nil,
            queue: .main
        ) { note in
            StripePaymentCoordinator.shared.start(from: note)
        }
	}
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
                .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("TriggerGoogleSignIn"))) { _ in
                    GoogleSignInHelper.signIn()
                }
        }
    }
}
