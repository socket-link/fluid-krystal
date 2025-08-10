package link.socket.krystal.curve

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.ExperimentalUuidApi

@Stable
data class CurveState(
    val curveEnabledParameter: Boolean = CurveDefaults.curveEnabled(),
) {
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
    return remember {
        CurveState(
            curveEnabledParameter = curveEnabled,
        )
    }.apply {
        this.curveEnabled = curveEnabled
    }
}
