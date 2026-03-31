package link.socket.krystal.curve

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.ExperimentalUuidApi

@Stable
data class CurveState(
    val curveEnabledParameter: Boolean = CurveDefaults.curveEnabled(),
    val graphicsLayer: GraphicsLayer,
) {
    var position: Offset by mutableStateOf(Offset.Zero)

    private val _areasState = MutableStateFlow<List<CurveArea>>(emptyList())
    val areasState: StateFlow<List<CurveArea>> = _areasState.asStateFlow()

    var curveEnabled: Boolean by mutableStateOf(curveEnabledParameter)

    internal fun addArea(area: CurveArea) {
        _areasState.value += area
    }

    internal fun removeArea(area: CurveArea) {
        _areasState.value -= area
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun rememberCurveState(
    curveEnabled: Boolean = CurveDefaults.curveEnabled(),
): CurveState {
    val graphicsLayer = rememberGraphicsLayer()

    return remember(curveEnabled, graphicsLayer) {
        CurveState(
            curveEnabledParameter = curveEnabled,
            graphicsLayer = graphicsLayer,
        )
    }.apply {
        this.curveEnabled = curveEnabled
    }
}
