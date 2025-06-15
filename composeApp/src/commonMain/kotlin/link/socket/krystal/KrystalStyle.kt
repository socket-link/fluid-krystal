package link.socket.krystal

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

data class KrystalStyle(
    val cornerRadius: Dp = 64.dp,
    val blurRadius: Dp = 80.dp,
    val backgroundOpacity: Float = 0.2f,
    val tintColor: Color = Color.White.copy(alpha = 0.15f),
    val borderColor: Color = Color.White.copy(alpha = 0.5f),
    val borderThickness: Dp = 1.5.dp,
    val noiseFactor: Float = 0.75f,
)

val KrystalStyle.hazeStyle: HazeStyle
    get() = HazeStyle(
        backgroundColor = tintColor.copy(alpha = 0.05f),
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

    val reflectionLayer = style.tintColor.copy(alpha = 0.2f)
    val refractionLayer = style.tintColor.copy(alpha = 0.1f)

    return Brush.radialGradient(
        colors = listOf(
            reflectionLayer,
            baseColor,
            refractionLayer,
            baseColor.copy(alpha = style.backgroundOpacity * 0.15f)
        )
    )
}
