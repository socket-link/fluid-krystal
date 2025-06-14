package link.socket.krystal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import kotlinx.coroutines.delay

/**
 * Debug window to display all KrystalButton instances and their contexts
 * along with the Content Capture Engine state
 */
@Composable
fun KrystalDebugWindow(onClose: () -> Unit) {
    Window(
        title = "Krystal Debug Window",
        onCloseRequest = onClose
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Krystal Debug Console",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                var selectedTab by remember { mutableStateOf(DebugTab.BUTTONS) }

                // Tabs for different debug views
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    DebugTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab content
                when (selectedTab) {
                    DebugTab.BUTTONS -> KrystalButtonsDebugContent()
                    DebugTab.CONTENT_CAPTURE -> ContentCaptureDebugContent()
                }
            }
        }
    }
}

/**
 * Display a single KrystalButton's context information
 */
/**
 * Content displaying registered KrystalButtons
 */
@Composable
private fun KrystalButtonsDebugContent() {
    // This will trigger recomposition when registry changes
    val updateTrigger by KrystalDebugRegistry.updateTrigger
    
    // Also observe the engine triggers from all contexts
    val buttonContexts = remember(updateTrigger) { 
        val contexts = KrystalDebugRegistry.getAllButtonContexts()
        println("🐛 Retrieved ${contexts.size} button contexts (trigger: $updateTrigger)")
        contexts
    }
    
    // Add periodic force update for the buttons tab
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Every second, force a registry update
            KrystalDebugRegistry.forceUpdate()
        }
    }
    
    // Create a combined trigger that includes all engine triggers
    val allEngineTriggers = buttonContexts.values.map { it.contentCaptureEngine.debugUpdateTrigger }
    val combinedTrigger = remember(updateTrigger, allEngineTriggers) {
        updateTrigger + allEngineTriggers.sum()
    }
    
    println("🐛 Debug content recomposing with combined trigger: $combinedTrigger")

    if (buttonContexts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No KrystalButton instances detected")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(buttonContexts.entries.toList()) { (id, context) ->
                KrystalButtonDebugItem(id, context, combinedTrigger)
            }
        }
    }
}

@Composable
private fun KrystalButtonDebugItem(id: String, context: KrystalContext, refreshTrigger: Int) {
    var expanded by remember { mutableStateOf(false) }
    val engine = context.contentCaptureEngine

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = id,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }

        // Show basic context info
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bounds: ${context.bounds}", style = MaterialTheme.typography.bodySmall)

        // Show region analysis for this button's area - but don't trigger analysis in composition
        val analysis = remember(context.bounds, refreshTrigger) {
            // Only analyze if we have discovered content
            if (engine.discoveredContent.isNotEmpty()) {
                engine.analyzeRegion(context.bounds)
            } else {
                ContentAnalysis() // Default analysis
            }
        }

        // Analysis preview
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show dominant color preview
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(analysis.dominantColor)
                    .border(1.dp, Color.Black)
            )

            Text("isDarkContent: ${analysis.isDarkContent}")
            Text("contrastLevel: ${analysis.contrastLevel}")
        }

        // Expanded details
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                ContextProperty("Brightness", "${analysis.brightness}")
                ContextProperty("Dominant Color", "#${analysis.dominantColor.toArgb().toString(16)}")
                ContextProperty("Color Temperature", "${analysis.colorTemperature}")
                ContextProperty("Has Text Content", "${analysis.hasTextContent}")
                ContextProperty("Has Image Content", "${analysis.hasImageContent}")
                ContextProperty("Has High Contrast", "${analysis.hasHighContrast}")
            }
        }
    }
}

@Composable
private fun ContentCaptureDebugContent() {
    val updateTrigger by KrystalDebugRegistry.updateTrigger
    
    // Remove the force update loop - it's not needed and can cause issues
    // var forceUpdate by remember { mutableStateOf(0) }
    // LaunchedEffect(Unit) {
    //     while (true) {
    //         delay(1000)
    //         forceUpdate++
    //     }
    // }

    // We'll use the first button's context to access the engine
    val contexts = remember(updateTrigger) { 
        KrystalDebugRegistry.getAllButtonContexts() 
    }

    if (contexts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active content capture engines detected")
        }
        return
    }

    // Get the engine from the first context
    val engine = contexts.values.first().contentCaptureEngine
    
    // Observe the engine's debug trigger to force recomposition only when needed
    val engineTrigger = engine.debugUpdateTrigger
    
    println("🐛 ContentCapture recomposing with engine trigger: $engineTrigger")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            DebugSection("Discovered Content Regions") {
                ContentRegionsDebugView(engine, engineTrigger)
            }
        }

        item {
            DebugSection("Content Analysis Cache") {
                AnalysisCacheDebugView(engine, engineTrigger)
            }
        }

        item {
            DebugSection("Content Hierarchy") {
                ContentHierarchyDebugView(engine, engineTrigger)
            }
        }
    }
}

@Composable
private fun DebugSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ContentRegionsDebugView(engine: KrystalContentCaptureEngine, trigger: Int) {
    // Now we can access the public property directly instead of using reflection
    val discoveredContent = remember(trigger) { engine.discoveredContent }
    
    println("🐛 ContentRegions recomposing with ${discoveredContent.size} regions (trigger: $trigger)")

    if (discoveredContent.isEmpty()) {
        Text("No content regions discovered yet")
        return
    }

    Column {
        Text("Total regions: ${discoveredContent.size}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        discoveredContent.entries.take(5).forEach { (id, region) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text("Region: $id", style = MaterialTheme.typography.bodyMedium)
                Text("Type: ${region.contentType}", style = MaterialTheme.typography.bodySmall)
                Text("Bounds: ${region.bounds}", style = MaterialTheme.typography.bodySmall)

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(region.visualProperties.primaryColor)
                            .border(0.5.dp, Color.Black)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Brightness: ${region.visualProperties.brightness}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (discoveredContent.size > 5) {
            Text("... and ${discoveredContent.size - 5} more regions", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalysisCacheDebugView(engine: KrystalContentCaptureEngine, trigger: Int) {
    // Use stable reference to avoid flickering
    val analysisCache by remember(trigger) { 
        derivedStateOf { engine.analysisCache }
    }

    if (analysisCache.isEmpty()) {
        Text("Analysis cache is empty", style = MaterialTheme.typography.bodyMedium)
        return
    }

    Column {
        Text("Cache entries: ${analysisCache.size}", style = MaterialTheme.typography.bodyMedium)
        Text("Engine trigger: $trigger", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))

        // Show cache entries in a stable order
        val sortedEntries = remember(analysisCache) {
            analysisCache.entries.sortedBy { it.key.toString() }
        }

        sortedEntries.take(5).forEach { (rect, analysis) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text("Region: $rect", style = MaterialTheme.typography.bodySmall)

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(analysis.dominantColor)
                            .border(0.5.dp, Color.Black)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Brightness: ${String.format("%.2f", analysis.brightness)}", 
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Contrast: ${String.format("%.2f", analysis.contrastLevel)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    "Text: ${analysis.hasTextContent}, Images: ${analysis.hasImageContent}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (analysisCache.size > 5) {
            Text(
                "... and ${analysisCache.size - 5} more cache entries", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContentHierarchyDebugView(engine: KrystalContentCaptureEngine, trigger: Int) {
    // Use public property instead of reflection
    val hierarchy = remember(trigger) { engine.contentHierarchy }

    if (hierarchy == null) {
        Text("No content hierarchy built yet")
        return
    }

    Column {
        Text("Root node: ${hierarchy.contentType}", style = MaterialTheme.typography.bodyMedium)
        Text("Bounds: ${hierarchy.bounds}", style = MaterialTheme.typography.bodySmall)
        Text("Children: ${hierarchy.children.size}", style = MaterialTheme.typography.bodySmall)

        if (hierarchy.children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Top-level children:", style = MaterialTheme.typography.bodyMedium)

            hierarchy.children.take(5).forEachIndexed { index, child ->
                Text(
                    "$index: ${child.contentType} (${child.children.size} children)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (hierarchy.children.size > 5) {
                Text("... and ${hierarchy.children.size - 5} more children", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ContextProperty(name: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/**
 * Enum defining the available debug tabs
 */
private enum class DebugTab(val title: String) {
    BUTTONS("Buttons"),
    CONTENT_CAPTURE("Content Capture")
}

/**
 * Shows a button to toggle the debug window
 */
@Composable
fun KrystalDebugToggle(modifier: Modifier = Modifier) {
    var showDebugWindow by remember { mutableStateOf(false) }

    Button(
        onClick = { showDebugWindow = true },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Text("Show Krystal Debug")
    }

    if (showDebugWindow) {
        KrystalDebugWindow(onClose = { showDebugWindow = false })
    }
}
