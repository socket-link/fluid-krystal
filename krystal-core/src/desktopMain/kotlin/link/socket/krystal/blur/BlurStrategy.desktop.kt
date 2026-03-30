package link.socket.krystal.blur

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap

private object DesktopPlatform {
    private val osName = System.getProperty("os.name").lowercase()

    val isMacOS: Boolean = osName.contains("mac") || osName.contains("darwin")
    val isWindows: Boolean = osName.contains("win")
    val isLinux: Boolean = osName.contains("nix") || osName.contains("nux") || osName.contains("aix")

    fun getPlatformName(): String = when {
        isMacOS -> "macOS"
        isWindows -> "Windows"
        isLinux -> "Linux"
        else -> "Unknown Desktop Platform"
    }
}

actual fun createNativeBlurStrategy(): BlurStrategy {
    println("createNativeBlurStrategy: ${DesktopPlatform.getPlatformName()}")
    return when {
        DesktopPlatform.isMacOS -> createAppleNativeBlurStrategy()
        else -> createCommonNativeBlurStrategy()
    }
}

actual fun createAcceleratedBlurStrategy(): BlurStrategy {
    println("createAcceleratedBlurStrategy: ${DesktopPlatform.getPlatformName()}")
    return when {
        DesktopPlatform.isMacOS -> createAppleAcceleratedBlurStrategy()
        else -> createCommonAcceleratedBlurStrategy()
    }
}

private fun createAppleNativeBlurStrategy(): BlurStrategy = AppleDesktopNativeBlurStrategy()
private fun createAppleAcceleratedBlurStrategy(): BlurStrategy = AppleDesktopAcceleratedBlurStrategy()

private fun createCommonNativeBlurStrategy(): BlurStrategy = CommonDesktopNativeBlurStrategy()
private fun createCommonAcceleratedBlurStrategy(): BlurStrategy = CommonDesktopAcceleratedBlurStrategy()

class AppleDesktopNativeBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean by lazy {
        DesktopPlatform.isMacOS && checkAppleBlurCapabilities()
    }

    override val performanceLevel = BlurPerformance.NATIVE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return if (isAvailable) {
            // TODO: Integrate with SwiftUI's native blur when available
            bitmap.appleDesktopBlur(radius)
        } else {
            throw UnsupportedOperationException("Apple native blur not available on this system")
        }
    }

    private fun checkAppleBlurCapabilities(): Boolean {
        return try {
            val osVersion = System.getProperty("os.version")
            val majorVersion = osVersion.split(".")[0].toIntOrNull() ?: 0
            majorVersion >= 21
        } catch (e: Exception) {
            println(e)
            false
        }
    }
}

class AppleDesktopAcceleratedBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = DesktopPlatform.isMacOS
    override val performanceLevel = BlurPerformance.ACCELERATED

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.appleDesktopBlur(radius)
    }
}

class CommonDesktopNativeBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean by lazy {
        checkHardwareAcceleration()
    }

    override val performanceLevel = BlurPerformance.NATIVE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return if (isAvailable) {
            bitmap.hardwareAcceleratedBlur(radius)
        } else {
            throw UnsupportedOperationException("Hardware-accelerated blur not available")
        }
    }

    private fun checkHardwareAcceleration(): Boolean {
        return try {
            when {
                DesktopPlatform.isWindows -> checkDirectXSupport()
                DesktopPlatform.isLinux -> checkOpenGLSupport()
                else -> false
            }
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    private fun checkDirectXSupport(): Boolean {
        // TODO: Implement proper check
        return System.getProperty("java.awt.headless") != "true"
    }

    private fun checkOpenGLSupport(): Boolean {
        // TODO: Implement proper check
        return System.getProperty("java.awt.headless") != "true"
    }
}

class CommonDesktopAcceleratedBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = true
    override val performanceLevel = BlurPerformance.ACCELERATED

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.commonDesktopBlur(radius)
    }
}

// Platform-specific blur implementations

private fun ImageBitmap.appleDesktopBlur(radius: Float): ImageBitmap {
    return this.applyOptimizedGaussianBlur(radius, useAppleOptimizations = true)
}

private fun ImageBitmap.hardwareAcceleratedBlur(radius: Float): ImageBitmap {
    return this.applyOptimizedGaussianBlur(radius, useAppleOptimizations = false)
}

private fun ImageBitmap.commonDesktopBlur(radius: Float): ImageBitmap {
    return this.applyOptimizedGaussianBlur(radius, useAppleOptimizations = false)
}

private fun ImageBitmap.applyOptimizedGaussianBlur(radius: Float, useAppleOptimizations: Boolean): ImageBitmap {
    val blurred = ImageBitmap(width, height)
    val canvas = Canvas(blurred)

    try {
        val pixelMap = this.toPixelMap()
        val sourcePixels = Array(height) { y ->
            IntArray(width) { x ->
                pixelMap[x, y].toArgb()
            }
        }

        val kernelSize = if (useAppleOptimizations) {
            (radius * 2.5).toInt().coerceAtLeast(3) or 1
        } else {
            (radius * 1.5).toInt().coerceAtLeast(3) or 1
        }

        val kernel = createGaussianKernel(kernelSize, radius)
        val offset = kernelSize / 2
        val tempPixels = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                var red = 0f
                var green = 0f
                var blue = 0f
                var alpha = 0f

                for (i in 0 until kernelSize) {
                    val sourceX = (x + i - offset).coerceIn(0, width - 1)
                    val pixel = sourcePixels[y][sourceX]
                    val weight = kernel[i]

                    alpha += ((pixel shr 24) and 0xFF) * weight
                    red += ((pixel shr 16) and 0xFF) * weight
                    green += ((pixel shr 8) and 0xFF) * weight
                    blue += (pixel and 0xFF) * weight
                }

                tempPixels[y][x] = (alpha.toInt() shl 24) or
                    (red.toInt() shl 16) or
                    (green.toInt() shl 8) or
                    blue.toInt()
            }
        }

        val paint = Paint()
        for (y in 0 until height) {
            for (x in 0 until width) {
                var red = 0f
                var green = 0f
                var blue = 0f
                var alpha = 0f

                for (i in 0 until kernelSize) {
                    val sourceY = (y + i - offset).coerceIn(0, height - 1)
                    val pixel = tempPixels[sourceY][x]
                    val weight = kernel[i]

                    alpha += ((pixel shr 24) and 0xFF) * weight
                    red += ((pixel shr 16) and 0xFF) * weight
                    green += ((pixel shr 8) and 0xFF) * weight
                    blue += (pixel and 0xFF) * weight
                }

                paint.color = Color(
                    red = red.toInt().coerceIn(0, 255) / 255f,
                    green = green.toInt().coerceIn(0, 255) / 255f,
                    blue = blue.toInt().coerceIn(0, 255) / 255f,
                    alpha = alpha.toInt().coerceIn(0, 255) / 255f
                )

                canvas.drawRect(
                    Rect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat()),
                    paint
                )
            }
        }

    } catch (e: Exception) {
        println(e)
        drawFallbackPattern(canvas, width, height)
    }

    return blurred
}

private fun createGaussianKernel(size: Int, sigma: Float): FloatArray {
    val kernel = FloatArray(size)
    val twoSigmaSquared = 2 * sigma * sigma
    val center = size / 2
    var sum = 0f

    for (i in 0 until size) {
        val x = i - center
        kernel[i] = kotlin.math.exp(-(x * x) / twoSigmaSquared)
        sum += kernel[i]
    }

    for (i in 0 until size) {
        kernel[i] /= sum
    }

    return kernel
}

private fun drawFallbackPattern(canvas: Canvas, width: Int, height: Int) {
    val paint = Paint().apply {
        isAntiAlias = true
    }

    for (y in 0 until height step 4) {
        for (x in 0 until width step 4) {
            val gray = ((x + y) * 255 / (width + height)).coerceIn(0, 255) / 255f
            paint.color = Color(gray, gray, gray, 1f)
            canvas.drawRect(
                Rect(x.toFloat(), y.toFloat(), (x + 4).toFloat(), (y + 4).toFloat()),
                paint
            )
        }
    }
}
