package link.socket.krystal

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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Example of a Krystal-aware component that adapts based on visual context.
 * Notice how it doesn't need special modifiers or complex setup - it just
 * consumes the context provided by the [KrystalContainer].
 */
@OptIn(ExperimentalTime::class)
@Composable
fun KrystalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    // Get the Krystal context
    val krystalContext = LocalKrystalContext.current
    var buttonBounds by remember { mutableStateOf(Rect.Zero) }

    // Create a unique ID for this button
    val buttonId = remember { "krystal_button_${Clock.System.now().toEpochMilliseconds()}_${kotlin.random.Random.nextInt()}" }

    // Register this button instance and update when bounds change
    LaunchedEffect(buttonId, buttonBounds, krystalContext) {
        if (buttonBounds != Rect.Zero) {
            val buttonContext = KrystalContext(buttonBounds, krystalContext.contentCaptureEngine)
            KrystalDebugRegistry.registerButton(buttonId, buttonContext)
            println("🔘 Registered button $buttonId with bounds $buttonBounds")
        }
    }

    // Cleanup when the composable is disposed
    DisposableEffect(buttonId) {
        onDispose {
            KrystalDebugRegistry.unregisterButton(buttonId)
            println("🔘 Unregistered button $buttonId")
        }
    }

    // Create an updated context specific to this button's bounds
    val buttonContext = remember(buttonBounds, krystalContext) {
        if (buttonBounds != Rect.Zero) {
            KrystalContext(buttonBounds, krystalContext.contentCaptureEngine)
        } else {
            krystalContext
        }
    }

    // Analyze the region for this button more frequently
    val analysis = remember(buttonBounds, krystalContext.contentCaptureEngine.debugUpdateTrigger) {
        if (buttonBounds != Rect.Zero) {
            krystalContext.contentCaptureEngine.analyzeRegion(buttonBounds)
        } else {
            ContentAnalysis() // Default analysis
        }
    }

    // Determine if content is dark to adjust button styling
    val isDarkContent = analysis.isDarkContent
    val buttonColors = if (isDarkContent) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val newBounds = Rect(
                    offset = coordinates.localToWindow(Offset.Zero),
                    size = coordinates.size.toSize()
                )
                // Update bounds even for small changes
                if ((newBounds.center - buttonBounds.center).getDistance() > 0.1f ||
                    kotlin.math.abs(newBounds.width - buttonBounds.width) > 0.1f ||
                    kotlin.math.abs(newBounds.height - buttonBounds.height) > 0.1f) {
                    buttonBounds = newBounds
                    
                    // Update the registry immediately when button position changes
                    if (buttonBounds != Rect.Zero) {
                        val updatedContext = KrystalContext(buttonBounds, krystalContext.contentCaptureEngine)
                        KrystalDebugRegistry.registerButton(buttonId, updatedContext)
                        println("🔘 Updated button $buttonId position: $buttonBounds")
                    }
                }
            },
        enabled = enabled,
        colors = buttonColors,
        content = content
    )
}
