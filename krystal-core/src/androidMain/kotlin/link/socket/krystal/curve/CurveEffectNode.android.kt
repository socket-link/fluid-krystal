package link.socket.krystal.curve

import androidx.compose.ui.graphics.drawscope.DrawScope

internal actual fun CurveEffectNode.updateCurveEffectIfNeeded(drawScope: DrawScope) {
    when {
        resolveCurveEnabled() -> {
            if (curveEffect !is CurveRenderEffect) {
                curveEffect = CurveRenderEffect(this)
            }
        }
        else -> {
            // TODO: Handle fallback
        }
    }
}
