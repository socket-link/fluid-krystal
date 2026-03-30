package link.socket.krystal

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.flow.map
import link.socket.krystal.engine.InteractionType
import link.socket.krystal.engine.LocalKrystalContainerContext
import kotlin.math.abs

private var buttonCounter = 0

@Composable
fun KrystalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    surfaceKey: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val krystalContext = requireNotNull(LocalKrystalContainerContext.current) {
        "KrystalButton must be used inside a KrystalContainer"
    }

    val surfaceId = remember { surfaceKey ?: "krystal_button_${buttonCounter++}" }
    var buttonBounds by remember { mutableStateOf(Rect.Zero) }

    val hazeState = krystalContext.hazeState.collectAsState()
    val curveState = krystalContext.curveState.collectAsState()

    val surfaceStyle = krystalContext.surfaceStyleCache.map { cache ->
        cache[surfaceId] ?: KrystalStyle.Surface.EMPTY
    }.collectAsState(KrystalStyle.Surface.EMPTY)

    LaunchedEffect(surfaceId, buttonBounds) {
        if (buttonBounds != Rect.Zero) {
            KrystalDebug.registerButton(surfaceId, buttonBounds)
        }
    }

    DisposableEffect(surfaceId) {
        onDispose {
            KrystalDebug.unregisterButton(surfaceId)
        }
    }

    val buttonColors = buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.Black,
    )

    Button(
        modifier = modifier
            .krystalizedSurface(
                hazeState = hazeState.value,
                curveState = curveState.value,
                surfaceStyle = surfaceStyle.value,
            )
            .onGloballyPositioned { coordinates ->
                val newBounds = Rect(
                    offset = coordinates.localToWindow(Offset.Zero),
                    size = coordinates.size.toSize()
                )
                if ((newBounds.center - buttonBounds.center).getDistance() > 0.1f ||
                    abs(newBounds.width - buttonBounds.width) > 0.1f ||
                    abs(newBounds.height - buttonBounds.height) > 0.1f
                ) {
                    buttonBounds = newBounds

                    if (buttonBounds != Rect.Zero) {
                        KrystalDebug.registerButton(surfaceId, buttonBounds)
                    }
                }
            },
        colors = buttonColors,
        enabled = enabled,
        interactionSource = null,
        onClick = {
            onClick()
            krystalContext.contentCaptureEngine.recordInteraction(buttonBounds, InteractionType.CLICK)
        },
        content = content,
    )
}
