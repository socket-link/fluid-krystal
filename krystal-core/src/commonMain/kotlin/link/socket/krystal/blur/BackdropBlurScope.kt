package link.socket.krystal.blur

import androidx.compose.ui.graphics.ImageBitmap

interface BackdropBlurScope {
    val blurredBackground: ImageBitmap?
}

class BackdropBlurScopeImpl(
    override val blurredBackground: ImageBitmap?
) : BackdropBlurScope
