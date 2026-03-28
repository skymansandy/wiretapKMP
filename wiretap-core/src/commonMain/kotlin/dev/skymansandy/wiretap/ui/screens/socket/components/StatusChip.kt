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
import dev.skymansandy.wiretap.ui.theme.WiretapColors

@Composable
internal fun StatusChip(status: SocketStatus) {
    val bgColor = when (status) {
        SocketStatus.Connecting -> WiretapColors.StatusBlue
        SocketStatus.Open -> WiretapColors.StatusGreen
        SocketStatus.Closing -> WiretapColors.StatusAmber
        SocketStatus.Closed -> WiretapColors.StatusGray
        SocketStatus.Failed -> WiretapColors.StatusRed
    }
    val label = when (status) {
        SocketStatus.Connecting -> "Connecting"
        SocketStatus.Open -> "Open"
        SocketStatus.Closing -> "Closing"
        SocketStatus.Closed -> "Closed"
        SocketStatus.Failed -> "Failed"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .padding(end = 12.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun Preview_StatusChipOpen() {
    MaterialTheme {
        StatusChip(SocketStatus.Open)
    }
}

@Preview
@Composable
private fun Preview_StatusChipFailed() {
    MaterialTheme {
        StatusChip(SocketStatus.Failed)
    }
}
