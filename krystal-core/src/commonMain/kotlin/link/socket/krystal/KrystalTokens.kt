package link.socket.krystal

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ColorTokens(
    val glassTint: Color,
    val overlayTint: Color,
    val vibrancyTint: Color,
)

@Immutable
enum class ElevationLevel { L1, L2, L3, L4, L5, L6 }

@Immutable
data class ElevationSpec(
    val blurRadius: Dp,
    val saturation: Float, // 1.0 = no change, >1 increases saturation
    val opacity: Float, // background tint opacity used for glass backdrop
    val cornerRadius: Dp,
    val shadowElevation: Dp,
    val borderThickness: Dp,
    val noiseFactor: Float,
)

@Immutable
enum class InteractionState { Normal, Hover, Pressed }

@Immutable
data class MotionSpec(
    val scale: Float = 1f, // 1.0 = no scale change
    val opacityDelta: Float = 0f, // added to base opacity
    val blurDelta: Dp = 0.dp, // added to base blur
    val durationMillis: Int = 160,
    val easing: String = "cubic-bezier(0.2, 0.0, 0.2, 1.0)",
)

@Immutable
data class KrystalTokens(
    val colors: ColorTokens,
    val elevations: Map<ElevationLevel, ElevationSpec>,
    val motions: Map<InteractionState, MotionSpec>,
) {
    companion object Defaults {
        val DefaultLight = KrystalTokens(
            colors = ColorTokens(
                glassTint = Color.White.copy(alpha = 0.18f),
                overlayTint = Color.White.copy(alpha = 0.06f),
                vibrancyTint = Color(0xFF007AFF).copy(alpha = 0.28f),
            ),
            elevations = mapOf(
                ElevationLevel.L1 to ElevationSpec(
                    blurRadius = 12.dp, saturation = 1.03f, opacity = 0.06f,
                    cornerRadius = 8.dp, shadowElevation = 1.dp, borderThickness = 0.5.dp, noiseFactor = 0.10f
                ),
                ElevationLevel.L2 to ElevationSpec(
                    blurRadius = 16.dp, saturation = 1.06f, opacity = 0.08f,
                    cornerRadius = 12.dp, shadowElevation = 2.dp, borderThickness = 0.6.dp, noiseFactor = 0.12f
                ),
                ElevationLevel.L3 to ElevationSpec(
                    blurRadius = 20.dp, saturation = 1.09f, opacity = 0.10f,
                    cornerRadius = 16.dp, shadowElevation = 4.dp, borderThickness = 0.8.dp, noiseFactor = 0.14f
                ),
                ElevationLevel.L4 to ElevationSpec(
                    blurRadius = 24.dp, saturation = 1.12f, opacity = 0.12f,
                    cornerRadius = 20.dp, shadowElevation = 6.dp, borderThickness = 1.0.dp, noiseFactor = 0.16f
                ),
                ElevationLevel.L5 to ElevationSpec(
                    blurRadius = 30.dp, saturation = 1.18f, opacity = 0.14f,
                    cornerRadius = 28.dp, shadowElevation = 8.dp, borderThickness = 1.2.dp, noiseFactor = 0.18f
                ),
                ElevationLevel.L6 to ElevationSpec(
                    blurRadius = 40.dp, saturation = 1.24f, opacity = 0.16f,
                    cornerRadius = 36.dp, shadowElevation = 12.dp, borderThickness = 1.4.dp, noiseFactor = 0.20f
                ),
            ),
            motions = mapOf(
                InteractionState.Normal to MotionSpec(scale = 1.0f, opacityDelta = 0f, blurDelta = 0.dp, durationMillis = 180),
                InteractionState.Hover to MotionSpec(scale = 1.01f, opacityDelta = 0.02f, blurDelta = (-0.5f).dp, durationMillis = 200,
                    easing = "cubic-bezier(0.3, 0.0, 0.1, 1.0)"),
                InteractionState.Pressed to MotionSpec(scale = 0.985f, opacityDelta = -0.02f, blurDelta = 1.dp, durationMillis = 140,
                    easing = "cubic-bezier(0.2, 0.0, 0.0, 1.0)"),
            )
        )

        val DefaultDark = KrystalTokens(
            colors = ColorTokens(
                glassTint = Color.Black.copy(alpha = 0.22f),
                overlayTint = Color.Black.copy(alpha = 0.08f),
                vibrancyTint = Color(0xFF0A84FF).copy(alpha = 0.32f),
            ),
            elevations = mapOf(
                ElevationLevel.L1 to ElevationSpec(
                    blurRadius = 12.dp, saturation = 1.02f, opacity = 0.08f,
                    cornerRadius = 8.dp, shadowElevation = 1.dp, borderThickness = 0.5.dp, noiseFactor = 0.10f
                ),
                ElevationLevel.L2 to ElevationSpec(
                    blurRadius = 16.dp, saturation = 1.05f, opacity = 0.10f,
                    cornerRadius = 12.dp, shadowElevation = 2.dp, borderThickness = 0.6.dp, noiseFactor = 0.12f
                ),
                ElevationLevel.L3 to ElevationSpec(
                    blurRadius = 20.dp, saturation = 1.08f, opacity = 0.12f,
                    cornerRadius = 16.dp, shadowElevation = 4.dp, borderThickness = 0.8.dp, noiseFactor = 0.14f
                ),
                ElevationLevel.L4 to ElevationSpec(
                    blurRadius = 24.dp, saturation = 1.12f, opacity = 0.14f,
                    cornerRadius = 20.dp, shadowElevation = 6.dp, borderThickness = 1.0.dp, noiseFactor = 0.16f
                ),
                ElevationLevel.L5 to ElevationSpec(
                    blurRadius = 30.dp, saturation = 1.18f, opacity = 0.16f,
                    cornerRadius = 28.dp, shadowElevation = 8.dp, borderThickness = 1.2.dp, noiseFactor = 0.18f
                ),
                ElevationLevel.L6 to ElevationSpec(
                    blurRadius = 40.dp, saturation = 1.24f, opacity = 0.18f,
                    cornerRadius = 36.dp, shadowElevation = 12.dp, borderThickness = 1.4.dp, noiseFactor = 0.20f
                ),
            ),
            motions = mapOf(
                InteractionState.Normal to MotionSpec(scale = 1.0f, opacityDelta = 0f, blurDelta = 0.dp, durationMillis = 180),
                InteractionState.Hover to MotionSpec(scale = 1.01f, opacityDelta = 0.02f, blurDelta = (-0.5f).dp, durationMillis = 200,
                    easing = "cubic-bezier(0.3, 0.0, 0.1, 1.0)"),
                InteractionState.Pressed to MotionSpec(scale = 0.985f, opacityDelta = -0.02f, blurDelta = 1.dp, durationMillis = 140,
                    easing = "cubic-bezier(0.2, 0.0, 0.0, 1.0)"),
            )
        )
    }
}


val LocalKrystalTokens = staticCompositionLocalOf { KrystalTokens.DefaultLight }

fun KrystalTokens.toContainerStyle(level: ElevationLevel): KrystalStyle.Container {
    val spec = elevations.getValue(level)
    return KrystalStyle.Container(
        cornerRadius = spec.cornerRadius,
        blurRadius = spec.blurRadius,
        backgroundOpacity = spec.opacity,
        backgroundColor = colors.glassTint.copy(alpha = 1f),
        borderColor = colors.overlayTint,
        borderThickness = spec.borderThickness,
        noiseFactor = maxOf(spec.noiseFactor, 0.0f),
    )
}

fun KrystalTokens.toSurfaceStyle(level: ElevationLevel): KrystalStyle.Surface {
    val spec = elevations.getValue(level)
    return KrystalStyle.Surface(
        cornerRadius = spec.cornerRadius,
        blurRadius = spec.blurRadius,
        backgroundOpacity = spec.opacity,
        backgroundColor = colors.glassTint.copy(alpha = 1f),
        borderColor = colors.vibrancyTint.copy(alpha = 0.9f),
        borderThickness = spec.borderThickness,
        noiseFactor = maxOf(spec.noiseFactor, 0.0f),
    )
}
