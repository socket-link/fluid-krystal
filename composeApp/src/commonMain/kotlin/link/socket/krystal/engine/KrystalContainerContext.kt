package link.socket.krystal.engine

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.CoroutineScope
import link.socket.krystal.KrystalStyle

data class KrystalContainerContext(
    val baseHazeState: HazeState,
    val baseKrystalStyle: KrystalStyle,
    val contextStateEngine: KrystalContextStateEngine,
    val contentCaptureEngine: KrystalContentCaptureEngine,
    val containerContent: List<ContentInfo>,
) {
    companion object {
        fun newInstance(
            scope: CoroutineScope,
            baseHazeState: HazeState,
            baseKrystalStyle: KrystalStyle,
            contentEngine: KrystalContentCaptureEngine = KrystalContentCaptureEngine(),
        ): KrystalContainerContext {
            val contextEngine = KrystalContextStateEngine(
                scope,
                baseHazeState,
                baseKrystalStyle,
            )

            return KrystalContainerContext(
                baseHazeState = baseHazeState,
                baseKrystalStyle = baseKrystalStyle,
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
