package link.socket.krystal

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

@Composable
fun baseKrystalContainerStyle() = with(MaterialTheme.colorScheme) {
    KrystalStyle.Container(
        cornerRadius = Dp.Unspecified,
        blurRadius = 24.dp,
        backgroundOpacity = 1f,
        backgroundColor = background,
        borderColor = Color.Unspecified,
        borderThickness = Dp.Unspecified,
        noiseFactor = 0.8f,
    )
}

@Composable
fun baseKrystalSurfaceStyle() = with(MaterialTheme.colorScheme) {
    KrystalStyle.Surface(
        cornerRadius = 64.dp,
        blurRadius = 24.dp,
        backgroundOpacity = 0.1f,
        backgroundColor = surface.copy(alpha = 0.1f),
        borderColor = Color.White,
        borderThickness = 0.8.dp,
        noiseFactor = 0.2f,
    )
}

enum class GradientType {
    HORIZONTAL, VERTICAL, RADIAL;
}

sealed class KrystalStyle(
    open val cornerRadius: Dp,
    open val blurRadius: Dp,
    open val backgroundOpacity: Float,
    open val backgroundColor: Color,
    open val borderColor: Color,
    open val borderThickness: Dp,
    open val noiseFactor: Float,
    val gradientType: GradientType,
) {
    data class Container(
        override val cornerRadius: Dp,
        override val blurRadius: Dp,
        override val backgroundOpacity: Float,
        override val backgroundColor: Color,
        override val borderColor: Color,
        override val borderThickness: Dp,
        override val noiseFactor: Float,
    ) : KrystalStyle(
        cornerRadius = cornerRadius,
        blurRadius = blurRadius,
        backgroundOpacity = backgroundOpacity,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderThickness = borderThickness,
        noiseFactor = noiseFactor,
        gradientType = GradientType.VERTICAL,
    ) {
        companion object {
            val EMPTY = Container(
                cornerRadius = 0.dp,
                blurRadius = 0.dp,
                backgroundOpacity = 0f,
                backgroundColor = Color.Transparent,
                borderColor = Color.Transparent,
                borderThickness = 0.dp,
                noiseFactor = 0f,
            )
        }
    }

    data class Surface(
        override val cornerRadius: Dp,
        override val blurRadius: Dp,
        override val backgroundOpacity: Float,
        override val backgroundColor: Color,
        override val borderColor: Color,
        override val borderThickness: Dp,
        override val noiseFactor: Float,
    ) : KrystalStyle(
        cornerRadius = cornerRadius,
        blurRadius = blurRadius,
        backgroundOpacity = backgroundOpacity,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderThickness = borderThickness,
        noiseFactor = noiseFactor,
        gradientType = GradientType.RADIAL,
    ) {
        companion object {
            val EMPTY = Surface(
                cornerRadius = 0.dp,
                blurRadius = 0.dp,
                backgroundOpacity = 0f,
                backgroundColor = Color.Transparent,
                borderColor = Color.Transparent,
                borderThickness = 0.dp,
                noiseFactor = 0f,
            )
        }
    }
}

val KrystalStyle.containerHazeStyle: HazeStyle
    get() = HazeStyle(
        backgroundColor = backgroundColor.copy(alpha = backgroundOpacity),
        tints = listOf(
            HazeTint(
                brush = createContainerGlassGradient(this),
            )
        ),
        blurRadius = blurRadius,
        noiseFactor = noiseFactor,
        fallbackTint = HazeTint(
            brush = createContainerGlassGradient(this),
        ),
    )

val KrystalStyle.surfaceHazeStyle: HazeStyle
    get() = HazeStyle(
        backgroundColor = backgroundColor.copy(alpha = backgroundOpacity),
        tints = listOf(
            HazeTint(
                brush = createSurfaceGlassGradient(this),
            )
        ),
        blurRadius = blurRadius,
        noiseFactor = noiseFactor,
        fallbackTint = HazeTint(
            brush = createSurfaceGlassGradient(this),
        ),
    )

private fun createContainerGlassGradient(
    style: KrystalStyle,
): Brush {
    val baseColor = style.backgroundColor.copy(alpha = style.backgroundOpacity)
    val reflectionLayer = baseColor.copy(alpha = 0.9f)

    // Use the style's borderColor as the overlay/refraction layer; this is mapped from tokens.overlayTint
    val refractionLayer = style.borderColor.copy(alpha = 0.5f)

    val colors = listOf(
        reflectionLayer,
        refractionLayer,
    )

    return when (style.gradientType) {
        GradientType.HORIZONTAL -> {
            Brush.horizontalGradient(colors)
        }
        GradientType.VERTICAL -> {
            Brush.verticalGradient(colors)
        }
        GradientType.RADIAL -> {
            Brush.radialGradient(colors)
        }
    }
}

private fun createSurfaceGlassGradient(
    style: KrystalStyle,
): Brush {
    val baseColor = style.backgroundColor.copy(alpha = style.backgroundOpacity)

    // Use vibrancy tint (encoded in style.borderColor) as the reflective highlight layer
    val reflectionLayer = style.borderColor.copy(alpha = 0.35f)
    val refractionLayer = baseColor.copy(alpha = 0.8f)

    val colors = listOf(
        reflectionLayer,
        refractionLayer,
    )

    return when (style.gradientType) {
        GradientType.HORIZONTAL -> {
            Brush.horizontalGradient(colors)
        }
        GradientType.VERTICAL -> {
            Brush.verticalGradient(colors)
        }
        GradientType.RADIAL -> {
            Brush.radialGradient(colors)
        }
    }
}


@Composable
fun krystalContainerStyleFor(
    level: ElevationLevel,
    tokens: KrystalTokens = LocalKrystalTokens.current,
): KrystalStyle.Container = tokens.toContainerStyle(level)

@Composable
fun krystalSurfaceStyleFor(
    level: ElevationLevel,
    tokens: KrystalTokens = LocalKrystalTokens.current,
): KrystalStyle.Surface = tokens.toSurfaceStyle(level)
