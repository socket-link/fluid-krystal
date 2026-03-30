package link.socket.krystal.api

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GlassElevation(
    val blurBoost: Float,
    val shadowElevation: Dp,
    val backgroundAlpha: Float,
) {
    Raised(
        blurBoost = 1.2f,
        shadowElevation = 8.dp,
        backgroundAlpha = 0.08f,
    ),
    Flat(
        blurBoost = 1.0f,
        shadowElevation = 0.dp,
        backgroundAlpha = 0.12f,
    ),
    Inset(
        blurBoost = 0.8f,
        shadowElevation = 0.dp,
        backgroundAlpha = 0.18f,
    ),
}
