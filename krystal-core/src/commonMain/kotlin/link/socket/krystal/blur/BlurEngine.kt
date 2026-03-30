package link.socket.krystal.blur

import androidx.compose.ui.graphics.ImageBitmap

class BlurEngine private constructor(private val strategy: BlurStrategy) {

    companion object {
        val instance: BlurEngine by lazy {
            val availableStrategies = listOfNotNull(
                try {
                    createNativeBlurStrategy().takeIf { it.isAvailable }
                } catch (e: Exception) {
                    println(e)
                    null
                },
                try {
                    createAcceleratedBlurStrategy().takeIf { it.isAvailable }
                } catch (e: Exception) {
                    println(e)
                    null
                },
                SoftwareBlurStrategy()
            )

            val bestStrategy = availableStrategies.minByOrNull {
                it.performanceLevel.ordinal
            } ?: SoftwareBlurStrategy()

            println("BlurEngine initialized with: ${bestStrategy::class.simpleName}")
            BlurEngine(bestStrategy)
        }
    }

    fun blur(bitmap: ImageBitmap, radius: Float): ImageBitmap {
        return strategy.blur(bitmap, radius)
    }
}
