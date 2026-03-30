package link.socket.krystal.curve

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode

// iOS passes through to native Liquid Glass — these stubs exist only to satisfy
// the expect/actual contract. They are never called at runtime on Apple platforms.

internal actual fun CompositionLocalConsumerModifierNode.createRenderEffect(
    params: RenderEffectParams,
): RenderEffect? = null

internal actual fun CurveRenderEffect.drawEffect(
    drawScope: DrawScope,
    contentLayer: GraphicsLayer,
) = Unit
