package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.model.NetworkLogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogDetailScreen(
    entry: NetworkLogEntry,
    onBack: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Request", "Response")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${entry.method} ${entry.responseCode}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab(entry)
                1 -> RequestTab(entry)
                2 -> ResponseTab(entry)
            }
        }
    }
}

@Composable
private fun OverviewTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailRow("URL", entry.url)
        DetailRow("Method", entry.method)
        DetailRow("Status", entry.responseCode.toString())
        DetailRow("Duration", "${entry.durationMs}ms")
        DetailRow("Source", entry.source.name)
    }
}

@Composable
private fun RequestTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle("Headers")
        if (entry.requestHeaders.isEmpty()) {
            Text("No headers", style = MaterialTheme.typography.bodyMedium)
        } else {
            entry.requestHeaders.forEach { (key, value) ->
                DetailRow(key, value)
            }
        }

        SectionTitle("Body")
        CodeBlock(entry.requestBody ?: "No body")
    }
}

@Composable
private fun ResponseTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle("Headers")
        if (entry.responseHeaders.isEmpty()) {
            Text("No headers", style = MaterialTheme.typography.bodyMedium)
        } else {
            entry.responseHeaders.forEach { (key, value) ->
                DetailRow(key, value)
            }
        }

        SectionTitle("Body")
        CodeBlock(entry.responseBody ?: "No body")
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun CodeBlock(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
        )
    }
}
