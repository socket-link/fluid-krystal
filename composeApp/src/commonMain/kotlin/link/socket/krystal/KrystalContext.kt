package link.socket.krystal

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Rect
import link.socket.krystal.engine.KrystalContentCaptureEngine

data class KrystalContext(
    val bounds: Rect,
    val contentCaptureEngine: KrystalContentCaptureEngine
)

val LocalKrystalContext: ProvidableCompositionLocal<KrystalContext> = compositionLocalOf {
    error("No KrystalContext provided")
}
