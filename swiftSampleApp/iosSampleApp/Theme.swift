import SwiftUI

// MARK: - Color Palette (matching Android sample)

enum Theme {
    static let success = Color(red: 0.400, green: 0.733, blue: 0.416)       // #66BB6A
    static let redirect = Color(red: 0.259, green: 0.647, blue: 0.961)      // #42A5F5
    static let clientError = Color(red: 1.0, green: 0.655, blue: 0.149)     // #FFA726
    static let serverError = Color(red: 0.937, green: 0.325, blue: 0.314)   // #EF5350
    static let timeout = Color(red: 0.620, green: 0.620, blue: 0.620)       // #9E9E9E
    static let batch = Color(red: 0.149, green: 0.651, blue: 0.604)         // #26A69A
}

// MARK: - Action Category

enum ActionCategory {
    case success, redirect, clientError, serverError, timeout, cancel, batch

    var color: Color {
        switch self {
        case .success: return Theme.success
        case .redirect: return Theme.redirect
        case .clientError: return Theme.clientError
        case .serverError: return Theme.serverError
        case .timeout, .cancel: return Theme.timeout
        case .batch: return Theme.batch
        }
    }
}
