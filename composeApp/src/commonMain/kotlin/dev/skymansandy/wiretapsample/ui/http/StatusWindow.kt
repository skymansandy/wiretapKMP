package dev.skymansandy.wiretapsample.ui.http

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.wiretapsample.resources.*

@Composable
internal fun StatusWindow(
    statusLog: String,
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val scrollState = rememberScrollState()

        LaunchedEffect(statusLog) {
            scrollState.animateScrollTo(0)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.status_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = statusLog,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Preview
@Composable
private fun StatusWindowPreview() {
    MaterialTheme {
        StatusWindow(
            statusLog = "GET https://api.example.com/users → 200 (142ms)\n" +
                "POST https://api.example.com/auth → 401 (89ms)\n" +
                "GET https://api.example.com/data → 500 (2034ms)",
        )
    }
}

@Preview
@Composable
private fun StatusWindowEmptyPreview() {
    MaterialTheme {
        StatusWindow(statusLog = "")
    }
}
