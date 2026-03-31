package link.socket.krystal.curve

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

internal interface CurveEffect {
    fun DrawScope.drawEffect()
    fun cleanup() = Unit
}

@Stable
fun Modifier.curveEffect(
    curveState: CurveState,
    style: CurveStyle = CurveStyle.Unspecified,
): Modifier = this then CurveEffectNodeElement(
    curveState = curveState,
    curveStyle = style,
)

private data class CurveEffectNodeElement(
    val curveState: CurveState,
    val curveStyle: CurveStyle = CurveStyle.Unspecified,
) : ModifierNodeElement<CurveEffectNode>() {

    override fun create(): CurveEffectNode =
        CurveEffectNode(
            curveState = curveState,
            styleParameter = curveStyle,
        )

    override fun update(node: CurveEffectNode) {
        node.update(
            newState = curveState,
            newStyle = curveStyle,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "curveEffect"
    }
}
