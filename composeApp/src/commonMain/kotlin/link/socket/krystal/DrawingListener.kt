package link.socket.krystal

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.debugInspectorInfo

/**
 * Custom modifier that captures drawing operations to improve content analysis.
 */
fun Modifier.drawingListener(
    onDrawingOperation: (List<DrawingOperation>) -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "drawingListener"
    }
) {
    val operations = mutableListOf<DrawingOperation>()

    this.drawWithContent {
        // Record drawing operations
        val originalDrawRect: (Color, Offset, Size, Float) -> Unit = { color, topLeft, size, alpha ->
            this.drawRect(color, topLeft, size, alpha)
        }
        val originalDrawContent = this::drawContent

        // Create drawing recorder to capture operations
        val recorder = object : DrawingRecorder {
            override fun drawRect(
                color: Color,
                topLeft: Offset,
                size: Size,
                alpha: Float
            ) {
                // Call original implementation
                originalDrawRect(color, topLeft, size, alpha)

                // Record the operation
                operations.add(DrawingOperation(
                    type = DrawingType.BACKGROUND,
                    bounds = androidx.compose.ui.geometry.Rect(topLeft, size),
                    color = color.copy(alpha = alpha)
                ))
            }

            override fun drawContent() {
                // Draw original content
                originalDrawContent()
                // Report the operations collected
                onDrawingOperation(operations.toList())
                // Clear the operations list for the next frame
                operations.clear()
            }
        }

        // Use the recorder to draw content
        with(recorder) {
            drawContent()
        }
    }
}

/**
 * Interface for recording drawing operations
 */
interface DrawingRecorder {
    fun drawRect(
        color: Color,
        topLeft: Offset = Offset.Zero,
        size: Size,
        alpha: Float = 1.0f
    )

    fun drawContent()
}
