package dev.skymansandy.wiretapsample.ui.design.sections

import dev.skymansandy.wiretap.design.component.badge.TagKind
import dev.skymansandy.wiretap.design.component.list.LogRowData
import dev.skymansandy.wiretap.design.component.list.RuleRowData
import dev.skymansandy.wiretap.design.component.list.RuleTag
import dev.skymansandy.wiretap.design.foundation.HttpMethod
import dev.skymansandy.wiretap.design.foundation.LogSource

internal val SampleJson = """
    {"id":"tsk_8h2k","title":"Ship v2 API","done":false,"tags":["api","p1"],"assignee":{"id":47,"name":"rui"},"created":"2026-06-01T14:02:41Z"}
""".trimIndent()

internal val SampleCurl = """
    $ curl -X POST https://api.beacon.dev/v2/tasks \
      -H "authorization: Bearer ••••" \
      -H "content-type: application/json" \
      -d '{"title":"Ship v2 API"}'
""".trimIndent()

internal val SampleLogRows: List<LogRowData> = listOf(
    LogRowData(HttpMethod.GET, 200, "api.beacon.dev/v2/tasks", "14:02:41.881", "124 ms", listOf("↓ 18.0 KB"), LogSource.Network),
    LogRowData(HttpMethod.POST, 201, "api.beacon.dev/v2/tasks", "14:02:38.014", "312 ms", listOf("↑ 388 B", "↓ 412 B"), LogSource.Mock),
    LogRowData(HttpMethod.DELETE, 204, "api.beacon.dev/v2/tasks/8h2", "14:02:30.550", "1.21s", listOf("↓ 0 B"), LogSource.Throttled),
    LogRowData(HttpMethod.GET, 404, "cdn.beacon.dev/avatars/47.png", "14:02:22.310", "88 ms", listOf("↓ 512 B"), LogSource.Network),
)

internal val SampleRuleRows: List<RuleRowData> = listOf(
    RuleRowData(
        method = HttpMethod.POST,
        criteria = listOf(RuleTag("URL"), RuleTag("BODY~")),
        action = RuleTag("Mock", TagKind.Mock),
        pattern = "/v2/tasks",
        actionDetail = "→ 201 Created",
    ),
    RuleRowData(
        method = null,
        criteria = listOf(RuleTag("URL*")),
        action = RuleTag("Throttle", TagKind.Throttle),
        pattern = "*.beacon.dev/v2/workspaces",
        actionDetail = "+200–600ms",
    ),
)

internal val SampleHeaders: List<Pair<String, String>> = listOf(
    "content-type" to "application/json; charset=utf-8",
    "cache-control" to "no-store",
    "x-request-id" to "req_8h2k9q",
    "content-length" to "18432",
)
