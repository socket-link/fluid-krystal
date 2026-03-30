package link.socket.krystal.api

import androidx.compose.ui.graphics.Color

sealed class GlassTint(
    val color: Color,
    val alpha: Float,
) {
    data object Clear : GlassTint(
        color = Color.White,
        alpha = 0.05f,
    )

    data object ElectricPurple : GlassTint(
        color = Color(0xFF7B2FBE),
        alpha = 0.15f,
    )

    data object CerebralBlue : GlassTint(
        color = Color(0xFF3B82F6),
        alpha = 0.15f,
    )

    data object SignalAmber : GlassTint(
        color = Color(0xFFF59E0B),
        alpha = 0.15f,
    )

    data class Custom(
        val customColor: Color,
        val customAlpha: Float = 0.15f,
    ) : GlassTint(
        color = customColor,
        alpha = customAlpha,
    )

    val tintColor: Color get() = color.copy(alpha = alpha)
}
