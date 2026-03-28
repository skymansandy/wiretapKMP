package dev.skymansandy.wiretap.ui.screens.socket.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.SocketStatus

@Composable
internal fun StatusChip(
    modifier: Modifier = Modifier,
    status: SocketStatus,
) {
    Text(
        text = status.name,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .padding(end = 12.dp)
            .background(status.bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun Preview_StatusChipOpen() {
    MaterialTheme {
        StatusChip(status = SocketStatus.Open)
    }
}

@Preview
@Composable
private fun Preview_StatusChipFailed() {
    MaterialTheme {
        StatusChip(status = SocketStatus.Failed)
    }
}
