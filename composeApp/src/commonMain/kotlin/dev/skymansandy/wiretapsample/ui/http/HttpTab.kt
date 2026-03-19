package dev.skymansandy.wiretapsample.ui.http

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretapsample.model.actionColor
import dev.skymansandy.wiretapsample.model.httpActions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import wiretapkmp.composeapp.generated.resources.*

@Composable
internal fun HttpTab(client: HttpClient, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope {
        CoroutineExceptionHandler { _, _ -> }
    }
    val readyText = stringResource(Res.string.status_ready)
    val cancelStartText = stringResource(Res.string.starting_cancel)
    val cancelledText = stringResource(Res.string.request_cancelled)
    var statusLog by remember { mutableStateOf(readyText) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.http_requests),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        StatusWindow(statusLog)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(httpActions) { action ->
                val color = actionColor.getValue(action.category)
                Button(
                    onClick = {
                        scope.launch {
                            if (action.label == "Cancel") {
                                statusLog = cancelStartText
                                val job = launch {
                                    try {
                                        client.get("https://httpbin.org/delay/10") {
                                            timeout { requestTimeoutMillis = 30_000 }
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (_: Exception) {
                                        // ignored
                                    }
                                }
                                delay(500)
                                job.cancel()
                                statusLog = cancelledText
                            } else {
                                try {
                                    action.action(client) { statusLog = it }
                                } catch (e: Exception) {
                                    statusLog = "Error: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
