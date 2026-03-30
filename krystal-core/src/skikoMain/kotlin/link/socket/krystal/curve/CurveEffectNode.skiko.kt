package link.socket.krystal.curve

import androidx.compose.ui.graphics.drawscope.DrawScope

private const val TAG = "CurveEffectNode.skiko"

internal actual fun CurveEffectNode.updateCurveEffectIfNeeded(drawScope: DrawScope) {
    when {
        resolveCurveEnabled() -> {
            println("$TAG: Updating curve effect node, curve enabled")
            if (curveEffect !is CurveRenderEffect) {
                curveEffect = CurveRenderEffect(this)
            }
        }
        else -> {
            println("$TAG: Updating curve effect node, curve disabled")
            // TODO: Handle fallback
        }
    }
}
