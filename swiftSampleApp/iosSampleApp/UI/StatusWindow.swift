import SwiftUI

struct StatusWindow: View {
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
