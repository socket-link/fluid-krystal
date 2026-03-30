package link.socket.krystal.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun GlassSurface(
    style: GlassStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
