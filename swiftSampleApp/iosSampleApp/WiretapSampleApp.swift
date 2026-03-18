import SwiftUI
import WiretapURLSession

@main
struct WiretapSampleApp: App {
    
    init() {
        WiretapLauncher_iosKt.enableLaunchTool()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
