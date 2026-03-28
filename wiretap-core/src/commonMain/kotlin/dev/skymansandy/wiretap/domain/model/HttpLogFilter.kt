package dev.skymansandy.wiretap.domain.model

data class HttpLogFilter(
    val statusGroups: Set<StatusGroup> = emptySet(),
    val methods: Set<String> = emptySet(),
    val sources: Set<ResponseSource> = emptySet(),
    val domains: Set<String> = emptySet(),
) {

    val isActive: Boolean
        get() = statusGroups.isNotEmpty() || methods.isNotEmpty() ||
            sources.isNotEmpty() || domains.isNotEmpty()

    val activeCount: Int
        get() = listOf(
            statusGroups.isNotEmpty(),
            methods.isNotEmpty(),
            sources.isNotEmpty(),
            domains.isNotEmpty(),
        ).count { it }
}

enum class StatusGroup(
    val label: String,
    val statusMin: Long?,
    val statusMax: Long?,
) {

    All("All", null, null),
    InProgress("In Progress", -2, -2),
    Success("2xx", 200, 299),
    Redirect("3xx", 300, 399),
    ClientError("4xx", 400, 499),
    ServerError("5xx", 500, 599),
    Failed("Failed", -1, 0),
}
