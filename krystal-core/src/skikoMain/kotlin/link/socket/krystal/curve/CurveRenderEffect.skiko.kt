package link.socket.krystal.curve

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.Easing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.util.lerp
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.ImageFilter.Companion.makeRuntimeShader
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Shader

private const val TAG = "CurveRenderEffect.skiko"

// TODO: Use this
class RadialGradient(
    val easing: Easing = EaseIn,
    val center: Offset = Offset.Unspecified,
    val centerIntensity: Float = 0f,
    val radius: Float = Float.POSITIVE_INFINITY,
    val radiusIntensity: Float = 1f,
) {
    fun toBrush(
        numStops: Int = 20,
    ): Brush = Brush.radialGradient(
        colors = List(numStops) { i ->
            val x = i * 1f / (numStops - 1)
            Color.Magenta.copy(
                alpha = lerp(
                    centerIntensity,
                    radiusIntensity,
                    easing.transform(x),
                ),
            )
        },
        center = center,
        radius = radius,
    )
}

private object EffectShaderFactory {
    private var curveInitialized = false
    private var curveShaderEffect: RuntimeEffect? = null
    private var compositeInitialized = false
    private var compositeShaderEffect: RuntimeEffect? = null

    fun getCompositeShaderEffect(): RuntimeEffect? {
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

    fun getCurveShaderEffect(): RuntimeEffect? {
        if (!curveInitialized) {
            println("$TAG: getCurveShaderEffect, creating new curve shader effect")
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
    println("$TAG: createRenderEffect called with params: $params")
    
    val compositeShaderEffect = EffectShaderFactory.getCompositeShaderEffect() ?: return null
    println("$TAG: compositeShaderEffect created successfully")

    val size = ceil(params.contentSize * params.scaleFactor)
    val offset = (params.contentOffset * params.scaleFactor).round().toOffset()
    
    println("$TAG: size=$size, offset=$offset")

    // Use your custom curve shader instead of makeDisplacementMap
    val curveImageFilter = createCurveImageFilterWithMask(
        curveIntensity = 100f,
        size = size,
        offset = offset,
        maskShader = createMaskShader(size)
    )
    
    if (curveImageFilter == null) {
        println("$TAG: curveImageFilter is null!")
        return null
    }
    
    println("$TAG: curveImageFilter created successfully")

    val noiseImageFilter = ImageFilter.makeShader(
        shader = NOISE_SHADER,
        crop = null,
    )
    
    println("$TAG: noiseImageFilter created successfully")

    val compositeShaderBuilder = RuntimeShaderBuilder(compositeShaderEffect)
    
    val result = makeRuntimeShader(
        runtimeShaderBuilder = compositeShaderBuilder,
        shaderNames = arrayOf("content", "curve", "noise"),
        inputs = arrayOf(null, curveImageFilter, noiseImageFilter),
    )
        .withMask(params.mask, size, offset)
        .asComposeRenderEffect()
    
    println("$TAG: final render effect created: $result")
    
    return result
}

internal actual fun CurveRenderEffect.drawEffect(
    drawScope: DrawScope,
    contentLayer: GraphicsLayer,
) = with(drawScope) {
    println("$TAG: drawEffect")
    contentLayer.renderEffect = node.getOrCreateRenderEffect()
    contentLayer.alpha = node.alpha
    drawLayer(contentLayer)
}

private fun createMaskShader(
    size: Size,
): Shader {
    return Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0f), // Center: no effect
            Color.White.copy(alpha = 1f), // Edges: full effect
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
    println("$TAG: createCurveImageFilterWithMask called with intensity=$curveIntensity, size=$size, offset=$offset")
    
    val curveShaderEffect = EffectShaderFactory.getCurveShaderEffect()
    if (curveShaderEffect == null) {
        println("$TAG: curveShaderEffect is null!")
        return null
    }
    
    val displacementShader = createDisplacementShader(size)
    println("$TAG: displacementShader created: $displacementShader")

    val curveShaderBuilder = RuntimeShaderBuilder(curveShaderEffect).apply {
        uniform("curveIntensity", 200f) // Much higher intensity
        uniform("crop", offset.x, offset.y, offset.x + size.width, offset.y + size.height)
        child("mask", maskShader)
        child("displacement", displacementShader)
    }
    
    println("$TAG: curveShaderBuilder configured")

    val curveImageFilter = makeRuntimeShader(
        runtimeShaderBuilder = curveShaderBuilder,
        shaderNames = arrayOf("content"),
        inputs = arrayOf(null),
    )
    
    println("$TAG: curveImageFilter created: $curveImageFilter")

    return curveImageFilter
}

private fun createDisplacementShader(
    size: Size,
): Shader {
    val displacementShader = createSphericalDisplacementBrush().toShader(size)
    return displacementShader
}

private fun createSphericalDisplacementBrush(): Brush {
    return object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            return radialGradient(
                colors = listOf(
                    Color(red = 0.5f, green = 0.5f, blue = 0.5f, alpha = 1.0f), // Center: no displacement
                    Color(red = 0.8f, green = 0.2f, blue = 0.5f, alpha = 1.0f), // Edges: displacement
                ),
                center = Offset(size.width / 2, size.height / 2),
                radius = minOf(size.width, size.height),
            ).toShader(size)
        }
    }
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
