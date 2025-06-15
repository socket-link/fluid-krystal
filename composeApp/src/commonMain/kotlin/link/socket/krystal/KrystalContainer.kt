package link.socket.krystal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import link.socket.krystal.engine.DrawingHints
import link.socket.krystal.engine.EnhancedLayoutInfo
import link.socket.krystal.engine.KrystalContentCaptureEngine
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun KrystalContainer(
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.ScrollState? = null,
    foregroundContent: @Composable BoxScope.() -> Unit,
    backgroundContent: @Composable BoxScope.() -> Unit,
) {
    val contentCaptureEngine = remember { KrystalContentCaptureEngine() }

    val layoutInfoChannel = remember { Channel<EnhancedLayoutInfo>(Channel.UNLIMITED) }

    var containerBounds by remember { mutableStateOf(Rect.Zero) }
    var lastProcessedTime by remember { mutableStateOf(0L) }
    var lastScrollValue by remember { mutableStateOf(0) }

    val krystalContext = remember(containerBounds) {
        KrystalContext(containerBounds, contentCaptureEngine)
    }

    LaunchedEffect(scrollState?.value) {
        val currentScrollValue = scrollState?.value ?: 0
        if (currentScrollValue != lastScrollValue) {
            println("🔄 Scroll detected: $lastScrollValue -> $currentScrollValue")
            lastScrollValue = currentScrollValue

            contentCaptureEngine.forceContentUpdate()
            KrystalDebugRegistry.forceUpdate()
        }
    }

    LaunchedEffect(Unit) {
        val collectedInfo = mutableListOf<EnhancedLayoutInfo>()
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
                contentCaptureEngine.discoverContent(containerBounds, collectedInfo.toList())
                collectedInfo.clear()
                lastProcessedTime = currentTime
            }
            delay(30)
        }
    }

    var backgroundBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (containerBounds != Rect.Zero) {
                contentCaptureEngine.forceContentUpdate()
                KrystalDebugRegistry.forceUpdate()
            }
        }
    }

    CompositionLocalProvider(LocalKrystalContext provides krystalContext) {
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
                    val sizeChanged = kotlin.math.abs(newBounds.width - containerBounds.width) + 
                                     kotlin.math.abs(newBounds.height - containerBounds.height)
                    
                    if (centerDistance > 0.5f || sizeChanged > 1.0f) {
                        containerBounds = newBounds
                        println("📏 Container bounds updated: $newBounds")
                        KrystalDebugRegistry.forceUpdate()
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
                    
                    val layoutInfo = EnhancedLayoutInfo(
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
                        contentCaptureEngine.processDrawingOperations(containerBounds, operations)
                    }
                },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
                            backgroundBitmap = bitmap
                            onDrawBehind { /* TODO: Drawing code */ }
                        }
                ) {
                    backgroundContent()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            backgroundBitmap?.let { bitmap ->
                                // TODO: Re-enable blur
                                // val blurredBitmap = bitmap.applyGaussianBlur(8f)
                                drawImage(backgroundBitmap as ImageBitmap)
                            }
                            drawContent()
                        }
                ) {
                    foregroundContent()
                }
            }
        )
    }
}
