package dev.skymansandy.wiretap.ui.screens.http.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.TimingPhase
import dev.skymansandy.wiretap.ui.theme.WiretapColors

private val BAR_HEIGHT = 8.dp
private val MIN_BAR_WIDTH = 3.dp

@Composable
internal fun TimingGanttChart(
    modifier: Modifier = Modifier,
    phases: List<TimingPhase>,
    totalDurationMs: Long,
) {

    if (phases.isEmpty()) return

    val timelineEndMs = remember(phases, totalDurationMs) {
        maxOf(
            totalDurationMs.toDouble(),
            phases.maxOfOrNull { it.startMs + it.durationMs } ?: 0.0,
        )
    }

    if (timelineEndMs <= 0.0) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {

        Text(
            text = "Timing",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        phases.forEach { phase ->
            TimingBar(
                phase = phase,
                timelineEndMs = timelineEndMs,
            )
        }
    }
}

@Composable
private fun TimingBar(
    phase: TimingPhase,
    timelineEndMs: Double,
) {

    val color = phaseColor(phase.name)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Text(
            text = phase.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.widthIn(min = 60.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        BoxWithConstraints(
            modifier = Modifier.weight(1f).height(BAR_HEIGHT),
        ) {

            val trackWidth = maxWidth

            val startOffset: Dp = (trackWidth * (phase.startMs / timelineEndMs).toFloat())
                .coerceAtLeast(0.dp)

            val barWidth: Dp = (trackWidth * (phase.durationMs / timelineEndMs).toFloat())
                .coerceAtLeast(MIN_BAR_WIDTH)
                .coerceAtMost(trackWidth - startOffset)

            // Background track line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    ),
            )

            // Phase bar positioned on the timeline
            Row(modifier = Modifier.height(BAR_HEIGHT)) {
                if (startOffset > 0.dp) {
                    Spacer(modifier = Modifier.width(startOffset))
                }
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(BAR_HEIGHT)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color),
                )
            }
        }

        Text(
            text = formatPhaseDuration(phase.durationMs),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.widthIn(min = 56.dp),
        )
    }
}

private fun phaseColor(name: String): Color = when (name) {
    "DNS" -> WiretapColors.TimingDns
    "TCP" -> WiretapColors.TimingTcp
    "TLS" -> WiretapColors.TimingTls
    "Request" -> WiretapColors.TimingRequest
    "Waiting" -> WiretapColors.TimingWaiting
    "Download" -> WiretapColors.TimingDownload
    else -> WiretapColors.StatusGray
}

private fun formatPhaseDuration(ms: Double): String = when {
    ms < 0.1 -> "<0.1 ms"
    ms < 10 -> "${formatOneDecimal(ms)} ms"
    ms < 1000 -> "${ms.toLong()} ms"
    else -> "${formatOneDecimal(ms / 1000.0)} s"
}

private fun formatOneDecimal(value: Double): String {
    val int = value.toLong()
    val frac = ((value - int) * 10).toLong()
    return "$int.$frac"
}
