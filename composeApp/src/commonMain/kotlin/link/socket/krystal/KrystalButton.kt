package link.socket.krystal

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import link.socket.krystal.engine.ContentAnalysis
import link.socket.krystal.engine.LocalKrystalContainerContext
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun KrystalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val krystalContext = LocalKrystalContainerContext.current
    var buttonBounds by remember { mutableStateOf(Rect.Zero) }

    val surfaceId = remember { "krystal_button_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt()}" }
    val currentSurfaceContext = krystalContext.contextStateEngine.getSurfaceContextFlow(surfaceId).collectAsStateWithLifecycle()

    LaunchedEffect(surfaceId, buttonBounds) {
        if (buttonBounds != Rect.Zero) {
            KrystalDebug.registerButton(surfaceId, buttonBounds)
            println("🔘 Registered button $surfaceId with bounds $buttonBounds")
        }
    }

    DisposableEffect(surfaceId) {
        onDispose {
            KrystalDebug.unregisterButton(surfaceId)
            krystalContext.contextStateEngine.unregisterSurfaceContext(surfaceId)
            println("🔘 Unregistered button $surfaceId")
        }
    }

    val analysis = remember(buttonBounds, krystalContext.contentCaptureEngine.debugUpdateTrigger) {
        if (buttonBounds != Rect.Zero) {
            krystalContext.contentCaptureEngine.analyzeRegion(buttonBounds)
        } else {
            ContentAnalysis()
        }
    }

    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        with(currentSurfaceContext.value.surfaceStyle) {
            krystalContext.contextStateEngine.updateSurfaceContext(
                id = surfaceId,
                surfaceStyle = copy(
                    backgroundOpacity = if (isPressed) {
                        backgroundOpacity * 2f
                    } else {
                        backgroundOpacity
                    }
                ),
            )
        }
    }

    val buttonColors = buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.Black,
    )

    Button(
        modifier = modifier
            .krystalizedSurface(
                hazeState = currentSurfaceContext.value.surfaceHazeState,
                surfaceStyle = currentSurfaceContext.value.surfaceStyle,
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
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
                        println("🔘 Updated button $surfaceId position: $buttonBounds")
                    }
                }
            },
        colors = buttonColors,
        enabled = enabled,
        interactionSource = null,
        onClick = onClick,
        content = content,
    )
}
