package link.socket.krystal.blur

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import link.socket.krystal.blur.WebPlatform.hasCSSFilters
import link.socket.krystal.blur.WebPlatform.hasOffscreenCanvas
import link.socket.krystal.blur.WebPlatform.hasWebGL
import link.socket.krystal.blur.WebPlatform.hasWebGL2

external interface JsImageData : JsAny {
    val width: Int
    val height: Int
    val data: JsArray<JsNumber>
}

private object WebPlatform {
    val isWebWorker: Boolean =
        js("typeof importScripts === 'function'") as Boolean

    val hasWebGL: Boolean =
        js("""
                (function() {
                    try {
                        var canvas = document.createElement('canvas');
                        var gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
                        return gl !== null;
                    } catch (e) {
                        return false;
                    }
                })()
            """) as Boolean

    val hasWebGL2: Boolean =
        js("""
                (function() {
                    try {
                        var canvas = document.createElement('canvas');
                        var gl2 = canvas.getContext('webgl2');
                        return gl2 !== null;
                    } catch (e) {
                        return false;
                    }
                })()
            """) as Boolean

    val hasOffscreenCanvas: Boolean =
        js("typeof OffscreenCanvas !== 'undefined'") as Boolean

    val hasCSSFilters: Boolean =
        js("""
                (function() {
                    try {
                        var testElement = document.createElement('div');
                        testElement.style.filter = 'blur(1px)';
                        return testElement.style.filter !== '';
                    } catch (e) {
                        return false;
                    }
                })()
            """) as Boolean

    fun getPlatformCapabilities(): String {
        val capabilities = mutableListOf<String>()
        if (hasWebGL2) capabilities.add("WebGL2")
        else if (hasWebGL) capabilities.add("WebGL")
        if (hasOffscreenCanvas) capabilities.add("OffscreenCanvas")
        if (hasCSSFilters) capabilities.add("CSS Filters")
        if (isWebWorker) capabilities.add("Web Worker")
        return capabilities.joinToString(", ").ifEmpty { "Basic Canvas" }
    }
}

actual fun createNativeBlurStrategy(): BlurStrategy {
    println("Web Platform Capabilities: ${WebPlatform.getPlatformCapabilities()}")

    return when {
        hasWebGL2 -> WebGL2BlurStrategy()
        hasWebGL -> WebGLBlurStrategy()
        hasOffscreenCanvas -> OffscreenCanvasBlurStrategy()
        else -> CanvasBlurStrategy()
    }
}

actual fun createAcceleratedBlurStrategy(): BlurStrategy {
    println("Web Platform Capabilities: ${WebPlatform.getPlatformCapabilities()}")

    return when {
        hasWebGL2 -> WebGL2AcceleratedBlurStrategy()
        hasWebGL -> WebGLAcceleratedBlurStrategy()
        else -> OptimizedCanvasBlurStrategy()
    }
}

class WebGL2BlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = hasWebGL2
    override val performanceLevel = BlurPerformance.NATIVE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return if (isAvailable) {
            bitmap.webGL2Blur(radius)
        } else {
            throw UnsupportedOperationException("WebGL2 not available in this browser")
        }
    }
}

class WebGL2AcceleratedBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = hasWebGL2
    override val performanceLevel = BlurPerformance.ACCELERATED

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.webGL2AcceleratedBlur(radius)
    }
}

class WebGLBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = hasWebGL
    override val performanceLevel = BlurPerformance.NATIVE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return if (isAvailable) {
            bitmap.webGLBlur(radius)
        } else {
            throw UnsupportedOperationException("WebGL not available in this browser")
        }
    }
}

class WebGLAcceleratedBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = hasWebGL
    override val performanceLevel = BlurPerformance.ACCELERATED

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.webGLAcceleratedBlur(radius)
    }
}

class OffscreenCanvasBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = hasOffscreenCanvas
    override val performanceLevel = BlurPerformance.SOFTWARE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return if (isAvailable) {
            bitmap.offscreenCanvasBlur(radius)
        } else {
            throw UnsupportedOperationException("OffscreenCanvas not available in this browser")
        }
    }
}

class CanvasBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = true
    override val performanceLevel = BlurPerformance.SOFTWARE

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.canvasBlur(radius)
    }
}

class OptimizedCanvasBlurStrategy : BlurStrategy {
    override val isAvailable: Boolean = true
    override val performanceLevel = BlurPerformance.ACCELERATED

    override fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return bitmap.optimizedCanvasBlur(radius)
    }
}

// Platform-specific blur implementations

private fun ImageBitmap.webGL2Blur(radius: Float): ImageBitmap {
    return try {
        this.canvasBlur(radius)
    } catch (e: Exception) {
        println(e)
        this.canvasBlur(radius)
    }
}

private fun ImageBitmap.webGL2AcceleratedBlur(radius: Float): ImageBitmap {
    return this.webGL2Blur(radius * 0.8f)
}

private fun ImageBitmap.webGLBlur(radius: Float): ImageBitmap {
    return try {
        this.canvasBlur(radius)
    } catch (e: Exception) {
        println(e)
        this.canvasBlur(radius)
    }
}

private fun ImageBitmap.webGLAcceleratedBlur(radius: Float): ImageBitmap {
    return this.webGLBlur(radius * 0.8f)
}

private fun executeCanvasBlur(bitmap: ImageBitmap, width: Int, height: Int, radius: Float, hasCSSFilters: Boolean): JsImageData? {
    return js("""
            (function() {
                try {
                    var canvas = document.createElement('canvas');
                    canvas.width = ${width};
                    canvas.height = ${height};
                    var ctx = canvas.getContext('2d');
                    
                    // Draw the bitmap to canvas (this requires the bitmap to be a proper ImageBitmap)
                    ctx.drawImage(arguments[0], 0, 0);
                    
                    if (${hasCSSFilters}) {
                        ctx.filter = 'blur(${radius}px)';
                        ctx.drawImage(canvas, 0, 0);
                    }
                    
                    return ctx.getImageData(0, 0, ${width}, ${height});
                } catch (e) {
                    console.error('Canvas blur failed:', e);
                    return null;
                }
            })(arguments[0])
        """) as? JsImageData
}

private fun executeOffscreenCanvasBlur(bitmap: ImageBitmap, width: Int, height: Int, radius: Float, hasCSSFilters: Boolean): JsImageData? {
    return js("""
            (function() {
                try {
                    if (typeof OffscreenCanvas === 'undefined') return null;
                    
                    var offscreenCanvas = new OffscreenCanvas(${width}, ${height});
                    var ctx = offscreenCanvas.getContext('2d');
                    ctx.drawImage(arguments[0], 0, 0);
                    
                    if (${hasCSSFilters}) {
                        ctx.filter = 'blur(${radius}px)';
                        ctx.drawImage(offscreenCanvas, 0, 0);
                    }
                    
                    return ctx.getImageData(0, 0, ${width}, ${height});
                } catch (e) {
                    console.error('OffscreenCanvas blur failed:', e);
                    return null;
                }
            })(arguments[0])
        """) as? JsImageData
}

private fun ImageBitmap.offscreenCanvasBlur(radius: Float): ImageBitmap {
    if (!hasOffscreenCanvas) {
        return this.canvasBlur(radius)
    }

    return try {
        val imageData = executeOffscreenCanvasBlur(this, width, height, radius, hasCSSFilters)

        if (imageData != null) {
            createImageBitmapFromImageData(imageData)
        } else {
            this.manualCanvasBlur(radius)
        }
    } catch (e: Exception) {
        println(e)
        this.canvasBlur(radius)
    }
}

private fun ImageBitmap.canvasBlur(radius: Float): ImageBitmap {
    return try {
        val imageData = executeCanvasBlur(this, width, height, radius, hasCSSFilters)

        if (imageData != null) {
            createImageBitmapFromImageData(imageData)
        } else {
            this.manualCanvasBlur(radius)
        }
    } catch (e: Exception) {
        println(e)
        this
    }
}

private fun ImageBitmap.optimizedCanvasBlur(radius: Float): ImageBitmap {
    val adjustedRadius = (radius * 0.6f).coerceAtLeast(1f)
    val passes = if (radius > 10f) 2 else 1

    var result = this
    repeat(passes) {
        result = result.canvasBlur(adjustedRadius)
    }

    return result
}

private fun ImageBitmap.manualCanvasBlur(radius: Float): ImageBitmap {
    val blurred = ImageBitmap(width, height)
    val canvas = Canvas(blurred)

    try {
        val pixelMap = this.toPixelMap()
        val sourcePixels = Array(height) { y ->
            IntArray(width) { x ->
                pixelMap[x, y].toArgb()
            }
        }

        val kernelSize = (radius * 1.2).toInt().coerceAtLeast(3).coerceAtMost(15) or 1
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
        drawWebFallbackPattern(canvas, width, height)
    }

    return blurred
}

private fun createImageBitmapFromImageData(imageData: JsImageData): ImageBitmap {
    return try {
        val bitmap = ImageBitmap(imageData.width, imageData.height)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        for (y in 0 until imageData.height) {
            for (x in 0 until imageData.width) {
                val index = (y * imageData.width + x) * 4

                val r = (imageData.data[index] as JsNumber).toInt() / 255f
                val g = (imageData.data[index + 1] as JsNumber).toInt() / 255f
                val b = (imageData.data[index + 2] as JsNumber).toInt() / 255f
                val a = (imageData.data[index + 3] as JsNumber).toInt() / 255f

                paint.color = Color(
                    red = r.coerceIn(0f, 1f),
                    green = g.coerceIn(0f, 1f),
                    blue = b.coerceIn(0f, 1f),
                    alpha = a.coerceIn(0f, 1f)
                )

                canvas.drawRect(
                    Rect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat()),
                    paint
                )
            }
        }

        bitmap
    } catch (e: Exception) {
        println(e)
        ImageBitmap(1, 1)
    }
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

private fun drawWebFallbackPattern(canvas: Canvas, width: Int, height: Int) {
    val paint = Paint().apply {
        isAntiAlias = true
    }

    for (y in 0 until height step 2) {
        for (x in 0 until width step 2) {
            val gray = ((x + y) * 180 / (width + height)).coerceIn(0, 180) / 255f + 0.3f
            paint.color = Color(gray, gray, gray, 0.8f)
            canvas.drawRect(
                Rect(x.toFloat(), y.toFloat(), (x + 2).toFloat(), (y + 2).toFloat()),
                paint
            )
        }
    }
}
