package link.socket.krystal.api

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GlassStyle(
    val tint: GlassTint = GlassTint.Clear,
    val blurRadius: Dp = 24.dp,
    val opacity: Float = 0.12f,
    val elevation: GlassElevation = GlassElevation.Flat,
    val cornerRadius: Dp = 16.dp,
) {
    val effectiveBlurRadius: Dp get() = blurRadius * elevation.blurBoost
    val effectiveBackgroundColor: Color get() = tint.tintColor.copy(
        alpha = opacity + elevation.backgroundAlpha,
    )

    companion object {
        fun clear(
            blurRadius: Dp = 24.dp,
            elevation: GlassElevation = GlassElevation.Flat,
        ) = GlassStyle(
            tint = GlassTint.Clear,
            blurRadius = blurRadius,
            elevation = elevation,
        )

        fun tinted(
            tint: GlassTint,
            blurRadius: Dp = 24.dp,
            elevation: GlassElevation = GlassElevation.Flat,
        ) = GlassStyle(
            tint = tint,
            blurRadius = blurRadius,
            elevation = elevation,
        )

        fun socketTab(
            tint: GlassTint = GlassTint.Clear,
        ) = GlassStyle(
            tint = tint,
            blurRadius = 32.dp,
            opacity = 0.15f,
            elevation = GlassElevation.Raised,
            cornerRadius = 0.dp,
        )
    }
}
