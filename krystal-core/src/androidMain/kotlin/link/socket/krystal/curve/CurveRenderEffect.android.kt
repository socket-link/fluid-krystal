package link.socket.krystal.curve

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode

internal actual fun CompositionLocalConsumerModifierNode.createRenderEffect(
    params: RenderEffectParams,
): RenderEffect? {
    // Android curve rendering uses RenderEffect from the framework.
    // Full Skia-based shader pipeline is not available on Android;
    // return null to fall back to the non-curve path.
    return null
}

internal actual fun CurveRenderEffect.drawEffect(
    drawScope: DrawScope,
    contentLayer: GraphicsLayer,
) {
    with(drawScope) {
        contentLayer.renderEffect = node.getOrCreateRenderEffect()
        contentLayer.alpha = node.alpha
        drawLayer(contentLayer)
    }
}
