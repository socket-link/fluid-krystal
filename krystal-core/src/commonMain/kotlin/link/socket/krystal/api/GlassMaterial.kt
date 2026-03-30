package link.socket.krystal.api

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

fun Modifier.glassMaterial(
    style: GlassStyle = GlassStyle.clear(),
): Modifier {
    val shape = RoundedCornerShape(style.cornerRadius)
    return this
        .shadow(style.elevation.shadowElevation, shape)
        .clip(shape)
        .background(style.effectiveBackgroundColor, shape)
}
