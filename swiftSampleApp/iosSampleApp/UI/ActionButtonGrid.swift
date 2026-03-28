import SwiftUI

struct ActionButtonGrid: View {
    let groups: [ActionGroup]
    @Binding var selectedGroupIndex: Int
    let onAction: (Int) -> Void

    var body: some View {
        VStack(spacing: 0) {
            tabRow
            Divider()
            pagerContent
        }
    }

    // MARK: - Tab Row

    private var tabRow: some View {
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
    }

    // MARK: - Pager Content

    private var pagerContent: some View {
        TabView(selection: $selectedGroupIndex) {
            ForEach(groups.indices, id: \.self) { pageIndex in
                ScrollView {
                    FlowLayout(spacing: 8) {
                        ForEach(groups[pageIndex].actions, id: \.action.id) { item in
                            actionButton(for: item)
                        }
                    }
                    .padding(12)
                }
                .tag(pageIndex)
            }
        }
        .tabViewStyle(.page(indexDisplayMode: .never))
    }

    // MARK: - Action Button

    private func actionButton(for item: (index: Int, action: ApiAction)) -> some View {
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
