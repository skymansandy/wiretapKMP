import Foundation
import SwiftUI
import WiretapURLSession

// MARK: - Color palette matching Android sample
private let colorSuccess = Color(red: 0.298, green: 0.686, blue: 0.314)    // Green – 2xx
private let colorRedirect = Color(red: 0.259, green: 0.647, blue: 0.961)   // Blue – 3xx
private let colorClientError = Color(red: 1.0, green: 0.655, blue: 0.149)  // Amber – 4xx
private let colorServerError = Color(red: 0.937, green: 0.325, blue: 0.314) // Red – 5xx
private let colorEdgeCase = Color(red: 0.620, green: 0.620, blue: 0.620)   // Gray – edge

// MARK: - Action Model

enum ActionCategory {
    case success, redirect, clientError, serverError, edgeCase

    var color: Color {
        switch self {
        case .success: return colorSuccess
        case .redirect: return colorRedirect
        case .clientError: return colorClientError
        case .serverError: return colorServerError
        case .edgeCase: return colorEdgeCase
        }
    }
}

struct ApiAction: Identifiable {
    let id = UUID()
    let label: String
    let category: ActionCategory
    let execute: (@escaping (String) -> Void) -> Void
}

// MARK: - Content View

struct ContentView: View {
    @State private var statusLog = "Ready. Tap a button to make a request."

    private let interceptor = WiretapURLSessionInterceptor.companion.shared

    private var apiActions: [ApiAction] {
        [
            ApiAction(label: "GET /get (HTTP)", category: .success) { onStatus in
                onStatus("GET /get ...")
                self.makeRequest(url: "https://httpbin.org/get", method: "GET") { code in
                    onStatus("GET /get -> \(code)")
                }
            },
            ApiAction(label: "GET /posts/1", category: .success) { onStatus in
                onStatus("GET /posts/1 ...")
                self.makeRequest(url: "https://jsonplaceholder.typicode.com/posts/1", method: "GET") { code in
                    onStatus("GET /posts/1 -> \(code)")
                }
            },
            ApiAction(label: "GET large json", category: .success) { onStatus in
                onStatus("GET large json ...")
                let url = "https://gist.githubusercontent.com/gcollazo/884a489a50aec7b53765405f40c6fbd1/raw/49d1568c34090587ac82e80612a9c350108b62c5/sample.json"
                self.makeRequest(url: url, method: "GET") { code in
                    onStatus("GET large json -> \(code)")
                }
            },
            ApiAction(label: "GET /comments", category: .success) { onStatus in
                onStatus("GET /comments ...")
                self.makeRequest(url: "https://jsonplaceholder.typicode.com/posts/1/comments", method: "GET") { code in
                    onStatus("GET /comments -> \(code)")
                }
            },
            ApiAction(label: "POST /posts", category: .success) { onStatus in
                onStatus("POST /posts ...")
                self.makePostRequest { code in
                    onStatus("POST /posts -> \(code)")
                }
            },
            ApiAction(label: "301 Redirect", category: .redirect) { onStatus in
                onStatus("GET /redirect/1 ...")
                self.makeRequest(url: "https://httpbin.org/redirect/1", method: "GET") { code in
                    onStatus("GET /redirect/1 -> \(code)")
                }
            },
            ApiAction(label: "404 Not Found", category: .clientError) { onStatus in
                onStatus("GET /status/404 ...")
                self.makeRequest(url: "https://httpbin.org/status/404", method: "GET") { code in
                    onStatus("GET /status/404 -> \(code)")
                }
            },
            ApiAction(label: "500 Error", category: .serverError) { onStatus in
                onStatus("GET /status/500 ...")
                self.makeRequest(url: "https://httpbin.org/status/500", method: "GET") { code in
                    onStatus("GET /status/500 -> \(code)")
                }
            },
            ApiAction(label: "Timeout (6s)", category: .edgeCase) { onStatus in
                onStatus("GET /delay/10 (6s timeout) ...")
                self.makeTimeoutRequest { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "Cancel", category: .edgeCase) { onStatus in
                onStatus("Starting request, cancelling in 500ms...")
                self.makeCancelRequest { result in
                    onStatus(result)
                }
            },
        ]
    }

    var body: some View {
        VStack(spacing: 12) {
            // Title
            Text("Wiretap Sample (Native iOS)")
                .font(.headline)
                .fontWeight(.bold)
                .frame(maxWidth: .infinity, alignment: .leading)

            // Status window
            ScrollView {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Status")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text(statusLog)
                        .font(.system(.caption, design: .monospaced))
                        .foregroundColor(.primary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(12)
            }
            .frame(height: 120)
            .background(Color(.systemGray6))
            .cornerRadius(12)

            // Button grid
            ScrollView {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                    ForEach(apiActions) { action in
                        Button {
                            action.execute { status in
                                DispatchQueue.main.async {
                                    self.statusLog = status
                                }
                            }
                        } label: {
                            Text(action.label)
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .padding(.horizontal, 8)
                                .background(action.category.color)
                                .cornerRadius(8)
                        }
                    }
                }
            }

            Spacer()
        }
        .padding()
    }

    // MARK: - Network Helpers

    private func makeRequest(url: String, method: String, completion: @escaping (String) -> Void) {
        guard let nsUrl = NSURL(string: url) else { return }
        let request = NSMutableURLRequest(url: nsUrl as URL)
        request.httpMethod = method

        interceptor.intercept(request: request as URLRequest) { data, response, error in
            if let error = error {
                completion("Error: \(error.localizedDescription)")
                return
            }
            let code = response?.statusCode ?? 0
            completion("\(code)")
        }
    }

    private func makePostRequest(completion: @escaping (String) -> Void) {
        guard let url = NSURL(string: "https://jsonplaceholder.typicode.com/posts") else { return }
        let request = NSMutableURLRequest(url: url as URL)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        let body = #"{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}"#
        request.httpBody = body.data(using: .utf8)

        interceptor.intercept(request: request as URLRequest) { data, response, error in
            if let error = error {
                completion("Error: \(error.localizedDescription)")
                return
            }
            let code = response?.statusCode ?? 0
            completion("\(code)")
        }
    }

    private func makeTimeoutRequest(completion: @escaping (String) -> Void) {
        guard let url = NSURL(string: "https://httpbin.org/delay/10") else { return }
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 6
        let session = URLSession(configuration: config)
        let timeoutInterceptor = WiretapURLSessionInterceptor(session: session)

        let request = NSMutableURLRequest(url: url as URL)
        request.httpMethod = "GET"

        timeoutInterceptor.intercept(request: request as URLRequest) { _, _, error in
            if let error = error {
                completion("Timeout: \(error.localizedDescription)")
            } else {
                completion("Unexpected success")
            }
        }
    }

    private func makeCancelRequest(completion: @escaping (String) -> Void) {
        guard let url = NSURL(string: "https://httpbin.org/delay/10") else { return }
        let request = NSMutableURLRequest(url: url as URL)
        request.httpMethod = "GET"

        let task = interceptor.dataTask(request: request as URLRequest) { _, _, error in
            if let error = error {
                completion("Cancelled: \(error.localizedDescription)")
            } else {
                completion("Unexpected success")
            }
        }
        task.resume()

        DispatchQueue.global().asyncAfter(deadline: .now() + 0.5) {
            task.cancel()
        }
    }
}
