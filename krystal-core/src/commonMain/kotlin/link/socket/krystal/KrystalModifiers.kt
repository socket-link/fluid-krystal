package link.socket.krystal

import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import link.socket.krystal.curve.CurveState
import link.socket.krystal.curve.CurveStyle
import link.socket.krystal.curve.curveEffect
import link.socket.krystal.curve.curveSource
import link.socket.krystal.engine.ContentAnalysis
import link.socket.krystal.engine.LocalKrystalContainerContext

fun Modifier.krystalizedContainer(
    hazeState: HazeState,
    curveState: CurveState,
    containerStyle: KrystalStyle.Container,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "krystalizedContainer"
    }
) {
    this
        .hazeSource(
            state = hazeState,
        )
        .hazeEffect(
            state = hazeState,
            style = containerStyle.containerHazeStyle,
        ) {
            progressive = HazeProgressive.verticalGradient(
                easing = CubicBezierEasing(0.3f, 0.0f, 0.1f, 0.4f),
                startIntensity = 0.9f,
                endIntensity = 0f,
            )
        }
        .curveSource(
            state = curveState,
        )
}

fun Modifier.krystalizedSurface(
    hazeState: HazeState,
    curveState: CurveState,
    surfaceStyle: KrystalStyle.Surface,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "krystalizedSurface"
        properties["hazeState"] = hazeState
        properties["curveState"] = curveState
        properties["surfaceStyle"] = surfaceStyle
    },
) {
    val shape = RoundedCornerShape(surfaceStyle.cornerRadius)

    this
        .clip(shape)
        .shadow(1.dp)
        .hazeEffect(
            state = hazeState,
            style = surfaceStyle.surfaceHazeStyle,
        ) {
            progressive = HazeProgressive.RadialGradient(
                easing = CubicBezierEasing(0.2f, 0.0f, 0.4f, 0.4f),
                centerIntensity = 0.2f,
                radius = surfaceStyle.blurRadius.value,
                radiusIntensity = 1f,
            )
        }
        .curveEffect(
            curveState = curveState,
            style = CurveStyle.Unspecified,
        )
        .border(
            width = surfaceStyle.borderThickness * 3,
            color = surfaceStyle.borderColor.copy(alpha = 0.15f),
            shape = shape
        )
        .border(
            width = surfaceStyle.borderThickness * 2,
            color = surfaceStyle.borderColor.copy(alpha = 0.35f),
            shape = shape
        )
        .border(
            width = surfaceStyle.borderThickness,
            color = surfaceStyle.borderColor.copy(alpha = 0.55f),
            shape = shape
        )
        .border(
            width = surfaceStyle.borderThickness * 3,
            color = surfaceStyle.borderColor.copy(alpha = 0.7f),
            shape = shape
        )
        .border(
            width = surfaceStyle.borderThickness * 2,
            color = surfaceStyle.borderColor.copy(alpha = 0.85f),
            shape = shape
        )
        .border(
            width = surfaceStyle.borderThickness,
            color = surfaceStyle.borderColor.copy(alpha = 1f),
            shape = shape
        )
}

// TODO: Add back
fun Modifier.autoAnalyzeBackground(
    onContentAnalyzed: (ContentAnalysis) -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "autoAnalyzeBackground"
    }
) {
    val captureEngine = LocalKrystalContainerContext.current?.contentCaptureEngine ?: return@composed this
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

// TODO: Add back
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
