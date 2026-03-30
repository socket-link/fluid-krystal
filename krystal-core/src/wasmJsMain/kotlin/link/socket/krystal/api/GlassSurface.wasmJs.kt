package link.socket.krystal.api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

@Composable
actual fun GlassSurface(
    style: GlassStyle,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(style.cornerRadius)
    Box(
        modifier = modifier
            .shadow(style.elevation.shadowElevation, shape)
            .clip(shape)
            .background(style.effectiveBackgroundColor, shape),
    ) {
        content()
    }
}
