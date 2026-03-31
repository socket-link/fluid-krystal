package link.socket.krystal.curve

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.node.requireGraphicsContext
import link.socket.krystal.util.Bitmask
import link.socket.krystal.util.DirtyFields

private const val TAG = "CurveEffectNode"

internal data class CurveEffectNode(
    private var curveState: CurveState,
    private val styleParameter: CurveStyle = CurveStyle.Unspecified,
) : CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode,
    ObserverModifierNode,
    DelegatingNode() {

    override val shouldAutoInvalidate: Boolean = false

    var dirtyTracker = Bitmask()

    private var _position: Offset? = null
    private var position: Offset by mutableStateOf(Offset.Zero)
    private var graphicsLayer: GraphicsLayer? = null

    var curveStyle: CurveStyle = styleParameter

    private var _renderEffect: RenderEffect? = null

    private var _size: Size = Size.Unspecified

    internal var curveEnabledSet: Boolean = false
    var curveEnabled: Boolean = resolveCurveEnabled()
        set(value) {
            if (value != field) {
                field = value
                dirtyTracker += DirtyFields.CurveEnabled
            }
            curveEnabledSet = true
        }

    var inputScale: CurveInputScale = CurveInputScale.Default
        set(value) {
            if (value != field) {
                field = value
                dirtyTracker += DirtyFields.InputScale
            }
        }

    internal var compositionLocalStyle: CurveStyle = CurveStyle.Unspecified
        set(value) {
            if (value != field) {
                onStyleChanged(field, value)
                field = value
            }
        }

    var mask: Brush? = null
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.Mask
                field = value
            }
        }

    var curveIntensity: Float = -1f
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.CurveIntensity
                field = value
            }
        }

    var alpha: Float = 1f
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.Alpha
                field = value
            }
        }

    var drawContentBehind: Boolean = CurveDefaults.DRAW_CONTENT_BEHIND
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.DrawContentBehind
                field = value
            }
        }

    private val drawNode = delegate(
        CacheDrawModifierNode {
            val size = this.size
            if (!size.isSpecified || !resolveCurveEnabled()) {
                return@CacheDrawModifierNode onDrawWithContent { drawContent() }
            }

            val renderEffect = getOrCreateRenderEffect()
            _renderEffect = renderEffect

            graphicsLayer?.let { layer ->
                layer.renderEffect = renderEffect

                if (_position != position) {
                    _position = position
                    layer.record {
                        translate(-position.x, -position.y) {
                            drawLayer(curveState.graphicsLayer)
                        }
                    }
                }
            }

            onDrawWithContent {
                graphicsLayer?.let { layer ->
                    drawLayer(layer)
                }
                drawContent()
            }
        }
    )

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        position = if (coordinates.isAttached) {
            val statePosition = curveState.position
            coordinates.positionInWindow() - statePosition
        } else {
            Offset.Unspecified
        }
    }

    override fun onObservedReadsChanged() = observeReads {
        updateEffect()
    }

    override fun onAttach() {
        val graphicsContext = requireGraphicsContext()
        graphicsLayer = graphicsContext.createGraphicsLayer().apply {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        updateEffect()
    }

    override fun onDetach() {
        val graphicsContext = requireGraphicsContext()
        graphicsLayer?.let { graphicsContext.releaseGraphicsLayer(it) }
        graphicsLayer = null
    }

    fun update(
        newState: CurveState,
        newStyle: CurveStyle,
    ) {
        if (curveState != newState || curveStyle != newStyle) {
            curveState = newState
            curveStyle = newStyle
            updateEffect()
        }
    }

    private fun updateEffect() {
        observeReads { curveStyle = styleParameter }
        drawNode.invalidateDrawCache()
    }

    private fun onStyleChanged(old: CurveStyle?, new: CurveStyle?) {
        if (old?.curveIntensity != new?.curveIntensity) dirtyTracker += DirtyFields.CurveIntensity
    }

    internal fun resolveCurveEnabled(): Boolean {
        return when {
            curveEnabledSet -> curveEnabled
            else -> CurveDefaults.curveEnabled()
        }
    }
}

internal fun CurveEffectNode.resolveCurveIntensity(): Float =
    if (curveIntensity == -1f) {
        curveStyle.curveIntensity
    } else {
        curveIntensity
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
