@file:OptIn(ExperimentalTime::class)

package link.socket.krystal

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import link.socket.krystal.engine.DrawingHints
import link.socket.krystal.engine.ContentInfo
import link.socket.krystal.engine.KrystalContainerContext
import link.socket.krystal.engine.LocalKrystalContainerContext
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val baseKrystalContainerStyle = KrystalStyle.Container()
private val baseKrystalSurfaceStyle = KrystalStyle.Surface()

@Composable
fun KrystalContainer(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    baseContainerStyle: KrystalStyle.Container = baseKrystalContainerStyle,
    baseSurfaceStyle: KrystalStyle.Surface = baseKrystalSurfaceStyle,
    backgroundContent: @Composable BoxScope.() -> Unit,
    foregroundContent: @Composable BoxScope.() -> Unit,
) {
    var containerBounds by remember { mutableStateOf(Rect.Zero) }
    var lastProcessedTime by remember { mutableStateOf(0L) }
    var lastScrollValue by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val backgroundHazeState = rememberHazeState()

    val krystalContainerContext = remember {
        mutableStateOf(
            KrystalContainerContext.newInstance(
                scope = scope,
                baseHazeState = backgroundHazeState,
                baseKrystalContainerStyle = baseContainerStyle,
                baseKrystalSurfaceStyle = baseSurfaceStyle,
            )
        )
    }

    val layoutInfoChannel = remember { Channel<ContentInfo>(Channel.UNLIMITED) }

    LaunchedEffect(backgroundHazeState) {
        krystalContainerContext.value = krystalContainerContext.value.copy(
            baseHazeState = backgroundHazeState,
        )
    }

    LaunchedEffect(scrollState.value) {
        val currentScrollValue = scrollState.value
        if (currentScrollValue != lastScrollValue) {
            println("🔄 Scroll detected: $lastScrollValue -> $currentScrollValue")
            lastScrollValue = currentScrollValue

            krystalContainerContext.value.contentCaptureEngine.forceContentUpdate()
            KrystalDebug.forceUpdate()
        }
    }

    LaunchedEffect(Unit) {
        val collectedInfo = mutableListOf<ContentInfo>()
        while (true) {
            var item = layoutInfoChannel.tryReceive().getOrNull()
            while (item != null) {
                collectedInfo.add(item)
                item = layoutInfoChannel.tryReceive().getOrNull()
            }

            val currentTime = Clock.System.now().toEpochMilliseconds()
            val hasEnoughInfo = collectedInfo.isNotEmpty()
            val hasValidBounds = containerBounds != Rect.Zero
            val enoughTimeHasPassed = currentTime - lastProcessedTime > 100

            if (hasEnoughInfo && hasValidBounds && enoughTimeHasPassed) {
                println("🔄 Processing ${collectedInfo.size} layout items")
                krystalContainerContext.value.contentCaptureEngine.discoverContent(
                    containerBounds,
                    collectedInfo.toList(),
                )
                collectedInfo.clear()
                lastProcessedTime = currentTime
            }
            delay(30)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (containerBounds != Rect.Zero) {
                krystalContainerContext.value.contentCaptureEngine.forceContentUpdate()
                KrystalDebug.forceUpdate()
            }
        }
    }

    CompositionLocalProvider(LocalKrystalContainerContext provides krystalContainerContext.value) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val windowOffset = coordinates.localToWindow(Offset.Zero)
                    val newBounds = Rect(
                        offset = windowOffset,
                        size = coordinates.size.toSize()
                    )
                    
                    val centerDistance = (newBounds.center - containerBounds.center).getDistance()
                    val sizeChanged = abs(newBounds.width - containerBounds.width) +
                                     abs(newBounds.height - containerBounds.height)
                    
                    if (centerDistance > 0.5f || sizeChanged > 1.0f) {
                        containerBounds = newBounds
                        println("📏 Container bounds updated: $newBounds")
                        KrystalDebug.forceUpdate()
                    }

                    val scrollOffset = scrollState?.value ?: 0
                    val adjustedOffset = Offset(
                        windowOffset.x,
                        windowOffset.y + scrollOffset
                    )
                    val adjustedBounds = Rect(
                        offset = adjustedOffset,
                        size = coordinates.size.toSize()
                    )
                    
                    val layoutInfo = ContentInfo(
                        bounds = adjustedBounds,
                        parentBounds = null,
                        drawingHints = DrawingHints(
                            backgroundColor = null,
                            hasComplexBackground = false,
                            isTextContent = false
                        )
                    )
                    layoutInfoChannel.trySend(layoutInfo)
                }
                .drawingListener { operations ->
                    if (operations.isNotEmpty()) {
                        krystalContainerContext.value.contentCaptureEngine.processDrawingOperations(
                            containerBounds,
                            operations,
                        )
                    }
                },
            content = {
                val hazeState = LocalKrystalContainerContext.current.baseHazeState

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                ) {
                    backgroundContent()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(100.dp)
                            .krystalizedContainer(LocalKrystalContainerContext.current)
                    )

                    foregroundContent()
                }
            }
        )
    }
}
