package link.socket.krystal

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import link.socket.krystal.engine.ContentAnalysis

fun Modifier.krystalized(
    style: KrystalStyle = KrystalStyle()
): Modifier {
    val shape = RoundedCornerShape(style.cornerRadius)
    val brush = createGlassGradient(style)

    return this
        .clip(shape)
        .background(
            brush = brush,
            shape = shape
        )
        .border(
            width = 1.dp,
            color = style.tintColor.copy(alpha = style.borderOpacity),
            shape = shape
        )
}

private fun createGlassGradient(style: KrystalStyle): Brush {
    val baseColor = style.tintColor.copy(alpha = style.backgroundOpacity)

    val reflectionLayer = style.tintColor.copy(alpha = 0.1f)
    val refractionLayer = style.tintColor.copy(alpha = 0.05f)

    return Brush.verticalGradient(
        colors = listOf(
            reflectionLayer,
            baseColor,
            refractionLayer,
            baseColor.copy(alpha = style.backgroundOpacity * 0.5f)
        )
    )
}

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

    LaunchedEffect(Unit) {
        while(true) {
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
