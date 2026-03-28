import SwiftUI

struct ContentView: View {
    @State private var statusLog = "Ready. Tap a button to make a request."
    @State private var selectedGroupIndex = 0

    private let client = NetworkClient()

    private var apiActions: [ApiAction] {
        [
            ApiAction(label: "GET /get (HTTP)", category: .success) { onStatus in
                onStatus("GET /get ...")
                self.client.makeRequest(url: "http://httpbin.org/get", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "GET /posts/1", category: .success) { onStatus in
                onStatus("GET /posts/1 ...")
                self.client.makeRequest(url: "https://jsonplaceholder.typicode.com/posts/1", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "GET large json", category: .success) { onStatus in
                onStatus("GET /users ...")
                self.client.makeRequest(
                    url: "https://gist.githubusercontent.com/gcollazo/884a489a50aec7b53765405f40c6fbd1/raw/49d1568c34090587ac82e80612a9c350108b62c5/sample.json",
                    method: "GET"
                ) { onStatus($0) }
            },
            ApiAction(label: "GET /comments", category: .success) { onStatus in
                onStatus("GET /posts/1/comments ...")
                self.client.makeRequest(url: "https://jsonplaceholder.typicode.com/posts/1/comments", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "POST /posts", category: .success) { onStatus in
                onStatus("POST /posts ...")
                self.client.makePostRequest(
                    url: "https://jsonplaceholder.typicode.com/posts",
                    body: #"{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}"#,
                    contentType: "application/json"
                ) { onStatus($0) }
            },
            ApiAction(label: "GET /headers", category: .success) { onStatus in
                onStatus("GET /headers (custom) ...")
                self.client.makeRequest(
                    url: "https://httpbin.org/headers",
                    method: "GET",
                    headers: [
                        "X-Wiretap-Debug": "true",
                        "X-Request-Source": "WiretapSampleApp",
                        "X-Correlation-Id": "abc-123-def-456",
                        "Accept-Language": "en-US,en;q=0.9",
                    ]
                ) { onStatus($0) }
            },
            ApiAction(label: "POST /anything", category: .success) { onStatus in
                onStatus("POST /anything (headers+body) ...")
                self.client.makePostRequest(
                    url: "https://httpbin.org/anything",
                    body: #"{"event":"purchase","item":"Wiretap Pro","quantity":3,"metadata":{"source":"sample-app","version":"1.0"}}"#,
                    contentType: "application/json",
                    headers: [
                        "X-Api-Key": "sample-key-12345",
                        "X-Idempotency-Key": "idem-99887766",
                        "X-Custom-Trace": "trace-aabbccdd",
                    ]
                ) { onStatus($0) }
            },
            ApiAction(label: "GET 64KB JSON", category: .success) { onStatus in
                onStatus("GET /64KB.json ...")
                self.client.makeRequest(url: "https://microsoftedge.github.io/Demos/json-dummy-data/64KB.json", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "GET 5MB JSON", category: .success) { onStatus in
                onStatus("GET /5MB.json ...")
                self.client.makeRequest(url: "https://microsoftedge.github.io/Demos/json-dummy-data/5MB.json", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "301 Redirect", category: .redirect) { onStatus in
                onStatus("GET /redirect/1 ...")
                self.client.makeRequest(url: "https://httpbin.org/redirect/1", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "404 Not Found", category: .clientError) { onStatus in
                onStatus("GET /status/404 ...")
                self.client.makeRequest(url: "https://httpbin.org/status/404", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "500 Error", category: .serverError) { onStatus in
                onStatus("GET /status/500 ...")
                self.client.makeRequest(url: "https://httpbin.org/status/500", method: "GET") { onStatus($0) }
            },
            ApiAction(label: "Timeout (3s)", category: .timeout) { onStatus in
                onStatus("GET /delay/10 (3s timeout) ...")
                self.client.makeTimeoutRequest(timeoutSeconds: 3) { onStatus($0) }
            },
            ApiAction(label: "Cancel in 1s", category: .cancel) { onStatus in
                onStatus("Starting request for cancellation ...")
                self.client.makeCancelRequest(cancelAfterMs: 1000) { onStatus($0) }
            },
            ApiAction(label: "4 reqs @ 4s interval", category: .batch) { onStatus in
                onStatus("Burst: 4 requests at 4s intervals ...")
                self.client.makeBurstRequest(count: 4, intervalMs: 4000) { onStatus($0) }
            },
            ApiAction(label: "10 reqs, cancel prev", category: .batch) { onStatus in
                onStatus("Rapid cancel: 10 requests, only last completes ...")
                self.client.makeRapidCancelRequest(count: 10) { onStatus($0) }
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
}
