import SwiftUI
import ComposeApp
import GoogleSignIn

@main
struct iOSApp: App {
	init(){
		Frameworks_iosKt.doInitKoinIos()
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