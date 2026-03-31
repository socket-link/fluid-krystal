package link.socket.krystal.curve

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.TraversableNode
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.DisposableHandle
import kotlin.math.roundToInt

private const val TAG = "CurveSourceNode"

class CurveSourceNode(
    val curveState: CurveState,
    zIndexParameter: Float = 0f,
    keyParameter: Any? = null,
) : Modifier.Node(),
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode,
    LayoutAwareModifierNode,
    DrawModifierNode,
    TraversableNode {

    override val traverseKey: Any
        get() = CurveTraversableNodeKeys.Source

    internal val area = CurveArea()

    var zIndex: Float = zIndexParameter
        set(value) {
            field = value
            area.zIndex = value
        }

    var key: Any? = null
        get() = area.key
        set(value) {
            if (field != value) {
                field = value
                area.key = value
            }
        }

    init {
        this.key = keyParameter
    }

    private var preDrawDisposable: DisposableHandle? = null

    override val shouldAutoInvalidate: Boolean = false

    override fun onAttach() {
        curveState.addArea(area)
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        Snapshot.withoutReadObservation {
            if (area.positionOnScreen.isUnspecified) {
                onPositioned(coordinates, "onPlaced")
            }
        }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        onPositioned(coordinates, "onGloballyPositioned")
    }

    override fun ContentDrawScope.draw() {
        try {
            area.contentDrawing = true

            if (!isAttached) {
                return
            }

            if (size.minDimension.roundToInt() >= 1) {
                curveState.graphicsLayer.record {
                    this@draw.drawContentSafely()
                }

                drawLayer(curveState.graphicsLayer)
            } else {
                drawContentSafely()
            }
        } finally {
            area.contentDrawing = false
        }
    }

    override fun onDetach() {
        println("$TAG: onDetach, area=${area.key}")
        preDrawDisposable?.dispose()
        area.reset()
        curveState.removeArea(area)
        println("$TAG: onDetach, new curveState=$curveState")
    }

    override fun onReset() {
        println("$TAG: onReset, resetting area=${area.key}")
        area.reset()
    }

    private fun onPositioned(coordinates: LayoutCoordinates, source: String) {
        println("$TAG: onPositioned called from $source, area=${area.key}")
        if (!isAttached) {
            println("$TAG: onPositioned called from $source, but not attached, skipping")
            return
        }

        val pos = coordinates.positionInWindow()
        area.positionOnScreen = pos
        area.size = coordinates.size.toSize()
        curveState.position = pos
    }

    private fun CurveArea.reset() {
        println("$TAG: resetting area=${this.key}")
        positionOnScreen = Offset.Unspecified
        size = Size.Unspecified
        contentDrawing = false
    }

    override fun toString(): String =
        "CurveSourceNode(area=$area, curveState=$curveState, zIndex=$zIndex)"
}
