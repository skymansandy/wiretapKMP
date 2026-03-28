import Foundation
#if DEBUG
import WiretapURLSession
#endif

final class NetworkClient {

    #if DEBUG
    private let session: WiretapSession

    init() {
        session = WiretapSession(configuration: .default) { config in
            config.enabled = true
            config.logRetention = LogRetentionDays(days: 7)
            config.headerAction = { key in
                if key.caseInsensitiveCompare("Authorization") == .orderedSame {
                    return HeaderActionMask(mask: "***")
                }
                return HeaderActionKeep.shared
            }
            config.shouldLog = { _, _ in
                return KotlinBoolean(value: true)
            }
        }
    }
    #else
    private let session = URLSession.shared

    init() {}
    #endif

    // MARK: - Request Builders

    func makeRequest(
        url: String,
        method: String,
        completion: @escaping (String) -> Void
    ) {
        guard let request = buildRequest(url: url, method: method) else { return }

        execute(request) { data, response, error in
            completion(error.map { "Error: \($0.localizedDescription)" }
                       ?? Self.formatResponse(response: response as? HTTPURLResponse, data: data))
        }
    }

    func makeRequest(
        url: String,
        method: String,
        headers: [String: String],
        completion: @escaping (String) -> Void
    ) {
        guard let request = buildRequest(url: url, method: method, headers: headers) else { return }

        execute(request) { data, response, error in
            completion(error.map { "Error: \($0.localizedDescription)" }
                       ?? Self.formatResponse(response: response as? HTTPURLResponse, data: data))
        }
    }

    func makePostRequest(
        url: String,
        body: String,
        contentType: String,
        headers: [String: String] = [:],
        completion: @escaping (String) -> Void
    ) {
        guard let request = buildRequest(
            url: url,
            method: "POST",
            headers: headers,
            body: body,
            contentType: contentType
        ) else { return }

        execute(request) { data, response, error in
            completion(error.map { "Error: \($0.localizedDescription)" }
                       ?? Self.formatResponse(response: response as? HTTPURLResponse, data: data))
        }
    }

    func makeTimeoutRequest(
        timeoutSeconds: TimeInterval,
        completion: @escaping (String) -> Void
    ) {
        guard let request = buildRequest(url: "https://httpbin.org/delay/10", method: "GET") else { return }

        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = timeoutSeconds

        #if DEBUG
        let timeoutSession = WiretapSession(configuration: config) { _ in }
        timeoutSession.intercept(request: request) { _, _, error in
            completion(error.map { "Timeout: \($0.localizedDescription)" } ?? "Unexpected success")
        }
        #else
        URLSession(configuration: config).dataTask(with: request) { _, _, error in
            completion(error.map { "Timeout: \($0.localizedDescription)" } ?? "Unexpected success")
        }.resume()
        #endif
    }

    func makeCancelRequest(
        cancelAfterMs: Int,
        completion: @escaping (String) -> Void
    ) {
        guard let request = buildRequest(url: "https://httpbin.org/delay/10", method: "GET") else { return }

        let task = createTask(request) { _, _, error in
            completion(error.map { "Cancelled: \($0.localizedDescription)" } ?? "Unexpected success")
        }
        task.resume()

        DispatchQueue.global().asyncAfter(deadline: .now() + Double(cancelAfterMs) / 1000.0) {
            task.cancel()
        }
    }

    func makeBurstRequest(
        count: Int,
        intervalMs: Int,
        completion: @escaping (String) -> Void
    ) {
        for i in 1...count {
            let delay = Double(i - 1) * Double(intervalMs) / 1000.0
            DispatchQueue.global().asyncAfter(deadline: .now() + delay) {
                let urlString = "https://jsonplaceholder.typicode.com/posts/\(i)"
                guard let request = self.buildRequest(url: urlString, method: "GET") else { return }

                self.execute(request) { _, response, error in
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

    func makeRapidCancelRequest(
        count: Int,
        completion: @escaping (String) -> Void
    ) {
        var previousTask: URLSessionDataTask?

        for i in 1...count {
            let delay = Double(i - 1) * 0.01
            DispatchQueue.global().asyncAfter(deadline: .now() + delay) {
                previousTask?.cancel()

                let urlString = "https://jsonplaceholder.typicode.com/posts/\(i)"
                guard let request = self.buildRequest(url: urlString, method: "GET") else { return }

                let task = self.createTask(request) { _, response, error in
                    guard error == nil else { return }
                    let code = (response as? HTTPURLResponse)?.statusCode ?? 0
                    completion("Request \(i)/\(count): HTTP \(code)")
                }
                previousTask = task
                task.resume()
            }
        }
    }

    // MARK: - Session Bridge

    private func execute(
        _ request: URLRequest,
        completion: @escaping (Data?, URLResponse?, Error?) -> Void
    ) {
        #if DEBUG
        session.intercept(request: request) { data, response, error in
            completion(data as Data?, response, error)
        }
        #else
        session.dataTask(with: request) { data, response, error in
            completion(data, response, error)
        }.resume()
        #endif
    }

    private func createTask(
        _ request: URLRequest,
        completion: @escaping (Data?, URLResponse?, Error?) -> Void
    ) -> URLSessionDataTask {
        #if DEBUG
        session.dataTask(request: request) { data, response, error in
            completion(data as Data?, response, error)
        }
        #else
        session.dataTask(with: request) { data, response, error in
            completion(data, response, error)
        }
        #endif
    }

    // MARK: - Private Helpers

    private func buildRequest(
        url: String,
        method: String,
        headers: [String: String] = [:],
        body: String? = nil,
        contentType: String? = nil
    ) -> URLRequest? {
        guard let url = URL(string: url) else { return nil }

        var request = URLRequest(url: url)
        request.httpMethod = method

        if let contentType {
            request.setValue(contentType, forHTTPHeaderField: "Content-Type")
        }
        if let body {
            request.httpBody = body.data(using: .utf8)
        }
        for (key, value) in headers {
            request.setValue(value, forHTTPHeaderField: key)
        }

        return request
    }

    private static let maxBodyDisplayLength = 16_384

    private static func formatResponse(response: HTTPURLResponse?, data: Data?) -> String {
        let statusCode = response?.statusCode ?? 0
        let statusText = HTTPURLResponse.localizedString(forStatusCode: statusCode)
        var result = "HTTP \(statusCode) \(statusText)\n"

        if let headers = response?.allHeaderFields as? [String: Any] {
            for (key, value) in headers.sorted(by: { $0.key < $1.key }) {
                result += "\(key): \(value)\n"
            }
        }
        result += "\n"

        if let data, let body = String(data: data, encoding: .utf8) {
            if body.count > maxBodyDisplayLength {
                result += String(body.prefix(maxBodyDisplayLength))
                result += "\n\n… (truncated \(body.count - maxBodyDisplayLength) chars)"
            } else {
                result += body
            }
        }

        return result
    }
}
