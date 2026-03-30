package link.socket.krystal

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import link.socket.krystal.engine.DrawingOperation
import link.socket.krystal.engine.DrawingType

fun Modifier.drawingListener(
    onDrawingOperation: (List<DrawingOperation>) -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "drawingListener"
    }
) {
    val operations = mutableListOf<DrawingOperation>()

    this.drawWithContent {
        val originalDrawRect: (Color, Offset, Size, Float) -> Unit = { color, topLeft, size, alpha ->
            this.drawRect(color, topLeft, size, alpha)
        }
        val originalDrawContent = this::drawContent

        val recorder = object : DrawingRecorder {
            override fun drawRect(
                color: Color,
                topLeft: Offset,
                size: Size,
                alpha: Float
            ) {
                originalDrawRect(color, topLeft, size, alpha)

                operations.add(
                    DrawingOperation(
                        type = DrawingType.BACKGROUND,
                        bounds = Rect(topLeft, size),
                        color = color.copy(alpha = alpha)
                    )
                )
            }

            override fun drawContent() {
                originalDrawContent()
                onDrawingOperation(operations.toList())
                operations.clear()
            }
        }

        with(recorder) {
            drawContent()
        }
    }
}

interface DrawingRecorder {
    fun drawRect(
        color: Color,
        topLeft: Offset = Offset.Zero,
        size: Size,
        alpha: Float = 1.0f
    )

    fun drawContent()
}
