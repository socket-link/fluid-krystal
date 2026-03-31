package link.socket.krystal.curve

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.util.lerp
import link.socket.krystal.ElevationLevel
import link.socket.krystal.KrystalTokens
import link.socket.krystal.LocalKrystalTokens
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.ImageFilter.Companion.makeRuntimeShader
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Shader

private const val TAG = "CurveRenderEffect.skiko"

private object EffectShaderFactory {
    private var curveInitialized = false
    private var curveShaderEffect: org.jetbrains.skia.RuntimeEffect? = null
    private var compositeInitialized = false
    private var compositeShaderEffect: org.jetbrains.skia.RuntimeEffect? = null

    fun getCompositeShaderEffect(): org.jetbrains.skia.RuntimeEffect? {
        if (!compositeInitialized) {
            compositeInitialized = true
            try {
                compositeShaderEffect = COMPOSITE_SHADER_EFFECT
            } catch (e: Exception) {
                println("Failed to create composite shader: ${e.message}")
            }
        }
        return compositeShaderEffect
    }

    fun getCurveShaderEffect(): org.jetbrains.skia.RuntimeEffect? {
        if (!curveInitialized) {
            curveInitialized = true
            try {
                curveShaderEffect = CURVE_SHADER_EFFECT
            } catch (e: Exception) {
                println("Failed to create curve shader: ${e.message}")
            }
        }
        return curveShaderEffect
    }
}

internal actual fun CompositionLocalConsumerModifierNode.createRenderEffect(
    params: RenderEffectParams,
): RenderEffect? {
    val node = this as? CurveEffectNode ?: return null

    val tokens: KrystalTokens = currentValueOf(LocalKrystalTokens)
    val spec = tokens.elevations.getValue(ElevationLevel.L3)

    val compositeShaderEffect = EffectShaderFactory.getCompositeShaderEffect() ?: return null

    val size = ceil(params.contentSize * params.scaleFactor)
    val offset = (params.contentOffset * params.scaleFactor).round().toOffset()

    val curveImageFilter = createCurveImageFilterWithMask(
        curveIntensity = spec.blurRadius.value,
        size = size,
        offset = offset,
        maskShader = createMaskShader(size)
    ) ?: return null

    val noiseImageFilter = ImageFilter.makeShader(
        shader = NOISE_SHADER,
        crop = null,
    )

    val compositeShaderBuilder = RuntimeShaderBuilder(compositeShaderEffect).apply {
        uniform("glassTint", tokens.colors.glassTint)
        uniform("overlayTint", tokens.colors.overlayTint)
        uniform("vibrancyTint", tokens.colors.vibrancyTint)
        uniform("noiseFactor", spec.noiseFactor)
        uniform("saturation", spec.saturation)
    }

    return makeRuntimeShader(
        runtimeShaderBuilder = compositeShaderBuilder,
        shaderNames = arrayOf("content", "curve", "noise"),
        inputs = arrayOf(null, curveImageFilter, noiseImageFilter),
    )
        .withMask(params.mask, size, offset)
        .asComposeRenderEffect()
}

private fun createMaskShader(
    size: Size,
): Shader {
    return Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0f),
            Color.White.copy(alpha = 1f),
        ),
        center = Offset(size.width / 2, size.height / 2),
        radius = minOf(size.width, size.height) / 2
    ).toShader(size)
}

private fun createCurveImageFilterWithMask(
    curveIntensity: Float,
    size: Size,
    offset: Offset,
    maskShader: Shader,
): ImageFilter? {
    val curveShaderEffect = EffectShaderFactory.getCurveShaderEffect() ?: return null

    val displacementShader = createDisplacementShader(size)

    val curveShaderBuilder = RuntimeShaderBuilder(curveShaderEffect).apply {
        uniform("curveIntensity", curveIntensity)
        uniform("crop", offset.x, offset.y, offset.x + size.width, offset.y + size.height)
        child("mask", maskShader)
        child("displacement", displacementShader)
    }

    return makeRuntimeShader(
        runtimeShaderBuilder = curveShaderBuilder,
        shaderNames = arrayOf("content"),
        inputs = arrayOf(null),
    )
}

private fun createDisplacementShader(
    size: Size,
): Shader {
    return createSphericalDisplacementBrush().toShader(size)
}

private fun createSphericalDisplacementBrush(): Brush {
    return object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            return radialGradient(
                colors = listOf(
                    Color(red = 0.5f, green = 0.5f, blue = 0.5f, alpha = 1.0f),
                    Color(red = 0.8f, green = 0.2f, blue = 0.5f, alpha = 1.0f),
                ),
                center = Offset(size.width / 2, size.height / 2),
                radius = minOf(size.width, size.height),
            ).toShader(size)
        }
    }
}

private fun RuntimeShaderBuilder.uniform(name: String, color: Color) {
    uniform(name, color.red, color.green, color.blue, color.alpha)
}

private fun ImageFilter.blendWith(
    foreground: ImageFilter,
    blendMode: BlendMode,
    offset: Offset,
): ImageFilter = ImageFilter.makeBlend(
    blendMode = blendMode,
    fg = when {
        offset.isUnspecified -> foreground
        offset == Offset.Zero -> foreground
        else -> ImageFilter.makeOffset(offset.x, offset.y, foreground, crop = null)
    },
    bg = this,
    crop = null,
)

private fun ImageFilter.withMask(
    brush: Brush?,
    size: Size,
    offset: Offset,
    blendMode: BlendMode = BlendMode.DST_IN,
): ImageFilter {
    val shader = brush?.toShader(size) ?: return this
    return blendWith(
        foreground = ImageFilter.makeShader(shader, crop = null),
        blendMode = blendMode,
        offset = offset,
    )
}

private fun Brush.toShader(size: Size): Shader =
    (this as ShaderBrush).createShader(size)
