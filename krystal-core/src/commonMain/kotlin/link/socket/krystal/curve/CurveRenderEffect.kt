package link.socket.krystal.curve

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.util.trace
import link.socket.krystal.util.SimpleLruCache
import kotlin.math.max

private const val TAG = "CurveRenderEffect"

internal data class RenderEffectParams(
    val curveIntensity: Float,
    val scaleFactor: Float,
    val contentSize: Size,
    val contentOffset: Offset,
    val mask: Brush?,
)

private val renderEffectCache = SimpleLruCache<RenderEffectParams, RenderEffect>(10)

internal expect fun CompositionLocalConsumerModifierNode.createRenderEffect(
    params: RenderEffectParams,
): RenderEffect?

internal fun CurveEffectNode.getOrCreateRenderEffect(
    curveIntensity: Float = resolveCurveIntensity(),
    mask: Brush? = this.mask,
): RenderEffect? =
    trace("CurveEffectNode-getOrCreateRenderEffect") {
        val params = RenderEffectParams(
            curveIntensity = curveIntensity,
            scaleFactor = calculateInputScaleFactor(curveIntensity),
            contentSize = Size.Unspecified,
            contentOffset = Offset.Zero,
            mask = mask,
        )
        getOrCreateRenderEffect(params)
    }

internal fun CompositionLocalConsumerModifierNode.getOrCreateRenderEffect(
    params: RenderEffectParams,
): RenderEffect? {
    val cached = renderEffectCache[params]
    if (cached != null) return cached

    return createRenderEffect(params)?.also { renderEffectCache[params] = it }
}

internal fun DrawScope.drawScaledContent(
    offset: Offset,
    scaledSize: Size,
    clip: Boolean = true,
    block: DrawScope.() -> Unit,
) {
    val scaleFactor = max(size.width / scaledSize.width, size.height / scaledSize.height)
    optionalClipRect(enabled = clip) {
        translate(offset) {
            scale(scale = scaleFactor, pivot = Offset.Zero) {
                block()
            }
        }
    }
}

private inline fun DrawScope.optionalClipRect(
    enabled: Boolean,
    left: Float = 0.0f,
    top: Float = 0.0f,
    right: Float = size.width,
    bottom: Float = size.height,
    clipOp: ClipOp = ClipOp.Intersect,
    block: DrawScope.() -> Unit,
) = withTransform(
    transformBlock = {
        if (enabled) {
            clipRect(left, top, right, bottom, clipOp)
        }
    },
    drawBlock = block,
)
