import Foundation
import UIKit

private var onShakeDetected: (() -> Void)? = nil

@objc public class WiretapShakeDetector: NSObject {

    @objc public func enableShakeDetector(callback: @escaping @convention(block) () -> Void) {
        onShakeDetected = callback
    }
}

extension UIWindow {
    open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        super.motionEnded(motion, with: event)
        if motion == .motionShake {
            onShakeDetected?()
        }
    }
}
