package dev.skymansandy.wiretap.ui.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

internal val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState?> { null }
