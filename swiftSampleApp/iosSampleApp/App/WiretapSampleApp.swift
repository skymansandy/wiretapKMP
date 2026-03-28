import SwiftUI
import WiretapURLSession

@main
struct WiretapSampleApp: App {
    
    init() {
        WiretapLauncher_iosKt.enableWiretapLauncher()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
