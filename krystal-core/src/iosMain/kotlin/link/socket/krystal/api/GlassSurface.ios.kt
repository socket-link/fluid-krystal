package link.socket.krystal.api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import link.socket.krystal.platform.NativeGlassEffectView

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
            .clip(shape),
    ) {
        // Native UIVisualEffectView provides the glass backdrop blur
        NativeGlassEffectView(
            style = style,
            modifier = Modifier.matchParentSize(),
        )

        // Compose content is rendered on top of the glass effect
        content()
    }
}
