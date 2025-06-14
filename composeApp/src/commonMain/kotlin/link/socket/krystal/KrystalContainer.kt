package link.socket.krystal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun KrystalContainer(
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.ScrollState? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // Create and remember the content capture engine
    val contentCaptureEngine = remember { KrystalContentCaptureEngine() }

    // A channel to safely pass layout info from the UI thread to the processing coroutine.
    val layoutInfoChannel = remember { Channel<EnhancedLayoutInfo>(Channel.UNLIMITED) }

    var containerBounds by remember { mutableStateOf(Rect.Zero) }
    var lastProcessedTime by remember { mutableStateOf(0L) }
    var lastScrollValue by remember { mutableStateOf(0) }

    // Create the Krystal context
    val krystalContext = remember(containerBounds) {
        KrystalContext(containerBounds, contentCaptureEngine)
    }

    // Monitor scroll changes and trigger updates
    LaunchedEffect(scrollState?.value) {
        val currentScrollValue = scrollState?.value ?: 0
        if (currentScrollValue != lastScrollValue) {
            println("🔄 Scroll detected: $lastScrollValue -> $currentScrollValue")
            lastScrollValue = currentScrollValue
            
            // Force updates when scrolling
            contentCaptureEngine.forceContentUpdate()
            KrystalDebugRegistry.forceUpdate()
        }
    }

    // Set up periodic content discovery with better timing
    LaunchedEffect(Unit) {
        val collectedInfo = mutableListOf<EnhancedLayoutInfo>()
        while (true) {
            // Drain the channel of all pending layout info.
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

    // Periodic updates to catch any missed changes
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Every second, trigger updates
            if (containerBounds != Rect.Zero) {
                contentCaptureEngine.forceContentUpdate()
                KrystalDebugRegistry.forceUpdate()
            }
        }
    }

    // Provide the context to descendants
    CompositionLocalProvider(LocalKrystalContext provides krystalContext) {
        // Main container
        Box(
            modifier = modifier
                .onGloballyPositioned { coordinates ->
                    // Update container bounds
                    val windowOffset = coordinates.localToWindow(Offset.Zero)
                    val newBounds = Rect(
                        offset = windowOffset,
                        size = coordinates.size.toSize()
                    )
                    
                    // Be more sensitive to position changes (for scrolling)
                    val centerDistance = (newBounds.center - containerBounds.center).getDistance()
                    val sizeChanged = kotlin.math.abs(newBounds.width - containerBounds.width) + 
                                     kotlin.math.abs(newBounds.height - containerBounds.height)
                    
                    // Trigger on smaller changes to catch scrolling
                    if (centerDistance > 0.5f || sizeChanged > 1.0f) {
                        containerBounds = newBounds
                        println("📏 Container bounds updated: $newBounds")
                        KrystalDebugRegistry.forceUpdate()
                    }

                    // Always send layout info, including scroll offset
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
            content = content
        )
    }
}
