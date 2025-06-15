package link.socket.krystal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.toSize
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import link.socket.krystal.engine.ContentAnalysis
import link.socket.krystal.engine.KrystalContainerContext
import link.socket.krystal.engine.KrystalSurfaceContext
import link.socket.krystal.engine.LocalKrystalContainerContext

fun Modifier.krystalizedContainer(
    context: KrystalContainerContext,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "krystalizedSurface"
        properties["context"] = context
    }
) {
    val hazeState = context.baseHazeState

    this.hazeEffect(
        state = hazeState,
        style = context.baseKrystalStyle.hazeStyle,
    ) {
        progressive = HazeProgressive.horizontalGradient(
            easing = FastOutSlowInEasing,
            startIntensity = 1f,
            endIntensity = .25f,
        )
    }
}

fun Modifier.krystalizedSurface(
    context: KrystalSurfaceContext,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "krystalizedSurface"
        properties["context"] = context
    }
) {
    val shape = RoundedCornerShape(context.surfaceStyle.cornerRadius)
    val hazeState = context.surfaceHazeState

    this.clip(shape)
        .hazeEffect(
            state = hazeState,
            style = context.surfaceStyle.hazeStyle,
        ) {
            progressive = HazeProgressive.horizontalGradient(
                easing = FastOutSlowInEasing,
                startIntensity = 1f,
                endIntensity = .25f,
            )
        }
        .border(
            width = context.surfaceStyle.borderThickness,
            color = context.surfaceStyle.borderColor,
            shape = shape
        )
}

fun Modifier.autoAnalyzeBackground(
    onContentAnalyzed: (ContentAnalysis) -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "autoAnalyzeBackground"
    }
) {
    val captureEngine = LocalKrystalContainerContext.current.contentCaptureEngine
    var lastBounds by remember { mutableStateOf<Rect?>(null) }
    var lastAnalysis by remember { mutableStateOf(ContentAnalysis()) }

    LaunchedEffect(Unit) {
        while (true) {
            lastBounds?.let { bounds ->
                val currentAnalysis = captureEngine.analyzeRegion(bounds)
                if (currentAnalysis != lastAnalysis) {
                    lastAnalysis = currentAnalysis
                    onContentAnalyzed(currentAnalysis)
                }
            }
            delay(100)
        }
    }

    this.onGloballyPositioned { coordinates ->
        val bounds = Rect(
            offset = coordinates.localToWindow(Offset.Zero),
            size = coordinates.size.toSize()
        )
        lastBounds = bounds

        val analysis = captureEngine.analyzeRegion(bounds)
        if (analysis != lastAnalysis) {
            lastAnalysis = analysis
            onContentAnalyzed(analysis)
        }
    }
}

@Composable
fun rememberDebouncedContentAnalysis(
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
