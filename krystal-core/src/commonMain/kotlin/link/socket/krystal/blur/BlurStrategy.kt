package link.socket.krystal.blur

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap

enum class BlurPerformance { NATIVE, ACCELERATED, SOFTWARE }

interface BlurStrategy {
    fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap
    val isAvailable: Boolean
    val performanceLevel: BlurPerformance
}

expect fun createNativeBlurStrategy(): BlurStrategy
expect fun createAcceleratedBlurStrategy(): BlurStrategy

class SoftwareBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = true
    override val performanceLevel = BlurPerformance.SOFTWARE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.softwareGaussianBlur(radius)
    }
}

fun ImageBitmap.applyGaussianBlur(radius: Float): ImageBitmap {
    if (radius <= 0f) return this
    return BlurEngine.instance.blur(this, radius)
}

private fun ImageBitmap.softwareGaussianBlur(radius: Float): ImageBitmap {
    if (radius <= 0f) return this

    val width = this.width
    val height = this.height

    try {
        val pixelMap = this.toPixelMap()
        val inputPixels = IntArray(width * height)
        val outputPixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = pixelMap[x, y]
                inputPixels[y * width + x] = color.toArgb()
            }
        }

        applyHorizontalBlur(inputPixels, outputPixels, width, height, radius)
        applyVerticalBlur(outputPixels, inputPixels, width, height, radius)

        return createImageBitmapFromPixels(inputPixels, width, height)

    } catch (e: Exception) {
        println(e)
        return this
    }
}

private fun applyHorizontalBlur(
    input: IntArray,
    output: IntArray,
    width: Int,
    height: Int,
    radius: Float
) {
    val kernelSize = (radius * 2).toInt() + 1
    val kernel = generateGaussianKernel(radius, kernelSize)
    val halfKernel = kernelSize / 2

    for (y in 0 until height) {
        for (x in 0 until width) {
            var red = 0f
            var green = 0f
            var blue = 0f
            var alpha = 0f
            var weightSum = 0f

            for (i in 0 until kernelSize) {
                val sampleX = (x - halfKernel + i).coerceIn(0, width - 1)
                val sampleIndex = y * width + sampleX
                val samplePixel = input[sampleIndex]
                val weight = kernel[i]

                red += ((samplePixel shr 16) and 0xFF) * weight
                green += ((samplePixel shr 8) and 0xFF) * weight
                blue += (samplePixel and 0xFF) * weight
                alpha += ((samplePixel shr 24) and 0xFF) * weight
                weightSum += weight
            }

            val outputIndex = y * width + x
            output[outputIndex] = (
                ((alpha / weightSum).toInt() shl 24) or
                    ((red / weightSum).toInt() shl 16) or
                    ((green / weightSum).toInt() shl 8) or
                    (blue / weightSum).toInt()
                )
        }
    }
}

private fun applyVerticalBlur(
    input: IntArray,
    output: IntArray,
    width: Int,
    height: Int,
    radius: Float
) {
    val kernelSize = (radius * 2).toInt() + 1
    val kernel = generateGaussianKernel(radius, kernelSize)
    val halfKernel = kernelSize / 2

    for (x in 0 until width) {
        for (y in 0 until height) {
            var red = 0f
            var green = 0f
            var blue = 0f
            var alpha = 0f
            var weightSum = 0f

            for (i in 0 until kernelSize) {
                val sampleY = (y - halfKernel + i).coerceIn(0, height - 1)
                val sampleIndex = sampleY * width + x
                val samplePixel = input[sampleIndex]
                val weight = kernel[i]

                red += ((samplePixel shr 16) and 0xFF) * weight
                green += ((samplePixel shr 8) and 0xFF) * weight
                blue += (samplePixel and 0xFF) * weight
                alpha += ((samplePixel shr 24) and 0xFF) * weight
                weightSum += weight
            }

            val outputIndex = y * width + x
            output[outputIndex] = (
                ((alpha / weightSum).toInt() shl 24) or
                    ((red / weightSum).toInt() shl 16) or
                    ((green / weightSum).toInt() shl 8) or
                    (blue / weightSum).toInt()
                )
        }
    }
}

fun generateGaussianKernel(radius: Float, size: Int): FloatArray {
    val kernel = FloatArray(size)
    val sigma = radius / 3f
    val twoSigmaSquared = 2f * sigma * sigma
    val center = size / 2

    var sum = 0f
    for (i in 0 until size) {
        val distance = (i - center).toFloat()
        kernel[i] = kotlin.math.exp(-(distance * distance) / twoSigmaSquared)
        sum += kernel[i]
    }

    for (i in 0 until size) {
        kernel[i] /= sum
    }

    return kernel
}
