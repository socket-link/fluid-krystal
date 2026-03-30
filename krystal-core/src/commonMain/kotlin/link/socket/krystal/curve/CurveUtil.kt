@file:Suppress("NOTHING_TO_INLINE")

package link.socket.krystal.curve

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.takeOrElse
import kotlin.math.ceil

internal enum class CurveTraversableNodeKeys {
    Effect,
    Source,
}

internal inline val Offset.orZero: Offset get() = takeOrElse { Offset.Zero }

internal inline fun <T> unsynchronizedLazy(
    noinline initializer: () -> T,
): Lazy<T> = lazy(mode = LazyThreadSafetyMode.NONE, initializer)

internal fun ceil(size: Size): Size = Size(width = ceil(size.width), height = ceil(size.height))
