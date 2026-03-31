package link.socket.krystal.curve

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode

internal actual fun CompositionLocalConsumerModifierNode.createRenderEffect(
    params: RenderEffectParams,
): RenderEffect? {
    // Android curve rendering uses RenderEffect from the framework.
    // Full Skia-based shader pipeline is not available on Android;
    // return null to fall back to the non-curve path.
    return null
}
