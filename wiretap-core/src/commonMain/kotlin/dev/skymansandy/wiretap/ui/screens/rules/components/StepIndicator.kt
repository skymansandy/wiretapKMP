package dev.skymansandy.wiretap.ui.screens.rules.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun StepIndicator(
    modifier: Modifier = Modifier,
    currentStep: Int,
    labels: List<String>,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEachIndexed { index, label ->
            val step = index + 1
            val isActive = step == currentStep
            val isCompleted = step < currentStep

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isActive || isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(start = 6.dp),
            )

            if (index < labels.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview_StepIndicatorStep1() {
    MaterialTheme {
        StepIndicator(
            currentStep = 1,
            labels = listOf("Request", "Response", "Review"),
        )
    }
}

@Preview
@Composable
private fun Preview_StepIndicatorStep2() {
    MaterialTheme {
        StepIndicator(
            currentStep = 2,
            labels = listOf("Request", "Response", "Review"),
        )
    }
}

@Preview
@Composable
private fun Preview_StepIndicatorStep3() {
    MaterialTheme {
        StepIndicator(
            currentStep = 3,
            labels = listOf("Request", "Response", "Review"),
        )
    }
}
