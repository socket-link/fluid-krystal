package link.socket.krystal.api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import link.socket.krystal.engine.LocalKrystalContainerContext
import link.socket.krystal.engine.toKrystalSurfaceStyle
import link.socket.krystal.krystalizedSurface

private var surfaceCounter = 0

@Composable
actual fun GlassSurface(
    style: GlassStyle,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val krystalContext = LocalKrystalContainerContext.current

    if (krystalContext != null) {
        // Inside a KrystalContainer — use the full renderer pipeline
        KrystalRenderedGlassSurface(style, modifier, content)
    } else {
        // Standalone — use tinted translucent background
        StandaloneGlassSurface(style, modifier, content)
    }
}

@Composable
private fun KrystalRenderedGlassSurface(
    style: GlassStyle,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val krystalContext = LocalKrystalContainerContext.current ?: return
    val surfaceId = remember { "glass_surface_${surfaceCounter++}" }
    val surfaceStyle = style.toKrystalSurfaceStyle()

    // Register this surface's style with the container context
    DisposableEffect(surfaceId, style) {
        krystalContext.registerSurface(surfaceId, style)
        onDispose {
            krystalContext.unregisterSurface(surfaceId)
        }
    }

    val hazeState = krystalContext.hazeState.collectAsState()
    val curveState = krystalContext.curveState.collectAsState()

    val shape = RoundedCornerShape(style.cornerRadius)
    Box(
        modifier = modifier
            .shadow(style.elevation.shadowElevation, shape)
            .krystalizedSurface(
                hazeState = hazeState.value,
                curveState = curveState.value,
                surfaceStyle = surfaceStyle,
            ),
    ) {
        content()
    }
}

@Composable
private fun StandaloneGlassSurface(
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
