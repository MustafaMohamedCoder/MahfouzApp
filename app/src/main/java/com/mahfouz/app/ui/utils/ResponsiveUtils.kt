package com.mahfouz.app.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthSize {
    COMPACT,   // Regular phones (< 600dp)
    MEDIUM,    // Foldables and small tablets (600dp - 840dp)
    EXPANDED   // Full tablets and desktop (> 840dp)
}

@Composable
fun rememberWindowWidthSize(): WindowWidthSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    return when {
        screenWidth < 600 -> WindowWidthSize.COMPACT
        screenWidth < 840 -> WindowWidthSize.MEDIUM
        else -> WindowWidthSize.EXPANDED
    }
}

@Composable
fun isTabletOrLandscape(): Boolean {
    val windowSize = rememberWindowWidthSize()
    return windowSize != WindowWidthSize.COMPACT
}

@Composable
fun responsivePadding(): Dp {
    val windowSize = rememberWindowWidthSize()
    return when (windowSize) {
        WindowWidthSize.COMPACT -> 16.dp
        WindowWidthSize.MEDIUM -> 24.dp
        WindowWidthSize.EXPANDED -> 36.dp
    }
}
