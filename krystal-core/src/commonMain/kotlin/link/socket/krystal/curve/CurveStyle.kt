package link.socket.krystal.curve

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalCurveStyle: ProvidableCompositionLocal<CurveStyle> =
    compositionLocalOf { CurveStyle.Unspecified }

@Immutable
data class CurveStyle(
    val curveIntensity: Float,
) {
    companion object {
        val Unspecified: CurveStyle = CurveDefaults.style()
    }
}
