import Foundation

// MARK: - Action Model

struct ApiAction: Identifiable {
    let id = UUID()
    let label: String
    let category: ActionCategory
    let execute: (@escaping (String) -> Void) -> Void
}

// MARK: - Action Grouping

struct ActionGroup: Identifiable {
    let id = UUID()
    let title: String
    let actions: [(index: Int, action: ApiAction)]
}

func groupActions(_ actions: [ApiAction]) -> [ActionGroup] {
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
