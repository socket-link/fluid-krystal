package link.socket.krystal.curve

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.util.trace
import link.socket.krystal.util.DirtyFields
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

internal expect fun CurveRenderEffect.drawEffect(
    drawScope: DrawScope,
    contentLayer: GraphicsLayer,
)

internal data class CurveRenderEffect(
    internal val node: CurveEffectNode,
) : CurveEffect {
    var renderEffect: RenderEffect? = null

    override fun DrawScope.drawEffect() {
        println("$TAG: drawEffect()")
        createAndDrawScaledContentLayer(node) { layer ->
            updateRenderEffectIfDirty()
            this@CurveRenderEffect.drawEffect(
                drawScope = this,
                contentLayer = layer,
            )
        }
    }

    private fun updateRenderEffectIfDirty() {
        println("$TAG: updateRenderEffectIfDirty start")
        if (renderEffect == null || node.dirtyTracker.any(DirtyFields.RenderEffectAffectingFlags)) {

            renderEffect = node.getOrCreateRenderEffect()
            println("$TAG: using render effect: $renderEffect")
        }
        println("$TAG: updateRenderEffectIfDirty end")
    }
}

internal fun CurveEffectNode.calculateInputScaleFactor(
    curveIntensity: Float = resolveCurveIntensity(),
): Float = when (val s = inputScale) {
    CurveInputScale.None -> 1f
    is CurveInputScale.Fixed -> s.scale
    CurveInputScale.Auto -> {
        when {
            curveIntensity < 10f -> 1f
            else -> 0.5f
        }
    }
}

internal fun CurveEffectNode.getOrCreateRenderEffect(
    curveIntensity: Float = resolveCurveIntensity(),
    mask: Brush? = this.mask,
): RenderEffect? =
    trace("CurveEffectNode-getOrCreateRenderEffect") {
        val params =  RenderEffectParams(
            curveIntensity = curveIntensity,
            scaleFactor = calculateInputScaleFactor(curveIntensity),
            contentSize = size,
            contentOffset = layerOffset,
            mask = mask,
        )
        println("$TAG: getOrCreateRenderEffect, params=$params")
        getOrCreateRenderEffect(params)
    }

internal fun CompositionLocalConsumerModifierNode.getOrCreateRenderEffect(
    params: RenderEffectParams,
): RenderEffect? {
    val cached = renderEffectCache[params]
    if (cached != null) {
        println("$TAG: getOrCreateRenderEffect, found cached effect=$cached")
        return cached
    }

    println("$TAG: getOrCreateRenderEffect, creating new effect for params=$params")
    return createRenderEffect(params)?.also { renderEffectCache[params] = it }
}

internal fun DrawScope.createAndDrawScaledContentLayer(
    node: CurveEffectNode,
    scaleFactor: Float = node.calculateInputScaleFactor(),
    clip: Boolean = true,
    releaseLayerOnExit: Boolean = true,
    block: DrawScope.(GraphicsLayer) -> Unit,
) {
    val graphicsContext = try {
        node.currentValueOf(LocalGraphicsContext)
    } catch (e: Exception) {
        println("$TAG: Failed to get graphics context: ${e.message}")
        return
    }

    val layer = createScaledContentLayer(
        node = node,
        scaleFactor = scaleFactor,
        layerSize = node.layerSize,
        layerOffset = node.layerOffset,
    )

    if (layer != null) {
        layer.clip = clip

        drawScaledContent(
            offset = -node.layerOffset,
            scaledSize = size * scaleFactor,
            clip = clip,
        ) {
            block(layer)
        }

        if (releaseLayerOnExit) {
            graphicsContext.releaseGraphicsLayer(layer)
        }
    }
}

private fun DrawScope.createScaledContentLayer(
    node: CurveEffectNode,
    scaleFactor: Float,
    layerSize: Size,
    layerOffset: Offset,
): GraphicsLayer? {
    val scaledLayerSize = (layerSize * scaleFactor).roundToIntSize()

    if (scaledLayerSize.width <= 0f || scaledLayerSize.height <= 0f) {
        return null
    }

    val graphicsContext = node.currentValueOf(LocalGraphicsContext)
    val layer = graphicsContext.createGraphicsLayer()

    layer.record(scaledLayerSize) {
        val offset = layerOffset - node.positionOnScreen
        println("$TAG: createContentLayer, recording layer using offset=$offset")

        scale(scale = scaleFactor, pivot = Offset.Zero) {
            translate(offset) {
                for (area in node.areas) {
                    if (area.contentDrawing) {
                        continue
                    }
                    require(!area.contentDrawing) {
                        "Modifier.curveSource nodes can not draw Modifier.curve nodes."
                    }

                    val position = Snapshot.withoutReadObservation {
                        area.positionOnScreen.orZero
                    }

                    translate(position) {
                        val areaLayer = area.contentLayer
                            ?.takeUnless { it.isReleased }
                            ?.takeUnless { it.size.width <= 0f || it.size.height <= 0f }

                        println("$TAG: createContentLayer, translated areaLayer for area=${area.key}")

                        if (areaLayer != null) {
                            println("$TAG: createContentLayer, drawing areaLayer for area=${area.key}")
                            drawLayer(areaLayer)
                        }
                    }
                }
            }
        }
    }

    return layer
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
