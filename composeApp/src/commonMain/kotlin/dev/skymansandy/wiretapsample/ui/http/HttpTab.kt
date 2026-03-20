package dev.skymansandy.wiretapsample.ui.http

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretapsample.model.httpActions
import dev.skymansandy.wiretapsample.resources.Res
import dev.skymansandy.wiretapsample.resources.http_requests
import dev.skymansandy.wiretapsample.resources.status_ready
import dev.skymansandy.wiretapsample.ui.theme.actionColor
import dev.skymansandy.wiretapsample.viewmodel.HttpViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HttpTab(
    viewModel: HttpViewModel,
    modifier: Modifier = Modifier,
) {

    val statusLog by viewModel.statusLog.collectAsStateWithLifecycle()
    val readyText = stringResource(Res.string.status_ready)

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

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val isWide = maxWidth > 600.dp

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ActionButtonGrid(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        viewModel = viewModel,
                    )

                    StatusWindow(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        statusLog = statusLog.ifEmpty { readyText },
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatusWindow(
                        modifier = Modifier.weight(1f),
                        statusLog = statusLog.ifEmpty { readyText },
                    )

                    ActionButtonGrid(
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonGrid(
    viewModel: HttpViewModel,
    modifier: Modifier = Modifier,
) {

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 8.dp),
    ) {
        items(httpActions) { action ->
            val color = actionColor.getValue(action.category)
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                onClick = { viewModel.executeAction(action) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = color,
                ),
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
