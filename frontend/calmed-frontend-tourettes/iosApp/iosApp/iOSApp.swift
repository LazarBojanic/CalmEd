import SwiftUI
import ComposeApp
import GoogleSignIn
import UserNotifications

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

@main
struct iOSApp: App {
	init(){
		Frameworks_iosKt.doInitKoinIos()
		UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
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
