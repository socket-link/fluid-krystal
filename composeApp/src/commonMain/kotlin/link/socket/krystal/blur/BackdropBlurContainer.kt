package link.socket.krystal.blur

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap

@Composable
fun BackdropBlurContainer(
    modifier: Modifier = Modifier,
    saturation: Float = 1.8f,
    content: @Composable BackdropBlurScope.() -> Unit
) {
    var backgroundBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var blurredBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var scope by remember { mutableStateOf(BackdropBlurScopeImpl(null)) }

    LaunchedEffect(blurredBitmap) {
        scope = BackdropBlurScopeImpl(blurredBitmap)
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .drawWithContent {
                    drawContent()

                    val width = size.width.toInt().coerceAtLeast(1)
                    val height = size.height.toInt().coerceAtLeast(1)

                    val testBitmap = createTestBitmap(width, height)
                    backgroundBitmap = testBitmap

                    backgroundBitmap?.let { bitmap ->
                        try {
                            val processed = bitmap.applySaturation(saturation)
                            blurredBitmap = processed
                        } catch (e: Exception) {
                            println("Error processing background: $e")
                            blurredBitmap = bitmap
                        }
                    }
                }
        )

        content(scope)
    }
}

private fun createTestBitmap(width: Int, height: Int): ImageBitmap {
    val bitmap = ImageBitmap(width, height)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    for (y in 0 until height) {
        for (x in 0 until width) {
            val red = (x.toFloat() / width * 255).toInt().coerceIn(0, 255)
            val green = (y.toFloat() / height * 255).toInt().coerceIn(0, 255)
            val blue = 128

            paint.color = Color(red = red / 255f, green = green / 255f, blue = blue / 255f, alpha = 1f)
            canvas.drawRect(
                Rect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat()),
                paint
            )
        }
    }

    return bitmap
}

fun ImageBitmap.applySaturation(saturation: Float): ImageBitmap {
    if (saturation == 1f) return this

    val width = this.width
    val height = this.height

    if (width <= 0 || height <= 0) {
        println("Invalid bitmap dimensions: ${width}x${height}")
        return this
    }

    return try {
        val pixelMap = this.toPixelMap()
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = pixelMap[x, y]
                val pixel = color.toArgb()

                val alpha = (pixel shr 24) and 0xFF
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF

                val gray = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()

                val newRed = (gray + saturation * (red - gray)).coerceIn(0f, 255f).toInt()
                val newGreen = (gray + saturation * (green - gray)).coerceIn(0f, 255f).toInt()
                val newBlue = (gray + saturation * (blue - gray)).coerceIn(0f, 255f).toInt()

                pixels[y * width + x] = (alpha shl 24) or (newRed shl 16) or (newGreen shl 8) or newBlue
            }
        }

        createImageBitmapFromPixels(pixels, width, height)
    } catch (e: Exception) {
        println("Error applying saturation: $e")
        this
    }
}
