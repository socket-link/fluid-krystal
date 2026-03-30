@file:OptIn(ExperimentalTime::class)

package link.socket.krystal

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import link.socket.krystal.engine.DrawingHints
import link.socket.krystal.engine.ContentInfo
import link.socket.krystal.engine.LocalKrystalContainerContext
import link.socket.krystal.engine.rememberKrystalContainerContext
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun KrystalContainer(
    scrollState: ScrollState,
    backgroundContent: @Composable BoxScope.() -> Unit,
    foregroundContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    baseContainerStyle: KrystalStyle.Container = baseKrystalContainerStyle(),
) {
    val krystalContainerContext = rememberKrystalContainerContext(baseContainerStyle)
    val layoutInfoChannel = remember { Channel<ContentInfo>(Channel.UNLIMITED) }

    val hazeState = krystalContainerContext.hazeState.collectAsState()
    val curveState = krystalContainerContext.curveState.collectAsState()
    val containerStyle = krystalContainerContext.containerStyle.collectAsState()

    var containerBounds by remember { mutableStateOf(Rect.Zero) }
    var lastProcessedTime by remember { mutableStateOf(0L) }
    var lastScrollValue by remember { mutableStateOf(0) }

    LaunchedEffect(scrollState.value) {
        val currentScrollValue = scrollState.value
        if (currentScrollValue != lastScrollValue) {
            lastScrollValue = currentScrollValue

            krystalContainerContext.contentCaptureEngine.forceContentUpdate()
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
                krystalContainerContext.contentCaptureEngine.discoverContent(
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
                krystalContainerContext.contentCaptureEngine.forceContentUpdate()
                KrystalDebug.forceUpdate()
            }
        }
    }

    CompositionLocalProvider(LocalKrystalContainerContext provides krystalContainerContext) {
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
                        krystalContainerContext.contentCaptureEngine.processDrawingOperations(
                            containerBounds,
                            operations,
                        )
                    }
                },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .krystalizedContainer(
                            hazeState = hazeState.value,
                            curveState = curveState.value,
                            containerStyle = containerStyle.value,
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        backgroundContent()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        foregroundContent()
                    }
                }
            }
        )
    }
}
