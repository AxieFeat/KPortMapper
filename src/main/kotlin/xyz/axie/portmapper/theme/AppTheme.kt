package xyz.axie.portmapper.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppTheme {
    val backgroundColor = Color(0xFF25272a)
    val surfaceColor = Color(0xFF191a1c)

    val accentColor = Color(0xFF00ff00)
    val whiteColor = Color(0xFFF2F2F2)
    val textColor = Color(0xFFF2F2F2)

    val colorScheme = lightColors(
        surface = backgroundColor,
        onSurface = whiteColor,
    )
}

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = AppTheme.colorScheme,
        content = content
    )
}