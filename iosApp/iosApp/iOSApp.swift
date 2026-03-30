import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        MainViewControllerKt.MainViewController()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
