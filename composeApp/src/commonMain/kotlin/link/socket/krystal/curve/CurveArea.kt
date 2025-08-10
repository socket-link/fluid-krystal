package link.socket.krystal.curve

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "CurveArea"

@Stable
class CurveArea {

    var positionOnScreen: Offset by mutableStateOf(Offset.Unspecified)
        internal set

    var size: Size by mutableStateOf(Size.Unspecified)
        internal set

    var zIndex: Float by mutableFloatStateOf(0f)
        internal set

    @OptIn(ExperimentalUuidApi::class)
    var key: Any? = Uuid.random().toString()
        internal set

    var contentLayer:  GraphicsLayer? by mutableStateOf(null)
        internal set

    internal var contentDrawing: Boolean = false

    override fun toString(): String {
        return "$TAG: (positionOnScreen=$positionOnScreen, size=$size, zIndex=$zIndex, key=$key, contentLayer=$contentLayer, contentDrawing=$contentDrawing)"
    }
}
