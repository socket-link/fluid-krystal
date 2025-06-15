package link.socket.krystal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import kotlinx.coroutines.delay
import link.socket.krystal.engine.ContentAnalysis
import link.socket.krystal.engine.KrystalContentCaptureEngine
import link.socket.krystal.engine.LocalKrystalContainerContext
import java.util.Locale

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

                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    DebugTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    DebugTab.BUTTONS -> KrystalButtonsDebugContent()
                    DebugTab.CONTENT_CAPTURE -> ContentCaptureDebugContent()
                }
            }
        }
    }
}

@Composable
private fun KrystalButtonsDebugContent() {
    val updateTrigger by KrystalDebug.updateTrigger

    val buttonBounds= remember(updateTrigger) {
        val contexts = KrystalDebug.getAllButtonBounds()
        println("🐛 Retrieved ${contexts.size} button contexts (trigger: $updateTrigger)")
        contexts
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            KrystalDebug.forceUpdate()
        }
    }

    if (buttonBounds.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No KrystalButton instances detected")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(buttonBounds.entries.toList()) { (id, bounds) ->
                KrystalButtonDebugItem(id, bounds, updateTrigger)
            }
        }
    }
}

@Composable
private fun KrystalButtonDebugItem(
    id: String,
    bounds: Rect,
    refreshTrigger: Int
) {
    var expanded by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(8.dp))
        Text("Bounds: $bounds", style = MaterialTheme.typography.bodySmall)

        val analysis = remember(bounds, refreshTrigger) {
            ContentAnalysis()
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(analysis.dominantColor)
                    .border(1.dp, Color.Black)
            )

            Text("isDarkContent: ${analysis.isDarkContent}")
            Text("contrastLevel: ${analysis.contrastLevel}")
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
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
    val updateTrigger by KrystalDebug.updateTrigger

    val contexts = remember(updateTrigger) {
        KrystalDebug.getAllButtonBounds()
    }

    if (contexts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active content capture engines detected")
        }
        return
    }

    val krystalContext = LocalKrystalContainerContext.current
    val engine = krystalContext.contentCaptureEngine
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
            Text(
                "... and ${discoveredContent.size - 5} more regions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalysisCacheDebugView(engine: KrystalContentCaptureEngine, trigger: Int) {
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
                        "Brightness: ${String.format(Locale.ROOT, "%.2f", analysis.brightness)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Contrast: ${String.format(Locale.ROOT, "%.2f", analysis.contrastLevel)}",
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
                Text(
                    "... and ${hierarchy.children.size - 5} more children",
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

private enum class DebugTab(val title: String) {
    BUTTONS("Buttons"),
    CONTENT_CAPTURE("Content Capture")
}

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
        Text("Show Debug")
    }

    if (showDebugWindow) {
        KrystalDebugWindow(onClose = { showDebugWindow = false })
    }
}
