package link.socket.krystal.curve

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.TraversableNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.findNearestAncestor
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.trace
import link.socket.krystal.util.Bitmask
import link.socket.krystal.util.DirtyFields

private const val TAG = "CurveEffectNode"

internal data class CurveEffectNode(
    private val curveState: CurveState,
    private val styleParameter: CurveStyle = CurveStyle.Unspecified,
    private val blockParameter: (CurveEffectScope.() -> Unit)? = null,
) : Modifier.Node(),
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode,
    LayoutAwareModifierNode,
    ObserverModifierNode,
    DrawModifierNode,
    TraversableNode,
    CurveEffectScope {

    override val traverseKey: Any
        get() = CurveTraversableNodeKeys.Effect

    override val shouldAutoInvalidate: Boolean = false

    internal var dirtyTracker = Bitmask()

    var block: (CurveEffectScope.() -> Unit)? = blockParameter
        set(value) {
            if (field != value) {
                field = value
            }
        }

    fun containsArea(area: CurveArea): Boolean = curveState.areasState.value.contains(area)

    internal val areas: List<CurveArea>
        get() = curveState.areasState.value.toList()

    fun addArea(area: CurveArea) {
        if (!containsArea(area)) {
            curveState.addArea(area)

            dirtyTracker += DirtyFields.Areas
            println("$TAG: addArea, area=$area")
        }
    }

    internal var curveEnabledSet: Boolean = false
    override var curveEnabled: Boolean = resolveCurveEnabled()
        set(value) {
            if (value != field) {
                field = value
                dirtyTracker += DirtyFields.CurveEnabled
                println("$TAG: setting curveEnabled=$value")
            }
            curveEnabledSet = true
        }

    override var inputScale: CurveInputScale = CurveInputScale.Default
        set(value) {
            if (value != field) {
                field = value
                dirtyTracker += DirtyFields.InputScale
                println("$TAG: setting inputScale=$value")
            }
        }

    internal var compositionLocalStyle: CurveStyle = CurveStyle.Unspecified
        set(value) {
            if (value != field) {
                onStyleChanged(field, value)
                field = value
                println("$TAG: setting compositionLocalStyle=$value")
            }
        }

    override var curveStyle: CurveStyle = styleParameter
        set(value) {
            if (field != value) {
                field = value
                onStyleChanged(field, value)
                println("$TAG: setting style=$value")
            }
        }

    internal var positionOnScreen: Offset = Offset.Unspecified
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.ScreenPosition
                field = value
                println("$TAG: setting positionOnScreen=$value")
            }
        }

    private var areaOffsets: Map<CurveArea, Offset> = emptyMap()
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.AreaOffsets
                field = value
                println("$TAG: setting areaOffsets=$value")
            }
        }

    internal var size: Size = Size.Unspecified
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.Size
                field = value
                println("$TAG: setting size=$value")
            }
        }

    internal var layerSize: Size = Size.Unspecified
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.LayerSize
                field = value
                println("$TAG: setting layerSize=$value")
            }
        }

    internal var layerOffset: Offset = Offset.Unspecified
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.LayerOffset
                field = value
                println("$TAG: setting layerOffset=$value")
            }
        }

    override var mask: Brush? = null
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.Mask
                field = value
                println("$TAG: setting mask=$value")
            }
        }

    override var curveIntensity: Float = -1f
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.CurveIntensity
                field = value
                println("$TAG: setting curveIntensity=$value")
            }
        }

    override var alpha: Float = 1f
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.Alpha
                field = value
                println("$TAG: setting alpha=$value")
            }
        }

    override var canDrawArea: ((CurveArea) -> Boolean)? = null
        set(value) {
            if (value != field) {
                field = value
                println("$TAG: setting canDrawArea=$value")
            }
        }

    internal var curveEffect: CurveEffect = CurveRenderEffect(this)
        set(value) {
            if (value != field) {
                field.cleanup()
                field = value
                println("$TAG: setting curveEffect=$value")
            }
        }

    override var drawContentBehind: Boolean = CurveDefaults.DRAW_CONTENT_BEHIND
        set(value) {
            if (value != field) {
                dirtyTracker += DirtyFields.DrawContentBehind
                field = value
                println("$TAG: setting drawContentBehind=$value")
            }
        }

    override fun ContentDrawScope.draw() {
        println("CurveEffectNode: draw() called, isAttached=$isAttached")

        try {
            if (!isAttached) {
                println("$TAG: draw() called, but not attached, skipping")
                return
            }

            if (size.isSpecified && layerSize.isSpecified) {
                println("$TAG: draw() called, size=$size, layerSize=$layerSize")
                if (curveState.areasState.value.isNotEmpty()) {
                    println("$TAG: draw() called, areas=$$curveState.areasState.value")
                    updateCurveEffectIfNeeded(this)
                    with(curveEffect) { drawEffect() }
                    println("$TAG: draw() called, effect drawn using curveEffect=$curveEffect")
                }
                drawContentSafely()

            } else {
                println("$TAG: draw() called, size=$size, layerSize=$layerSize, skipping due to unspecified")
                drawContentSafely()
            }
        } catch (e: Exception) {
            println("CurveEffectNode: Exception in draw(): ${e.message}")
            e.printStackTrace()
        } finally {
            dirtyTracker = Bitmask()
        }
    }

    internal fun update() {
        println("$TAG: update() called")
        onObservedReadsChanged()
    }

    override fun onAttach() {
        println("$TAG: onAttach() called")
        update()
    }

    override fun onObservedReadsChanged() = observeReads {
        println("$TAG: Observed reads changed, updating effect")
        updateEffect()
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        Snapshot.withoutReadObservation {
            if (positionOnScreen.isUnspecified) {
                onPositioned(coordinates, "onPlaced")
            }
        }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        onPositioned(coordinates, "onGloballyPositioned")
    }

    private fun onPositioned(coordinates: LayoutCoordinates, source: String) {
        if (!isAttached) {
            println("$TAG: onPositioned called from $source, but not attached, skipping")
            return
        }

        positionOnScreen = coordinates.positionInWindow()
        size = coordinates.size.toSize()

        updateEffect()
    }

    private fun updateEffect() = trace("CurveEffectNode-updateEffect") {
        println("$TAG: updateEffect() called")

        compositionLocalStyle = currentValueOf(LocalCurveStyle)
        block?.invoke(this)

        val ancestorSourceNode =
            (findNearestAncestor(CurveTraversableNodeKeys.Source) as? CurveSourceNode)
                ?.takeIf { it.curveState == this.curveState }

        println("$TAG: updateEffect() called, curving true, ancestorSourceNode=$ancestorSourceNode")

        val foundAreas = curveState.areasState.value
            .toMutableList()
            .apply { sortBy(CurveArea::zIndex) }

        println("$TAG: updateEffect() called, foundAreas=$foundAreas")

        areaOffsets = if (foundAreas.isNotEmpty()) {
            curveState.areasState.value.associateWith { area -> positionOnScreen - area.positionOnScreen }
        } else {
            emptyMap()
        }

        println("$TAG: updateEffect() called, areaOffsets=$areaOffsets")

        if (foundAreas.isNotEmpty() && size.isSpecified && positionOnScreen.isSpecified) {
            println("$TAG: updateEffect() called, curving true, size=$size, positionOnScreen=$positionOnScreen")
            val inflatedLayerBounds = Rect(positionOnScreen, size)
            layerSize = inflatedLayerBounds.size
            layerOffset = positionOnScreen - inflatedLayerBounds.topLeft
        } else {
            println("$TAG: updateEffect() called, curving false, size=$size, positionOnScreen=$positionOnScreen")
            layerSize = size
            layerOffset = Offset.Zero
        }

        invalidateIfNeeded()
    }

    private fun invalidateIfNeeded() {
        println("$TAG: invalidateIfNeeded called")
        val invalidationRequired = dirtyTracker.any(DirtyFields.InvalidateFlags)
        if (invalidationRequired) {
            invalidateDraw()
            println("$TAG: invalidateIfNeeded was true, invalidating")
        }
    }

    private fun onStyleChanged(old: CurveStyle?, new: CurveStyle?) {
        if (old?.curveIntensity != new?.curveIntensity) dirtyTracker += DirtyFields.CurveIntensity
    }

    internal fun resolveCurveEnabled(): Boolean {
        val isEnabled = when {
            curveEnabledSet -> curveEnabled
            else -> CurveDefaults.curveEnabled()
        }
        println("$TAG: resolveCurveEnabled=$isEnabled")
        return isEnabled
    }
}

internal expect fun CurveEffectNode.updateCurveEffectIfNeeded(drawScope: DrawScope)

internal fun CurveEffectNode.resolveCurveIntensity(): Float =
    if (curveIntensity == -1f) {
        curveStyle.curveIntensity
    } else {
        curveIntensity
    }
