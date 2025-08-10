package link.socket.krystal.curve

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

internal interface CurveEffect {
    fun DrawScope.drawEffect()
    fun cleanup() = Unit
}

interface CurveEffectScope {
    var curveEnabled: Boolean
    var alpha: Float
    var curveStyle: CurveStyle
    var mask: Brush?
    var curveIntensity: Float
    var inputScale: CurveInputScale
    var canDrawArea: ((CurveArea) -> Boolean)?
    var drawContentBehind: Boolean
}

@Stable
fun Modifier.curveEffect(
    curveState: CurveState,
    style: CurveStyle = CurveStyle.Unspecified,
    block: (CurveEffectScope.() -> Unit)? = null,
): Modifier = this then CurveEffectNodeElement(
    curveState = curveState,
    curveStyle = style,
    block = block,
)

private data class CurveEffectNodeElement(
    val curveState: CurveState,
    val curveStyle: CurveStyle = CurveStyle.Unspecified,
    val block: (CurveEffectScope.() -> Unit)? = null,
) : ModifierNodeElement<CurveEffectNode>() {

    override fun create(): CurveEffectNode =
        CurveEffectNode(
            curveState = curveState,
            styleParameter = curveStyle,
            blockParameter = block,
        )

    override fun update(node: CurveEffectNode) {
        curveState.areasState.value.forEach { area ->
            if (node.containsArea(area)) {
                return@forEach
            } else {
                node.addArea(area)
            }
        }
        node.curveStyle = curveStyle
        node.block = block
    }

    override fun InspectorInfo.inspectableProperties() {
        name =  "curveEffect"
    }
}
