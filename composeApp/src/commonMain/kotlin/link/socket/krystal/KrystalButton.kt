package link.socket.krystal

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import link.socket.krystal.blur.BackdropBlurContainer
import link.socket.krystal.engine.ContentAnalysis
import kotlin.math.abs
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
    val krystalContext = LocalKrystalContext.current
    var buttonBounds by remember { mutableStateOf(Rect.Zero) }

    val buttonId = remember { "krystal_button_${Clock.System.now().toEpochMilliseconds()}_${kotlin.random.Random.nextInt()}" }

    LaunchedEffect(buttonId, buttonBounds, krystalContext) {
        if (buttonBounds != Rect.Zero) {
            val buttonContext = KrystalContext(buttonBounds, krystalContext.contentCaptureEngine)
            KrystalDebugRegistry.registerButton(buttonId, buttonContext)
            println("🔘 Registered button $buttonId with bounds $buttonBounds")
        }
    }

    DisposableEffect(buttonId) {
        onDispose {
            KrystalDebugRegistry.unregisterButton(buttonId)
            println("🔘 Unregistered button $buttonId")
        }
    }

    val analysis = remember(buttonBounds, krystalContext.contentCaptureEngine.debugUpdateTrigger) {
        if (buttonBounds != Rect.Zero) {
            krystalContext.contentCaptureEngine.analyzeRegion(buttonBounds)
        } else {
            ContentAnalysis()
        }
    }

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    var isPressed by remember { mutableStateOf(false) }

    val style = remember(enabled) {
        KrystalStyle()
    }

    val effectiveOpacity = if (isPressed) {
        style.backgroundOpacity * 2f
    } else {
        style.backgroundOpacity
    }

    val pressedStyle = style.copy(backgroundOpacity = effectiveOpacity)

    BackdropBlurContainer(
        modifier = modifier,
    ) {
        Button(
            modifier = Modifier
                .krystalized(pressedStyle)
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
                            val updatedContext = KrystalContext(buttonBounds, krystalContext.contentCaptureEngine)
                            KrystalDebugRegistry.registerButton(buttonId, updatedContext)
                            println("🔘 Updated button $buttonId position: $buttonBounds")
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
}
