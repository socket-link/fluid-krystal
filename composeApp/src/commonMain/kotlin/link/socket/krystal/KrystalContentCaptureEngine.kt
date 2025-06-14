@file:OptIn(ExperimentalComposeUiApi::class)

package link.socket.krystal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import kotlin.collections.emptyMap
import kotlin.collections.plus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Advanced content capture engine that uses multiple sources of information
 * to intelligently understand UI content structure and types.
 */
class KrystalContentCaptureEngine {

    private var _discoveredContent: Map<String, DiscoveredContentRegion> by mutableStateOf(emptyMap())
    private var _contentHierarchy: ContentHierarchyNode? by mutableStateOf(null)
    private var _analysisCache: Map<Rect, Pair<ContentAnalysis, Long>> by mutableStateOf(emptyMap())
    private var _interactionHistory: Map<Rect, InteractionType> by mutableStateOf(emptyMap())
    private var _drawingOperationsHistory: Map<Rect, List<DrawingOperation>> by mutableStateOf(emptyMap())

    val discoveredContent: Map<String, DiscoveredContentRegion> get() = _discoveredContent
    val contentHierarchy: ContentHierarchyNode? get() = _contentHierarchy
    val analysisCache: Map<Rect, ContentAnalysis> get() = _analysisCache.mapValues { it.value.first }

    var debugUpdateTrigger: Int by mutableStateOf(0)
        private set

    private var discoveryVersion: Int by mutableStateOf(0)

    fun discoverContent(
        rootBounds: Rect,
        layoutInfo: List<EnhancedLayoutInfo>
    ) {
        println("🔍 discoverContent called with ${layoutInfo.size} layout items")
        
        val hierarchy = buildAdvancedContentHierarchy(layoutInfo, rootBounds)
        _contentHierarchy = hierarchy

        val discovered = extractContentRegions(hierarchy)
        
        println("🔍 Discovered ${discovered.size} content regions")

        val hasSignificantChange = discovered.size != _discoveredContent.size || 
                              discovered.keys != _discoveredContent.keys
    
        if (hasSignificantChange) {
            println("🔍 Updating discoveredContent from ${_discoveredContent.size} to ${discovered.size} regions")
            _discoveredContent = discovered
            discoveryVersion++
            debugUpdateTrigger++
            println("🔍 Debug trigger incremented to: $debugUpdateTrigger")
            _analysisCache = emptyMap()
            println("🧹 Analysis cache cleared due to content change")
        }
    }

    @OptIn(ExperimentalTime::class)
    fun analyzeRegion(region: Rect): ContentAnalysis {
        val cacheKey = region
        _analysisCache[cacheKey]?.let { (analysis, _) -> 
            println("💾 Cache hit for region: $region")
            return analysis 
        }

        val intersectingContent = _discoveredContent.values.filter { content ->
            content.bounds.overlaps(region)
        }

        val analysis = synthesizeAdvancedContentAnalysis(region, intersectingContent)

        val timestamp = Clock.System.now().toEpochMilliseconds()
        _analysisCache = _analysisCache.plus(cacheKey to (analysis to timestamp))
        println("💾 Cached analysis for region: $region (cache size: ${_analysisCache.size})")

        cleanupCache()
        return analysis
    }

    fun recordInteraction(bounds: Rect, interactionType: InteractionType) {
        _interactionHistory = _interactionHistory + (bounds to interactionType)
        println("👆 Recorded interaction: $interactionType at $bounds")
    }

    @OptIn(ExperimentalTime::class)
    fun processDrawingOperations(containerBounds: Rect, operations: List<DrawingOperation>) {
        _drawingOperationsHistory = _drawingOperationsHistory + (containerBounds to operations)
        
        if (operations.isNotEmpty() && operations.size > 2) {
            val shouldUpdate = (Clock.System.now().toEpochMilliseconds() / 1000) % 5 == 0L
            if (shouldUpdate) {
                debugUpdateTrigger++
                println("📊 Drawing operations processed, debug trigger incremented to: $debugUpdateTrigger")
            }
        }
    }

    private fun buildAdvancedContentHierarchy(
        layoutInfo: List<EnhancedLayoutInfo>,
        containerBounds: Rect
    ): ContentHierarchyNode {
        val rootNode = ContentHierarchyNode(
            bounds = containerBounds,
            contentType = ContentType.CONTAINER,
            visualProperties = VisualProperties(),
            children = mutableListOf(),
            semanticInfo = null,
            parentNode = null
        )

        // Build nodes with enhanced content type inference
        val nodeMap = mutableMapOf<Rect, ContentHierarchyNode>()
        nodeMap[containerBounds] = rootNode

        layoutInfo.forEach { info ->
            val contentType = inferAdvancedContentType(info, layoutInfo)
            val node = ContentHierarchyNode(
                bounds = info.bounds,
                contentType = contentType,
                visualProperties = inferEnhancedVisualProperties(info),
                children = mutableListOf(),
                semanticInfo = info.semanticInfo,
                parentNode = null
            )

            nodeMap[info.bounds] = node
            addToAdvancedHierarchy(rootNode, node, info.parentBounds, nodeMap)
        }

        // Set parent references and calculate hierarchy levels
        setParentReferencesAndLevels(rootNode)

        return rootNode
    }

    private fun inferAdvancedContentType(
        layoutInfo: EnhancedLayoutInfo,
        allLayoutInfo: List<EnhancedLayoutInfo>
    ): ContentType {
        // 1. Check semantic information first (highest priority)
        layoutInfo.semanticInfo?.let { semantics ->
            semantics.textContent?.let { return ContentType.TEXT }
            if (semantics.isButton) return ContentType.BUTTON
            if (semantics.isClickable) return ContentType.INTERACTIVE_ELEMENT
            if (semantics.isImage) return ContentType.IMAGE
            if (semantics.isTextField) return ContentType.TEXT_INPUT
            if (semantics.isContainer) return ContentType.CONTAINER
        }

        // 2. Check composable type information
        layoutInfo.composableType?.let { type ->
            return when {
                type.contains("Text", ignoreCase = true) || 
                type.contains("BasicText", ignoreCase = true) -> ContentType.TEXT
                type.contains("Image", ignoreCase = true) || 
                type.contains("Icon", ignoreCase = true) -> ContentType.IMAGE
                type.contains("Button", ignoreCase = true) -> ContentType.BUTTON
                type.contains("TextField", ignoreCase = true) ||
                type.contains("OutlinedTextField", ignoreCase = true) -> ContentType.TEXT_INPUT
                type.contains("Card", ignoreCase = true) -> ContentType.CARD
                type.contains("Surface", ignoreCase = true) -> ContentType.SURFACE
                type.contains("Box", ignoreCase = true) || 
                type.contains("Column", ignoreCase = true) || 
                type.contains("Row", ignoreCase = true) -> ContentType.CONTAINER
                type.contains("LazyColumn", ignoreCase = true) ||
                type.contains("LazyRow", ignoreCase = true) -> ContentType.LIST
                else -> ContentType.UNKNOWN
            }
        }

        // 3. Check modifier chain for hints
        layoutInfo.modifierChain.forEach { modifier ->
            when {
                modifier.contains("clickable") -> return ContentType.INTERACTIVE_ELEMENT
                modifier.contains("background") -> return ContentType.BACKGROUND
                modifier.contains("border") -> return ContentType.SHAPE
                modifier.contains("scrollable") -> return ContentType.SCROLLABLE_CONTENT
            }
        }

        // 4. Check interaction history
        _interactionHistory[layoutInfo.bounds]?.let { interaction ->
            return when (interaction) {
                InteractionType.CLICK -> ContentType.INTERACTIVE_ELEMENT
                InteractionType.SCROLL -> ContentType.SCROLLABLE_CONTENT
                InteractionType.TEXT_INPUT -> ContentType.TEXT_INPUT
                InteractionType.DRAG -> ContentType.DRAGGABLE_CONTENT
                InteractionType.PINCH -> ContentType.ZOOMABLE_CONTENT
            }
        }

        // 5. Analyze drawing operations
        _drawingOperationsHistory[layoutInfo.bounds]?.let { operations ->
            val inferredType = inferContentTypeFromDrawingOps(operations)
            if (inferredType != ContentType.UNKNOWN) return inferredType
        }

        // 6. Use layout pattern recognition
        val patternType = inferFromLayoutPatterns(layoutInfo, allLayoutInfo)
        if (patternType != ContentType.UNKNOWN) return patternType

        // 7. Use parent-child context analysis
        val contextType = inferFromContext(layoutInfo, allLayoutInfo)
        if (contextType != ContentType.UNKNOWN) return contextType

        // 8. Fall back to geometric heuristics (last resort)
        return inferFromGeometricHeuristics(layoutInfo)
    }

    private fun inferContentTypeFromDrawingOps(operations: List<DrawingOperation>): ContentType {
        val textOps = operations.count { it.type == DrawingType.TEXT }
        val imageOps = operations.count { it.type == DrawingType.IMAGE }
        val shapeOps = operations.count { it.type == DrawingType.SHAPE }
        val backgroundOps = operations.count { it.type == DrawingType.BACKGROUND }

        return when {
            textOps > 0 -> ContentType.TEXT
            imageOps > 0 -> ContentType.IMAGE
            backgroundOps > shapeOps -> ContentType.BACKGROUND
            shapeOps > 0 -> ContentType.SHAPE
            else -> ContentType.UNKNOWN
        }
    }

    private fun inferFromLayoutPatterns(
        layoutInfo: EnhancedLayoutInfo,
        allLayoutInfo: List<EnhancedLayoutInfo>
    ): ContentType {
        val aspectRatio = layoutInfo.bounds.width / layoutInfo.bounds.height
        val area = layoutInfo.bounds.width * layoutInfo.bounds.height
        val parentWidth = layoutInfo.parentBounds?.width ?: layoutInfo.bounds.width

        return when {
            // Navigation bars (wide and thin, spans most width)
            aspectRatio > 8f && layoutInfo.bounds.height < 80 && 
            layoutInfo.bounds.width > parentWidth * 0.9f -> ContentType.NAVIGATION

            // App bars / toolbars
            aspectRatio > 4f && layoutInfo.bounds.height in 40f..120f &&
            layoutInfo.bounds.width > parentWidth * 0.8f -> ContentType.APP_BAR

            // Cards (moderate size, reasonable aspect ratio)
            aspectRatio in 0.6f..2f && area > 15000 && area < 200000 -> ContentType.CARD

            // List items (wide, moderate height, multiple similar items)
            aspectRatio > 2f && layoutInfo.bounds.height in 60f..200f &&
            hasSimilarSiblings(layoutInfo, allLayoutInfo) -> ContentType.LIST_ITEM

            // Buttons (specific size range and aspect ratios)
            aspectRatio in 1.5f..5f && area in 3000f..25000f -> ContentType.BUTTON

            // Text blocks (wide, varying height, not too tall)
            aspectRatio > 2f && layoutInfo.bounds.height < 300 && area > 2000 -> ContentType.TEXT

            // Floating action buttons (small, square-ish)
            aspectRatio in 0.8f..1.25f && area in 2000f..8000f -> ContentType.FAB

            // Bottom sheets (wide, positioned at bottom)
            aspectRatio > 3f && isPositionedAtBottom(layoutInfo, parentWidth) -> ContentType.BOTTOM_SHEET

            else -> ContentType.UNKNOWN
        }
    }

    private fun inferFromContext(
        layoutInfo: EnhancedLayoutInfo,
        allLayoutInfo: List<EnhancedLayoutInfo>
    ): ContentType {
        // Find parent info
        val parentInfo = allLayoutInfo.find { it.bounds == layoutInfo.parentBounds }
        val childrenInfo = allLayoutInfo.filter { it.parentBounds == layoutInfo.bounds }

        parentInfo?.let { parent ->
            // Analyze based on parent's probable type
            val parentType = if (parent.bounds == layoutInfo.bounds) ContentType.UNKNOWN 
                            else inferAdvancedContentType(parent, allLayoutInfo)

            when (parentType) {
                ContentType.LIST -> return ContentType.LIST_ITEM
                ContentType.NAVIGATION -> return ContentType.NAV_ITEM
                ContentType.APP_BAR -> return ContentType.TOOLBAR_ITEM
                ContentType.CARD -> {
                    // Small elements in cards are likely text or buttons
                    val area = layoutInfo.bounds.width * layoutInfo.bounds.height
                    return if (area < 5000) ContentType.TEXT else ContentType.CONTENT_AREA
                }
                else -> { /* continue analysis */ }
            }
        }

        // Analyze children to infer container type
        if (childrenInfo.isNotEmpty()) {
            val childTypes = childrenInfo.map { child ->
                if (child.bounds == layoutInfo.bounds) ContentType.UNKNOWN
                else inferAdvancedContentType(child, allLayoutInfo)
            }

            when {
                childTypes.all { it == ContentType.TEXT || it == ContentType.BUTTON } -> 
                    return ContentType.TEXT_CONTAINER
                childTypes.all { it == ContentType.IMAGE } -> 
                    return ContentType.IMAGE_GALLERY
                childTypes.count { it == ContentType.LIST_ITEM } > 2 -> 
                    return ContentType.LIST
                else -> { /* continue analysis */ }
            }
        }

        return ContentType.UNKNOWN
    }

    private fun inferFromGeometricHeuristics(layoutInfo: EnhancedLayoutInfo): ContentType {
        val aspectRatio = layoutInfo.bounds.width / layoutInfo.bounds.height
        val area = layoutInfo.bounds.width * layoutInfo.bounds.height

        return when {
            // Very small elements
            area < 1000 -> ContentType.DECORATION

            // Text-like proportions
            aspectRatio > 2f && layoutInfo.bounds.height < 100 -> ContentType.TEXT

            // Square or near-square, medium size
            aspectRatio in 0.7f..1.4f && area in 5000f..50000f -> ContentType.IMAGE

            // Large background elements
            area > 100000 -> ContentType.BACKGROUND

            // Default to shape for other cases
            else -> ContentType.SHAPE
        }
    }

    private fun hasSimilarSiblings(
        layoutInfo: EnhancedLayoutInfo,
        allLayoutInfo: List<EnhancedLayoutInfo>
    ): Boolean {
        val siblings = allLayoutInfo.filter { 
            it.parentBounds == layoutInfo.parentBounds && it.bounds != layoutInfo.bounds 
        }

        if (siblings.size < 2) return false

        // Check if there are multiple items with similar dimensions
        val similarSiblings = siblings.count { sibling ->
            val widthRatio = sibling.bounds.width / layoutInfo.bounds.width
            val heightRatio = sibling.bounds.height / layoutInfo.bounds.height
            widthRatio in 0.7f..1.3f && heightRatio in 0.7f..1.3f
        }

        return similarSiblings >= 2
    }

    private fun isPositionedAtBottom(layoutInfo: EnhancedLayoutInfo, parentWidth: Float): Boolean {
        val parentHeight = layoutInfo.parentBounds?.height ?: return false
        val relativeY = layoutInfo.bounds.top / parentHeight
        return relativeY > 0.7f
    }

    private fun inferEnhancedVisualProperties(layoutInfo: EnhancedLayoutInfo): VisualProperties {
        val inferredColor = layoutInfo.drawingHints.backgroundColor ?: Color.Gray
        val brightness = inferredColor.luminance()
        val isTransparent = inferredColor.alpha < 1f
        val hasGradient = layoutInfo.drawingHints.hasComplexBackground

        // Enhanced property detection
        val hasElevation = layoutInfo.modifierChain.any { it.contains("shadow") || it.contains("elevation") }
        val isAnimated = layoutInfo.modifierChain.any { it.contains("animat") }
        val hasRipple = layoutInfo.modifierChain.any { it.contains("ripple") }

        return VisualProperties(
            primaryColor = inferredColor,
            brightness = brightness,
            hasGradient = hasGradient,
            isTransparent = isTransparent,
            hasElevation = hasElevation,
            isAnimated = isAnimated,
            hasRipple = hasRipple
        )
    }

    private fun addToAdvancedHierarchy(
        rootNode: ContentHierarchyNode,
        newNode: ContentHierarchyNode,
        parentBounds: Rect?,
        nodeMap: Map<Rect, ContentHierarchyNode>
    ) {
        val parentNode = if (parentBounds != null) {
            nodeMap[parentBounds] ?: findSmallestContainingNode(rootNode, newNode.bounds) ?: rootNode
        } else {
            findSmallestContainingNode(rootNode, newNode.bounds) ?: rootNode
        }

        parentNode.children.add(newNode)
        newNode.parentNode = parentNode
    }

    private fun setParentReferencesAndLevels(node: ContentHierarchyNode, level: Int = 0) {
        node.hierarchyLevel = level
        node.children.forEach { child ->
            child.parentNode = node
            setParentReferencesAndLevels(child, level + 1)
        }
    }

    private fun findSmallestContainingNode(node: ContentHierarchyNode, bounds: Rect): ContentHierarchyNode? {
        if (!node.bounds.contains(bounds.topLeft) || !node.bounds.contains(bounds.bottomRight)) return null

        for (child in node.children) {
            val found = findSmallestContainingNode(child, bounds)
            if (found != null) return found
        }

        return node
    }

    private fun extractContentRegions(hierarchy: ContentHierarchyNode): Map<String, DiscoveredContentRegion> {
        val regions = mutableMapOf<String, DiscoveredContentRegion>()
        extractRegionsRecursive(hierarchy, regions)
        return regions
    }

    @OptIn(ExperimentalTime::class)
    private fun extractRegionsRecursive(
        node: ContentHierarchyNode,
        regions: MutableMap<String, DiscoveredContentRegion>
    ) {
        val regionId = "region_${node.bounds.hashCode()}_${Clock.System.now().toEpochMilliseconds()}"

        regions[regionId] = DiscoveredContentRegion(
            bounds = node.bounds,
            contentType = node.contentType,
            visualProperties = node.visualProperties,
            hierarchyLevel = node.hierarchyLevel,
            lastDiscovered = Clock.System.now().toEpochMilliseconds(),
            semanticInfo = node.semanticInfo,
            confidence = calculateContentTypeConfidence(node)
        )

        node.children.forEach { child ->
            extractRegionsRecursive(child, regions)
        }
    }

    private fun calculateContentTypeConfidence(node: ContentHierarchyNode): Float {
        var confidence = 0.5f

        // Higher confidence if we have semantic information
        if (node.semanticInfo != null) confidence += 0.3f

        // Higher confidence for well-known content types
        when (node.contentType) {
            ContentType.TEXT, ContentType.IMAGE, ContentType.BUTTON -> confidence += 0.2f
            ContentType.CONTAINER, ContentType.BACKGROUND -> confidence += 0.1f
            ContentType.UNKNOWN -> confidence -= 0.2f
            else -> confidence += 0.1f
        }

        return confidence.coerceIn(0f, 1f)
    }

    private fun synthesizeAdvancedContentAnalysis(
        region: Rect,
        intersectingContent: List<DiscoveredContentRegion>
    ): ContentAnalysis {
        if (intersectingContent.isEmpty()) {
            return ContentAnalysis()
        }

        var totalWeight = 0f
        var weightedBrightness = 0f
        var weightedRed = 0f
        var weightedGreen = 0f
        var weightedBlue = 0f
        var maxContrast = 0f
        var hasTextContent = false
        var hasImageContent = false
        var hasInteractiveContent = false
        var hasAnimatedContent = false

        intersectingContent.forEach { content ->
            val intersectionArea = calculateIntersectionArea(region, content.bounds)
            val weight = intersectionArea / (region.width * region.height) * content.confidence

            totalWeight += weight

            val color = content.visualProperties.primaryColor
            val brightness = content.visualProperties.brightness

            weightedBrightness += brightness * weight
            weightedRed += color.red * weight
            weightedGreen += color.green * weight
            weightedBlue += color.blue * weight

            when (content.contentType) {
                ContentType.TEXT, ContentType.TEXT_INPUT -> {
                    hasTextContent = true
                    maxContrast = maxOf(maxContrast, 0.9f)
                }
                ContentType.IMAGE -> {
                    hasImageContent = true
                    maxContrast = maxOf(maxContrast, 0.6f)
                }
                ContentType.BUTTON, ContentType.INTERACTIVE_ELEMENT -> {
                    hasInteractiveContent = true
                    maxContrast = maxOf(maxContrast, 0.7f)
                }
                else -> {
                    maxContrast = maxOf(maxContrast, 0.4f)
                }
            }

            if (content.visualProperties.isAnimated) {
                hasAnimatedContent = true
            }
        }

        val finalBrightness = if (totalWeight > 0f) weightedBrightness / totalWeight else 0.5f
        val dominantColor = if (totalWeight > 0f) {
            Color(
                red = (weightedRed / totalWeight).coerceIn(0f, 1f),
                green = (weightedGreen / totalWeight).coerceIn(0f, 1f),
                blue = (weightedBlue / totalWeight).coerceIn(0f, 1f)
            )
        } else Color.Gray

        val colorTemperature = calculateColorTemperature(dominantColor)

        return ContentAnalysis(
            brightness = finalBrightness,
            dominantColor = dominantColor,
            contrastLevel = maxContrast,
            isDarkContent = finalBrightness < 0.5f,
            hasHighContrast = maxContrast > 0.6f,
            colorTemperature = colorTemperature,
            hasTextContent = hasTextContent,
            hasImageContent = hasImageContent,
            hasInteractiveContent = hasInteractiveContent,
            hasAnimatedContent = hasAnimatedContent
        )
    }

    private fun calculateIntersectionArea(rect1: Rect, rect2: Rect): Float {
        val intersectionRect = rect1.intersect(rect2)
        return if (intersectionRect.isEmpty) 0f else intersectionRect.width * intersectionRect.height
    }

    private fun calculateColorTemperature(color: Color): Float {
        return (color.red * 0.5f + color.green * 0.3f - color.blue * 0.8f).coerceIn(0f, 1f)
    }

    private fun cleanupCache() {
        if (_analysisCache.size > 50) {
            val sortedEntries = _analysisCache.entries.toList()
                .sortedBy { (_, pair) -> pair.second }
                .takeLast(25)
            
            _analysisCache = sortedEntries.associateBy({ it.key }, { it.value })
            debugUpdateTrigger++
            println("🧹 Cache cleaned, debug trigger incremented to: $debugUpdateTrigger")
        }
    }

    fun forceContentUpdate() {
        if (_discoveredContent.isNotEmpty()) {
            debugUpdateTrigger++
            println("🔄 Forced content update, debug trigger: $debugUpdateTrigger")
        }
    }
}

// Enhanced data classes with additional information

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

// Legacy data classes for compatibility
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
