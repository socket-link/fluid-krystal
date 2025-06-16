package link.socket.krystal

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

enum class GradientType {
    HORIZONTAL, VERTICAL, RADIAL;
}

sealed class KrystalStyle(
    open val cornerRadius: Dp,
    open val blurRadius: Dp,
    open val backgroundOpacity: Float,
    open val tintColor: Color,
    open val borderColor: Color,
    open val borderThickness: Dp,
    open val noiseFactor: Float,
    val gradientType: GradientType,
) {
    data class Container(
        override val cornerRadius: Dp = Dp.Unspecified,
        override val blurRadius: Dp = 256.dp,
        override val backgroundOpacity: Float = 0.8f,
        override val tintColor: Color = Color.DarkGray.copy(alpha = 0.75f),
        override val borderColor: Color = Color.Unspecified,
        override val borderThickness: Dp = Dp.Unspecified,
        override val noiseFactor: Float = .3f,
    ) : KrystalStyle(
        cornerRadius = cornerRadius,
        blurRadius = blurRadius,
        backgroundOpacity = backgroundOpacity,
        tintColor = tintColor,
        borderColor = borderColor,
        borderThickness = borderThickness,
        noiseFactor = noiseFactor,
        gradientType = GradientType.HORIZONTAL,
    )

    data class Surface(
        override val cornerRadius: Dp = 64.dp,
        override val blurRadius: Dp = 80.dp,
        override val backgroundOpacity: Float = 0.5f,
        override val tintColor: Color = Color.White.copy(alpha = 0.4f),
        override val borderColor: Color = Color.White.copy(alpha = 0.7f),
        override val borderThickness: Dp = 1.5.dp,
        override val noiseFactor: Float = 0.15f,
    ) : KrystalStyle(
        cornerRadius = cornerRadius,
        blurRadius = blurRadius,
        backgroundOpacity = backgroundOpacity,
        tintColor = tintColor,
        borderColor = borderColor,
        borderThickness = borderThickness,
        noiseFactor = noiseFactor,
        gradientType = GradientType.RADIAL,
    )
}

val KrystalStyle.hazeStyle: HazeStyle
    get() = HazeStyle(
        backgroundColor = tintColor.copy(alpha = 0.2f),
        tints = listOf(
            HazeTint(
                brush = createGlassGradient(this),
            )
        ),
        blurRadius = blurRadius,
        noiseFactor = noiseFactor,
        fallbackTint = HazeTint(
            brush = createGlassGradient(this),
        ),
    )

private fun createGlassGradient(style: KrystalStyle): Brush {
    val baseColor = style.tintColor.copy(alpha = style.backgroundOpacity)

    val reflectionLayer = style.tintColor.copy(alpha = 0.1f)
    val refractionLayer = style.tintColor.copy(alpha = 0.3f)

    return when (style.gradientType) {
        GradientType.HORIZONTAL -> {
            Brush.horizontalGradient(
                colors = listOf(
                    reflectionLayer,
                    baseColor,
                    refractionLayer,
                    baseColor.copy(alpha = style.backgroundOpacity * 0.2f)
                )
            )
        }
        GradientType.VERTICAL -> {
            Brush.verticalGradient(
                colors = listOf(
                    reflectionLayer,
                    baseColor,
                    refractionLayer,
                    baseColor.copy(alpha = style.backgroundOpacity * 0.2f)
                )
            )
        }
        GradientType.RADIAL -> {
            Brush.radialGradient(
                colors = listOf(
                    reflectionLayer,
                    baseColor,
                    refractionLayer,
                    baseColor.copy(alpha = style.backgroundOpacity * 0.15f)
                )
            )
        }
    }
}
