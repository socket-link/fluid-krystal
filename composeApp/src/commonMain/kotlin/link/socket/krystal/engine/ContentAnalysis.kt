package link.socket.krystal.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class EnhancedLayoutInfo(
    val bounds: Rect,
    val parentBounds: Rect?,
    val drawingHints: DrawingHints,
    val composableType: String? = null,
    val modifierChain: List<String> = emptyList(),
    val semanticInfo: SemanticInfo? = null
)

data class SemanticInfo(
    val textContent: String? = null,
    val isButton: Boolean = false,
    val isClickable: Boolean = false,
    val isImage: Boolean = false,
    val isTextField: Boolean = false,
    val isContainer: Boolean = false,
    val accessibilityLabel: String? = null,
    val role: String? = null
)

data class DiscoveredContentRegion(
    val bounds: Rect,
    val contentType: ContentType,
    val visualProperties: VisualProperties,
    val hierarchyLevel: Int,
    val lastDiscovered: Long,
    val semanticInfo: SemanticInfo? = null,
    val confidence: Float = 0.5f
)

data class ContentHierarchyNode(
    val bounds: Rect,
    val contentType: ContentType,
    val visualProperties: VisualProperties,
    val children: MutableList<ContentHierarchyNode>,
    val semanticInfo: SemanticInfo? = null,
    var parentNode: ContentHierarchyNode? = null,
    var hierarchyLevel: Int = 0
)

data class DrawingHints(
    val backgroundColor: Color? = null,
    val hasComplexBackground: Boolean = false,
    val isTextContent: Boolean = false,
    val hasElevation: Boolean = false,
    val isAnimated: Boolean = false
)

enum class ContentType {
    // Basic content types
    TEXT,
    IMAGE,
    BACKGROUND,
    CONTAINER,
    SHAPE,
    UNKNOWN,

    // Interactive elements
    BUTTON,
    INTERACTIVE_ELEMENT,
    TEXT_INPUT,

    // Layout components
    CARD,
    SURFACE,
    LIST,
    LIST_ITEM,
    NAVIGATION,
    APP_BAR,
    TOOLBAR_ITEM,
    NAV_ITEM,
    FAB,
    BOTTOM_SHEET,

    // Content areas
    TEXT_CONTAINER,
    IMAGE_GALLERY,
    CONTENT_AREA,
    SCROLLABLE_CONTENT,
    DRAGGABLE_CONTENT,
    ZOOMABLE_CONTENT,

    // Decorative elements
    DECORATION
}

data class VisualProperties(
    val primaryColor: Color = Color.Gray,
    val brightness: Float = 0.5f,
    val hasGradient: Boolean = false,
    val isTransparent: Boolean = false,
    val hasElevation: Boolean = false,
    val isAnimated: Boolean = false,
    val hasRipple: Boolean = false
)

enum class InteractionType {
    CLICK,
    SCROLL,
    TEXT_INPUT,
    DRAG,
    PINCH
}

data class DrawingOperation @OptIn(ExperimentalTime::class) constructor(
    val type: DrawingType,
    val bounds: Rect,
    val color: Color,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)

enum class DrawingType {
    BACKGROUND,
    TEXT,
    SHAPE,
    IMAGE
}

data class ContentAnalysis(
    val brightness: Float = 0.5f,
    val dominantColor: Color = Color.Gray,
    val contrastLevel: Float = 0.5f,
    val isDarkContent: Boolean = false,
    val hasHighContrast: Boolean = false,
    val colorTemperature: Float = 0.5f,
    val hasTextContent: Boolean = false,
    val hasImageContent: Boolean = false,
    val hasInteractiveContent: Boolean = false,
    val hasAnimatedContent: Boolean = false
)

data class MeasuredLayoutInfo(
    val bounds: Rect,
    val parentBounds: Rect?,
    val drawingHints: DrawingHints
)

data class ContentRegionInfo(
    val bounds: Rect,
    val contentType: ContentType,
    val visualProperties: VisualProperties,
    val lastUpdated: Long
)
