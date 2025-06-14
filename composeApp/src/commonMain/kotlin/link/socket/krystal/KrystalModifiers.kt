package link.socket.krystal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay

/**
 * Custom modifier that applies Krystal visual effects.
 * This encapsulates the actual rendering logic for Krystal materials.
 * Think of this as the "Krystal shader" that creates the visual effect.
 */
fun Modifier.krystalEffect(
    alpha: Float = 0.8f,
    shadowIntensity: Float = 0.5f,
    tintColor: Color = Color.Transparent
): Modifier = this.drawWithCache {
    onDrawBehind {
        // Basic Krystal effect implementation
        // This is where we'd implement the sophisticated shader effects

        // Background blur simulation (simplified for MVP)
        drawRect(
            color = Color.White.copy(alpha = alpha * 0.3f),
            topLeft = Offset.Zero,
            size = size
        )

        // Subtle tint based on background content
        if (tintColor != Color.Transparent) {
            drawRect(
                color = tintColor.copy(alpha = 0.1f),
                topLeft = Offset.Zero,
                size = size
            )
        }

        // Shadow effect (intensity based on content contrast)
        drawRect(
            color = Color.Black.copy(alpha = shadowIntensity * 0.2f),
            topLeft = Offset(0f, 2.dp.toPx()),
            size = Size(size.width, size.height - 2.dp.toPx())
        )
    }
}

/**
 * Simple modifier that enables glass components to automatically analyze
 * their background without manual content registration.
 */
fun Modifier.autoAnalyzeBackground(
    onContentAnalyzed: (ContentAnalysis) -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "autoAnalyzeBackground"
    }
) {
    val captureEngine = LocalKrystalContext.current.contentCaptureEngine
    var lastBounds by remember { mutableStateOf<Rect?>(null) }
    var lastAnalysis by remember { mutableStateOf(ContentAnalysis()) }

    // Set up a LaunchedEffect to periodically check for updates
    LaunchedEffect(Unit) {
        while(true) {
            lastBounds?.let { bounds ->
                val currentAnalysis = captureEngine.analyzeRegion(bounds)
                if (currentAnalysis != lastAnalysis) {
                    lastAnalysis = currentAnalysis
                    onContentAnalyzed(currentAnalysis)
                }
            }
            delay(100) // Check every 100ms
        }
    }

    this.onGloballyPositioned { coordinates ->
        val bounds = Rect(
            offset = coordinates.localToWindow(Offset.Zero),
            size = coordinates.size.toSize()
        )
        lastBounds = bounds

        // Initial analysis
        val analysis = captureEngine.analyzeRegion(bounds)
        if (analysis != lastAnalysis) {
            lastAnalysis = analysis
            onContentAnalyzed(analysis)
        }
    }
}

/**
 * Performance-optimized analysis with debouncing for smooth interactions.
 */
@Composable
fun rememberDebouncedMultiplatformAnalysis(
    analysis: ContentAnalysis,
    delayMillis: Long = 16L
): ContentAnalysis {
    var debouncedAnalysis by remember { mutableStateOf(analysis) }

    LaunchedEffect(analysis) {
        delay(delayMillis)
        debouncedAnalysis = analysis
    }

    return debouncedAnalysis
}
