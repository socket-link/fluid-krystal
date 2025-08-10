package link.socket.krystal.engine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import link.socket.krystal.KrystalStyle
import link.socket.krystal.curve.CurveState
import link.socket.krystal.curve.rememberCurveState

val LocalKrystalContainerContext: ProvidableCompositionLocal<KrystalContainerContext> = compositionLocalOf {
    error("No KrystalContainerContext provided")
}

data class KrystalContainerContext(
    val contentCaptureEngine: KrystalContentCaptureEngine,
    private val initialHazeState: HazeState,
    private val initialCurveState: CurveState,
    private val baseContainerStyle: KrystalStyle.Container,
) {
    private val _hazeState: MutableStateFlow<HazeState> = MutableStateFlow(initialHazeState)
    private val _curveState: MutableStateFlow<CurveState> = MutableStateFlow(initialCurveState)
    private val _containerStyle: MutableStateFlow<KrystalStyle.Container> = MutableStateFlow(baseContainerStyle)
    private val _surfaceStyleCache: MutableStateFlow<Map<String, KrystalStyle.Surface>> = MutableStateFlow(emptyMap())

    val hazeState: StateFlow<HazeState> = _hazeState.asStateFlow()
    val curveState: StateFlow<CurveState> = _curveState.asStateFlow()
    val containerStyle: StateFlow<KrystalStyle.Container> = _containerStyle.asStateFlow()
    val surfaceStyleCache: StateFlow<Map<String, KrystalStyle.Surface>> = _surfaceStyleCache.asStateFlow()
}

@Composable
fun rememberKrystalContainerContext(
    baseContainerStyle: KrystalStyle.Container = KrystalStyle.Container.EMPTY,
    contentCaptureEngine: KrystalContentCaptureEngine = rememberKrystalContentCaptureEngine(),
    initialHazeState: HazeState = rememberHazeState(),
    initialCurveState: CurveState = rememberCurveState(),
): KrystalContainerContext {
    return remember {
        KrystalContainerContext(
            contentCaptureEngine = contentCaptureEngine,
            initialHazeState = initialHazeState,
            initialCurveState = initialCurveState,
            baseContainerStyle = baseContainerStyle,
        )
    }
}
