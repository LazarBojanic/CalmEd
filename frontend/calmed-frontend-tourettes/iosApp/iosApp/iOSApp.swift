import SwiftUI

@main
struct iOSApp: App {
	init(){
		Frameworks_iosKt.doInitKoinIos()
	}
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}