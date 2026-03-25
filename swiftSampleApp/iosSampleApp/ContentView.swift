import Foundation
import SwiftUI
import WiretapURLSession

// MARK: - Color palette matching Android sample

private let colorSuccess = Color(red: 0.400, green: 0.733, blue: 0.416)    // 0xFF66BB6A
private let colorRedirect = Color(red: 0.259, green: 0.647, blue: 0.961)   // 0xFF42A5F5
private let colorClientError = Color(red: 1.0, green: 0.655, blue: 0.149)  // 0xFFFFA726
private let colorServerError = Color(red: 0.937, green: 0.325, blue: 0.314) // 0xFFEF5350
private let colorTimeout = Color(red: 0.620, green: 0.620, blue: 0.620)    // 0xFF9E9E9E
private let colorCancel = Color(red: 0.620, green: 0.620, blue: 0.620)     // 0xFF9E9E9E
private let colorBatch = Color(red: 0.149, green: 0.651, blue: 0.604)      // 0xFF26A69A

// MARK: - Action Model

enum ActionCategory {
    case success, redirect, clientError, serverError, timeout, cancel, batch

    var color: Color {
        switch self {
        case .success: return colorSuccess
        case .redirect: return colorRedirect
        case .clientError: return colorClientError
        case .serverError: return colorServerError
        case .timeout: return colorTimeout
        case .cancel: return colorCancel
        case .batch: return colorBatch
        }
    }
}

struct ApiAction: Identifiable {
    let id = UUID()
    let label: String
    let category: ActionCategory
    let execute: (@escaping (String) -> Void) -> Void
}

// MARK: - Action Grouping (matches Compose groupActions)

private struct ActionGroup: Identifiable {
    let id = UUID()
    let title: String
    let actions: [(index: Int, action: ApiAction)]
}

private func groupActions(_ actions: [ApiAction]) -> [ActionGroup] {
    var groups: [(String, [(Int, ApiAction)])] = []
    var groupMap: [String: Int] = [:]

    for (index, action) in actions.enumerated() {
        let key: String
        switch action.category {
        case .success:
            key = "Success"
        case .redirect, .clientError, .serverError:
            key = "!Success"
        case .timeout, .cancel:
            key = "Timeouts"
        case .batch:
            key = "Burst"
        }

        if let groupIndex = groupMap[key] {
            groups[groupIndex].1.append((index, action))
        } else {
            groupMap[key] = groups.count
            groups.append((key, [(index, action)]))
        }
    }

    return groups.map { ActionGroup(title: $0.0, actions: $0.1.map { (index: $0.0, action: $0.1) }) }
}

// MARK: - Content View

struct ContentView: View {
    @State private var statusLog = "Ready. Tap a button to make a request."
    @State private var selectedGroupIndex = 0

    private let interceptor = WiretapURLSessionInterceptor(session: .shared) { config in
        config.enabled = true
        config.logRetention = LogRetentionDays(days: 7)
        config.headerAction = { key in
            if key.caseInsensitiveCompare("Authorization") == .orderedSame {
                return HeaderActionMask(mask: "***")
            }
            return HeaderActionKeep.shared
        }
        config.shouldLog = { url, method in
            return KotlinBoolean(value: true)
        }
    }

    private var apiActions: [ApiAction] {
        [
            ApiAction(label: "GET /get (HTTP)", category: .success) { onStatus in
                onStatus("GET /get ...")
                self.makeRequest(url: "http://httpbin.org/get", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "GET /posts/1", category: .success) { onStatus in
                onStatus("GET /posts/1 ...")
                self.makeRequest(url: "https://jsonplaceholder.typicode.com/posts/1", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "GET large json", category: .success) { onStatus in
                onStatus("GET /users ...")
                let url = "https://gist.githubusercontent.com/gcollazo/884a489a50aec7b53765405f40c6fbd1/raw/49d1568c34090587ac82e80612a9c350108b62c5/sample.json"
                self.makeRequest(url: url, method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "GET /comments", category: .success) { onStatus in
                onStatus("GET /posts/1/comments ...")
                self.makeRequest(url: "https://jsonplaceholder.typicode.com/posts/1/comments", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "POST /posts", category: .success) { onStatus in
                onStatus("POST /posts ...")
                self.makePostRequest(
                    url: "https://jsonplaceholder.typicode.com/posts",
                    body: #"{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}"#,
                    contentType: "application/json"
                ) { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "GET /headers", category: .success) { onStatus in
                onStatus("GET /headers (custom) ...")
                self.makeRequestWithHeaders(
                    url: "https://httpbin.org/headers",
                    method: "GET",
                    headers: [
                        "X-Wiretap-Debug": "true",
                        "X-Request-Source": "WiretapSampleApp",
                        "X-Correlation-Id": "abc-123-def-456",
                        "Accept-Language": "en-US,en;q=0.9",
                    ]
                ) { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "POST /anything", category: .success) { onStatus in
                onStatus("POST /anything (headers+body) ...")
                self.makePostRequestWithHeaders(
                    url: "https://httpbin.org/anything",
                    body: #"{"event":"purchase","item":"Wiretap Pro","quantity":3,"metadata":{"source":"sample-app","version":"1.0"}}"#,
                    contentType: "application/json",
                    headers: [
                        "X-Api-Key": "sample-key-12345",
                        "X-Idempotency-Key": "idem-99887766",
                        "X-Custom-Trace": "trace-aabbccdd",
                    ]
                ) { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "GET 64KB JSON", category: .success) { onStatus in
                onStatus("GET /64KB.json ...")
                self.makeRequest(url: "https://microsoftedge.github.io/Demos/json-dummy-data/64KB.json", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "GET 5MB JSON", category: .success) { onStatus in
                onStatus("GET /5MB.json ...")
                self.makeRequest(url: "https://microsoftedge.github.io/Demos/json-dummy-data/5MB.json", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "301 Redirect", category: .redirect) { onStatus in
                onStatus("GET /redirect/1 ...")
                self.makeRequest(url: "https://httpbin.org/redirect/1", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "404 Not Found", category: .clientError) { onStatus in
                onStatus("GET /status/404 ...")
                self.makeRequest(url: "https://httpbin.org/status/404", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "500 Error", category: .serverError) { onStatus in
                onStatus("GET /status/500 ...")
                self.makeRequest(url: "https://httpbin.org/status/500", method: "GET") { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "Timeout (3s)", category: .timeout) { onStatus in
                onStatus("GET /delay/10 (3s timeout) ...")
                self.makeTimeoutRequest(timeoutSeconds: 3) { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "Cancel in 1s", category: .cancel) { onStatus in
                onStatus("Starting request for cancellation ...")
                self.makeCancelRequest(cancelAfterMs: 1000) { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "4 reqs @ 4s interval", category: .batch) { onStatus in
                onStatus("Burst: 4 requests at 4s intervals ...")
                self.makeBurstRequest(count: 4, intervalMs: 4000) { result in
                    onStatus(result)
                }
            },
            ApiAction(label: "10 reqs, cancel prev", category: .batch) { onStatus in
                onStatus("Rapid cancel: 10 requests, only last completes ...")
                self.makeRapidCancelRequest(count: 10) { result in
                    onStatus(result)
                }
            },
        ]
    }

    var body: some View {
        let actions = apiActions
        let groups = groupActions(actions)

        VStack(spacing: 12) {

            Text("HTTP Requests")
                .font(.title3)
                .frame(maxWidth: .infinity, alignment: .center)

            StatusWindow(statusLog: statusLog)
                .frame(maxHeight: .infinity)

            ActionButtonGrid(
                groups: groups,
                selectedGroupIndex: $selectedGroupIndex,
                onAction: { index in
                    actions[index].execute { status in
                        DispatchQueue.main.async {
                            self.statusLog = status
                        }
                    }
                },
            )
            .frame(maxHeight: .infinity)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    // MARK: - Network Helpers

    private static let maxBodyDisplayLength = 16_384

    private func formatResponse(response: HTTPURLResponse?, data: Data?) -> String {
        let statusCode = response?.statusCode ?? 0
        let statusText = HTTPURLResponse.localizedString(forStatusCode: statusCode)
        var result = "HTTP \(statusCode) \(statusText)\n"

        if let headers = response?.allHeaderFields as? [String: Any] {
            for (key, value) in headers.sorted(by: { $0.key < $1.key }) {
                result += "\(key): \(value)\n"
            }
        }
        result += "\n"

        if let data = data, let body = String(data: data, encoding: .utf8) {
            if body.count > Self.maxBodyDisplayLength {
                result += String(body.prefix(Self.maxBodyDisplayLength))
                result += "\n\n… (truncated \(body.count - Self.maxBodyDisplayLength) chars)"
            } else {
                result += body
            }
        }

        return result
    }

    private func makeRequest(url: String, method: String, completion: @escaping (String) -> Void) {
        guard let nsUrl = NSURL(string: url) else { return }
        let request = NSMutableURLRequest(url: nsUrl as URL)
        request.httpMethod = method

        interceptor.intercept(request: request as URLRequest) { data, response, error in
            if let error = error {
                completion("Error: \(error.localizedDescription)")
                return
            }
            completion(self.formatResponse(response: response as? HTTPURLResponse, data: data))
        }
    }

    private func makeRequestWithHeaders(url: String, method: String, headers: [String: String], completion: @escaping (String) -> Void) {
        guard let nsUrl = NSURL(string: url) else { return }
        let request = NSMutableURLRequest(url: nsUrl as URL)
        request.httpMethod = method
        for (key, value) in headers {
            request.setValue(value, forHTTPHeaderField: key)
        }

        interceptor.intercept(request: request as URLRequest) { data, response, error in
            if let error = error {
                completion("Error: \(error.localizedDescription)")
                return
            }
            completion(self.formatResponse(response: response as? HTTPURLResponse, data: data))
        }
    }

    private func makePostRequest(url: String, body: String, contentType: String, completion: @escaping (String) -> Void) {
        guard let nsUrl = NSURL(string: url) else { return }
        let request = NSMutableURLRequest(url: nsUrl as URL)
        request.httpMethod = "POST"
        request.setValue(contentType, forHTTPHeaderField: "Content-Type")
        request.httpBody = body.data(using: .utf8)

        interceptor.intercept(request: request as URLRequest) { data, response, error in
            if let error = error {
                completion("Error: \(error.localizedDescription)")
                return
            }
            completion(self.formatResponse(response: response as? HTTPURLResponse, data: data))
        }
    }

    private func makePostRequestWithHeaders(url: String, body: String, contentType: String, headers: [String: String], completion: @escaping (String) -> Void) {
        guard let nsUrl = NSURL(string: url) else { return }
        let request = NSMutableURLRequest(url: nsUrl as URL)
        request.httpMethod = "POST"
        request.setValue(contentType, forHTTPHeaderField: "Content-Type")
        request.httpBody = body.data(using: .utf8)
        for (key, value) in headers {
            request.setValue(value, forHTTPHeaderField: key)
        }

        interceptor.intercept(request: request as URLRequest) { data, response, error in
            if let error = error {
                completion("Error: \(error.localizedDescription)")
                return
            }
            completion(self.formatResponse(response: response as? HTTPURLResponse, data: data))
        }
    }

    private func makeTimeoutRequest(timeoutSeconds: TimeInterval, completion: @escaping (String) -> Void) {
        guard let url = NSURL(string: "https://httpbin.org/delay/10") else { return }
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = timeoutSeconds
        let session = URLSession(configuration: config)
        let timeoutInterceptor = WiretapURLSessionInterceptor(session: session) { _ in }

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

    private func makeCancelRequest(cancelAfterMs: Int, completion: @escaping (String) -> Void) {
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

        DispatchQueue.global().asyncAfter(deadline: .now() + Double(cancelAfterMs) / 1000.0) {
            task.cancel()
        }
    }

    private func makeBurstRequest(count: Int, intervalMs: Int, completion: @escaping (String) -> Void) {
        for i in 1...count {
            let delay = Double(i - 1) * Double(intervalMs) / 1000.0
            DispatchQueue.global().asyncAfter(deadline: .now() + delay) {
                let urlString = "https://jsonplaceholder.typicode.com/posts/\(i)"
                guard let nsUrl = NSURL(string: urlString) else { return }
                let request = NSMutableURLRequest(url: nsUrl as URL)
                request.httpMethod = "GET"

                self.interceptor.intercept(request: request as URLRequest) { data, response, error in
                    if let error = error {
                        completion("Burst \(i)/\(count): Error \(error.localizedDescription)")
                    } else {
                        let code = (response as? HTTPURLResponse)?.statusCode ?? 0
                        completion("Burst \(i)/\(count): HTTP \(code)")
                    }
                }
            }
        }
    }

    private func makeRapidCancelRequest(count: Int, completion: @escaping (String) -> Void) {
        var previousTask: URLSessionDataTask?

        for i in 1...count {
            let delay = Double(i - 1) * 0.01
            DispatchQueue.global().asyncAfter(deadline: .now() + delay) {
                previousTask?.cancel()

                let urlString = "https://jsonplaceholder.typicode.com/posts/\(i)"
                guard let nsUrl = NSURL(string: urlString) else { return }
                let request = NSMutableURLRequest(url: nsUrl as URL)
                request.httpMethod = "GET"

                let task = self.interceptor.dataTask(request: request as URLRequest) { data, response, error in
                    if let error = error {
                        // Cancelled tasks — ignore silently
                        return
                    }
                    let code = (response as? HTTPURLResponse)?.statusCode ?? 0
                    completion("Request \(i)/\(count): HTTP \(code)")
                }
                previousTask = task
                task.resume()
            }
        }
    }
}

// MARK: - Status Window (matches Compose StatusWindow)

private struct StatusWindow: View {
    let statusLog: String

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Status")
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    Text(statusLog)
                        .font(.system(.caption, design: .monospaced))
                        .foregroundColor(.primary)
                        .padding(.top, 4)

                    Spacer()
                        .frame(height: 0)
                        .id("bottom")
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(12)
            }
            .background(Color(.systemGray6))
            .cornerRadius(12)
            .onChange(of: statusLog) { _ in
                withAnimation {
                    proxy.scrollTo("bottom")
                }
            }
        }
    }
}

// MARK: - Action Button Grid (matches Compose ActionButtonGrid with tabs + pager)

private struct ActionButtonGrid: View {
    let groups: [ActionGroup]
    @Binding var selectedGroupIndex: Int
    let onAction: (Int) -> Void

    var body: some View {
        VStack(spacing: 0) {

            // Tab row (matches SecondaryTabRow)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 0) {
                    ForEach(groups.indices, id: \.self) { index in
                        Button {
                            withAnimation { selectedGroupIndex = index }
                        } label: {
                            VStack(spacing: 6) {
                                Text(groups[index].title)
                                    .font(.subheadline)
                                    .fontWeight(selectedGroupIndex == index ? .semibold : .regular)
                                    .foregroundColor(selectedGroupIndex == index ? .primary : .secondary)
                                    .padding(.horizontal, 16)
                                    .padding(.top, 8)

                                Rectangle()
                                    .fill(selectedGroupIndex == index ? Color.accentColor : Color.clear)
                                    .frame(height: 2)
                            }
                        }
                    }
                }
                .frame(maxWidth: .infinity)
            }

            Divider()

            // Pager content (matches HorizontalPager with FlowRow)
            TabView(selection: $selectedGroupIndex) {
                ForEach(groups.indices, id: \.self) { pageIndex in
                    ScrollView {
                        FlowLayout(spacing: 8) {
                            ForEach(groups[pageIndex].actions, id: \.action.id) { item in
                                Button {
                                    onAction(item.index)
                                } label: {
                                    Text(item.action.label)
                                        .font(.caption)
                                        .fontWeight(.bold)
                                        .foregroundColor(item.action.category.color)
                                        .lineLimit(1)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 8)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(item.action.category.color.opacity(0.5), lineWidth: 1)
                                        )
                                }
                            }
                        }
                        .padding(12)
                    }
                    .tag(pageIndex)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
    }
}

// MARK: - FlowLayout (matches Compose FlowRow)

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrange(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrange(proposal: proposal, subviews: subviews)
        for (index, position) in result.positions.enumerated() {
            subviews[index].place(
                at: CGPoint(x: bounds.minX + position.x, y: bounds.minY + position.y),
                proposal: .unspecified
            )
        }
    }

    private struct ArrangeResult {
        var size: CGSize
        var positions: [CGPoint]
    }

    private func arrange(proposal: ProposedViewSize, subviews: Subviews) -> ArrangeResult {
        let maxWidth = proposal.width ?? .infinity
        var positions: [CGPoint] = []
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var rowHeight: CGFloat = 0
        var totalHeight: CGFloat = 0
        var totalWidth: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > maxWidth && currentX > 0 {
                currentX = 0
                currentY += rowHeight + spacing
                rowHeight = 0
            }
            positions.append(CGPoint(x: currentX, y: currentY))
            rowHeight = max(rowHeight, size.height)
            currentX += size.width + spacing
            totalWidth = max(totalWidth, currentX - spacing)
            totalHeight = max(totalHeight, currentY + rowHeight)
        }

        return ArrangeResult(size: CGSize(width: totalWidth, height: totalHeight), positions: positions)
    }
}
