package link.socket.krystal.curve

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer

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
