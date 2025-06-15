package link.socket.krystal.blur

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint as ComposePaint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

actual fun createNativeBlurStrategy(): BlurStrategy = AndroidNativeBlurStrategy
actual fun createAcceleratedBlurStrategy(): BlurStrategy = AndroidAcceleratedBlurStrategy

data object AndroidNativeBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    override val performanceLevel = BlurPerformance.NATIVE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bitmap.applyRenderEffectBlur(radius)
        } else {
            throw UnsupportedOperationException("RenderEffect blur requires API 31+")
        }
    }
}

data object AndroidAcceleratedBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = true

    override val performanceLevel = BlurPerformance.ACCELERATED

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.applyCanvasBlur(radius)
    }
}

private fun ImageBitmap.toAndroidBitmap(): Bitmap {
    val androidBitmap = createBitmap(width, height)

    try {
        val pixelMap = this.toPixelMap()
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = pixelMap[x, y]
                pixels[y * width + x] = color.toArgb()
            }
        }

        androidBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

    } catch (e: Exception) {
        println(e)

        val canvas = Canvas(androidBitmap)
        val paint = Paint().apply {
            isAntiAlias = true
        }

        for (y in 0 until height step 4) {
            for (x in 0 until width step 4) {
                val gray = ((x + y) * 255 / (width + height)).coerceIn(0, 255)
                paint.color = Color.argb(255, gray, gray, gray)
                canvas.drawRect(x.toFloat(), y.toFloat(), (x + 4).toFloat(), (y + 4).toFloat(), paint)
            }
        }
    }

    return androidBitmap
}

private fun Bitmap.toComposeImageBitmap(): ImageBitmap {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)

    val composeBitmap = ImageBitmap(width, height)
    val canvas = ComposeCanvas(composeBitmap)
    val paint = ComposePaint()

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = pixels[y * width + x]
            paint.color = ComposeColor(pixel)
            canvas.drawRect(
                Rect(
                    x.toFloat(), y.toFloat(),
                    (x + 1).toFloat(), (y + 1).toFloat()
                ),
                paint
            )
        }
    }

    return composeBitmap
}

@RequiresApi(Build.VERSION_CODES.S)
private fun ImageBitmap.applyRenderEffectBlur(radius: Float): ImageBitmap {
    val androidBitmap = this.toAndroidBitmap()

    val blurredBitmap = createBitmap(androidBitmap.width, androidBitmap.height)
    val canvas = Canvas(blurredBitmap)

    canvas.enableZ()
    canvas.save()
    return applyCanvasBlur(radius)
}

private fun ImageBitmap.applyCanvasBlur(radius: Float): ImageBitmap {
    val androidBitmap = this.toAndroidBitmap()

    val scaleFactor = 0.25f
    val scaledWidth = (androidBitmap.width * scaleFactor).toInt().coerceAtLeast(1)
    val scaledHeight = (androidBitmap.height * scaleFactor).toInt().coerceAtLeast(1)

    val scaledBitmap = androidBitmap.scale(scaledWidth, scaledHeight)

    var currentBitmap = scaledBitmap
    val passes = (radius / 2).toInt().coerceIn(1, 4)

    repeat(passes) {
        currentBitmap = applySimpleBlur(currentBitmap)
    }

    val finalBitmap = currentBitmap.scale(androidBitmap.width, androidBitmap.height)

    return finalBitmap.toComposeImageBitmap()
}

private fun applySimpleBlur(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val blurred = createBitmap(width, height)

    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val output = IntArray(width * height)

    val kernel = floatArrayOf(
        1f/9f, 1f/9f, 1f/9f,
        1f/9f, 1f/9f, 1f/9f,
        1f/9f, 1f/9f, 1f/9f
    )

    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var red = 0f
            var green = 0f
            var blue = 0f
            var alpha = 0f

            for (ky in -1..1) {
                for (kx in -1..1) {
                    val pixel = pixels[(y + ky) * width + (x + kx)]
                    val weight = kernel[(ky + 1) * 3 + (kx + 1)]

                    alpha += ((pixel shr 24) and 0xFF) * weight
                    red += ((pixel shr 16) and 0xFF) * weight
                    green += ((pixel shr 8) and 0xFF) * weight
                    blue += (pixel and 0xFF) * weight
                }
            }

            output[y * width + x] = (
                (alpha.toInt() shl 24) or
                    (red.toInt() shl 16) or
                    (green.toInt() shl 8) or
                    blue.toInt()
                )
        }
    }

    blurred.setPixels(output, 0, width, 0, 0, width, height)
    return blurred
}
