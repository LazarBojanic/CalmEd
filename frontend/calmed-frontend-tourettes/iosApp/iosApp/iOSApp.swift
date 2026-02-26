import SwiftUI
import ComposeApp
import GoogleSignIn
import UserNotifications

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
        }
    }
}
