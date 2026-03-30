package link.socket.krystal.blur

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint

fun createImageBitmapFromPixels(pixels: IntArray, width: Int, height: Int): ImageBitmap {
    if (width <= 0 || height <= 0 || pixels.isEmpty()) {
        println("Invalid parameters for createImageBitmapFromPixels: ${width}x${height}, pixels: ${pixels.size}")
        return ImageBitmap(1, 1)
    }

    return try {
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                paint.color = Color(pixel)
                canvas.drawRect(
                    Rect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat()),
                    paint
                )
            }
        }

        bitmap
    } catch (e: Exception) {
        println("Error creating bitmap from pixels: $e")
        ImageBitmap(1, 1)
    }
}
