package dev.skymansandy.wiretap.ui.screens.console.http.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.source_mock
import dev.skymansandy.wiretap.resources.source_throttle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SourceChip(
    modifier: Modifier = Modifier,
    source: ResponseSource,
) {
    val bgColor = when (source) {
        ResponseSource.Mock -> MaterialTheme.colorScheme.secondaryContainer
        ResponseSource.Throttle -> MaterialTheme.colorScheme.tertiaryContainer
        ResponseSource.Network -> return
    }

    val textColor = when (source) {
        ResponseSource.Mock -> MaterialTheme.colorScheme.onSecondaryContainer
        ResponseSource.Throttle -> MaterialTheme.colorScheme.onTertiaryContainer
        ResponseSource.Network -> return
    }

    val label = when (source) {
        ResponseSource.Mock -> stringResource(Res.string.source_mock)
        ResponseSource.Throttle -> stringResource(Res.string.source_throttle)
        ResponseSource.Network -> return
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

@Preview
@Composable
private fun Preview_SourceChipMock() {
    MaterialTheme {
        SourceChip(source = ResponseSource.Mock)
    }
}

@Preview
@Composable
private fun Preview_SourceChipThrottle() {
    MaterialTheme {
        SourceChip(source = ResponseSource.Throttle)
    }
}
