package dev.skymansandy.kurlclient.ui.request

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurlclient.ui.HttpMethod
import dev.skymansandy.kurlclient.ui.KeyValueEntry

private val REQUEST_TABS = listOf("Params", "Headers", "Auth", "Body")

@Composable
fun RequestPanel(
    url: String,
    method: HttpMethod,
    params: List<KeyValueEntry>,
    headers: List<KeyValueEntry>,
    body: String,
    isLoading: Boolean,
    onUrlChange: (String) -> Unit,
    onMethodChange: (HttpMethod) -> Unit,
    onParamUpdate: (Long, String, String, Boolean) -> Unit,
    onParamAdd: () -> Unit,
    onParamRemove: (Long) -> Unit,
    onHeaderUpdate: (Long, String, String, Boolean) -> Unit,
    onHeaderAdd: () -> Unit,
    onHeaderRemove: (Long) -> Unit,
    onBodyChange: (String) -> Unit,
    onSend: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        UrlBar(
            method = method,
            url = url,
            isLoading = isLoading,
            onMethodChange = onMethodChange,
            onUrlChange = onUrlChange,
            onSend = onSend,
            onSave = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
            REQUEST_TABS.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            when (selectedTab) {
                0 -> KeyValueEditor(
                    entries = params,
                    onUpdate = onParamUpdate,
                    onAdd = onParamAdd,
                    onRemove = onParamRemove,
                    keyPlaceholder = "param",
                    valuePlaceholder = "value"
                )
                1 -> KeyValueEditor(
                    entries = headers,
                    onUpdate = onHeaderUpdate,
                    onAdd = onHeaderAdd,
                    onRemove = onHeaderRemove,
                    keyPlaceholder = "Header",
                    valuePlaceholder = "value"
                )
                2 -> AuthTab()
                3 -> BodyTab(body = body, onBodyChange = onBodyChange)
            }
        }
    }
}

// ── URL Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun UrlBar(
    method: HttpMethod,
    url: String,
    isLoading: Boolean,
    onMethodChange: (HttpMethod) -> Unit,
    onUrlChange: (String) -> Unit,
    onSend: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = methodColor(method),
                modifier = Modifier.clip(RoundedCornerShape(6.dp))
            ) {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        text = method.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                HttpMethod.entries.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.name) },
                        onClick = { onMethodChange(m); expanded = false }
                    )
                }
            }
        }

        BasicTextField(
            value = url,
            onValueChange = onUrlChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (url.isEmpty()) {
                        Text(
                            "https://...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.weight(1f).fillMaxSize()
        )

        FilledTonalIconButton(
            onClick = onSave,
            enabled = !isLoading
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = "Save to collection")
        }

        Button(
            onClick = onSend,
            enabled = !isLoading,
            shape = RoundedCornerShape(6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send")
            }
        }
    }
}

// ── Key-Value Editor ──────────────────────────────────────────────────────────

@Composable
fun KeyValueEditor(
    entries: List<KeyValueEntry>,
    onUpdate: (Long, String, String, Boolean) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    keyPlaceholder: String = "Key",
    valuePlaceholder: String = "Value",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        entries.forEach { entry ->
            KeyValueRow(
                entry = entry,
                keyPlaceholder = keyPlaceholder,
                valuePlaceholder = valuePlaceholder,
                onUpdate = { key, value, enabled -> onUpdate(entry.id, key, value, enabled) },
                onRemove = { onRemove(entry.id) }
            )
        }

        TextButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" Add", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun KeyValueRow(
    entry: KeyValueEntry,
    keyPlaceholder: String,
    valuePlaceholder: String,
    onUpdate: (String, String, Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Checkbox(
            checked = entry.enabled,
            onCheckedChange = { onUpdate(entry.key, entry.value, it) },
            modifier = Modifier.size(20.dp)
        )

        InlineTextField(
            value = entry.key,
            placeholder = keyPlaceholder,
            onValueChange = { onUpdate(it, entry.value, entry.enabled) },
            modifier = Modifier.weight(1f)
        )

        Text(":", color = MaterialTheme.colorScheme.onSurfaceVariant)

        InlineTextField(
            value = entry.value,
            placeholder = valuePlaceholder,
            onValueChange = { onUpdate(entry.key, it, entry.enabled) },
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InlineTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        },
        modifier = modifier
    )
}

// ── Auth Tab ──────────────────────────────────────────────────────────────────

@Composable
private fun AuthTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Auth configuration",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Body Tab ──────────────────────────────────────────────────────────────────

@Composable
private fun BodyTab(body: String, onBodyChange: (String) -> Unit) {
    BasicTextField(
        value = body,
        onValueChange = onBodyChange,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                if (body.isEmpty()) {
                    Text(
                        "Request body (JSON, XML, raw...)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// ── Method color ──────────────────────────────────────────────────────────────

@Composable
private fun methodColor(method: HttpMethod) = when (method) {
    HttpMethod.GET -> MaterialTheme.colorScheme.primary
    HttpMethod.POST -> MaterialTheme.colorScheme.secondary
    HttpMethod.PUT -> MaterialTheme.colorScheme.tertiary
    HttpMethod.DELETE -> MaterialTheme.colorScheme.error
    HttpMethod.PATCH -> MaterialTheme.colorScheme.secondary
    HttpMethod.HEAD -> MaterialTheme.colorScheme.primary
    HttpMethod.OPTIONS -> MaterialTheme.colorScheme.tertiary
}