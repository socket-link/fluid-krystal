package link.socket.krystal.engine

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.CoroutineScope
import link.socket.krystal.KrystalStyle

data class KrystalContainerContext(
    val baseHazeState: HazeState,
    val baseKrystalContainerStyle: KrystalStyle.Container,
    val contextStateEngine: KrystalContextStateEngine,
    val contentCaptureEngine: KrystalContentCaptureEngine,
    val containerContent: List<ContentInfo>,
) {
    companion object {
        fun newInstance(
            scope: CoroutineScope,
            baseHazeState: HazeState,
            baseKrystalContainerStyle: KrystalStyle.Container,
            baseKrystalSurfaceStyle: KrystalStyle.Surface,
            contentEngine: KrystalContentCaptureEngine = KrystalContentCaptureEngine(),
        ): KrystalContainerContext {
            val contextEngine = KrystalContextStateEngine(
                scope,
                baseHazeState,
                baseKrystalSurfaceStyle,
            )

            return KrystalContainerContext(
                baseHazeState = baseHazeState,
                baseKrystalContainerStyle = baseKrystalContainerStyle,
                contextStateEngine = contextEngine,
                contentCaptureEngine = contentEngine,
                containerContent = emptyList(),
            )
        }
    }
}

val LocalKrystalContainerContext: ProvidableCompositionLocal<KrystalContainerContext> = compositionLocalOf {
    error("No KrystalContext provided")
}
