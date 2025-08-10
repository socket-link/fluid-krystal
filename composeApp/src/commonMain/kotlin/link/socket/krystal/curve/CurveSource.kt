package link.socket.krystal.curve

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "CurveSource"

@OptIn(ExperimentalUuidApi::class)
@Stable
internal fun Modifier.curveSource(
    state: CurveState,
    zIndex: Float = 0f,
    key: Any? = null,
) : Modifier {
    return this then CurveSourceElement(state, zIndex, key)
}

private data class CurveSourceElement(
    val curveState: CurveState,
    val zIndex: Float = 0f,
    val key: Any? = null,
) : ModifierNodeElement<CurveSourceNode>() {

    override fun create(): CurveSourceNode {
        println("$TAG: create() called, state: $curveState, zIndex: $zIndex, key: $key")
        return CurveSourceNode(
            curveState,
            zIndex,
            key,
        )
    }

    override fun update(node: CurveSourceNode) {
        println("CurveSourceElement: update() called, state: ${node.curveState}, key: ${node.key}")

        curveState.areasState.value.forEach { area ->
            if (node.curveState.areasState.value.contains(area)) {
                return@forEach
            } else {
                node.curveState.addArea(area)
            }
        }
        node.zIndex = zIndex
        node.key = key
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "curveSource"
        properties["zIndex"] = zIndex
        properties["key"] = key
    }
}
