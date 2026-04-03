package link.socket.krystal.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import link.socket.krystal.api.GlassElevation
import link.socket.krystal.api.GlassStyle
import link.socket.krystal.api.GlassSurface
import link.socket.krystal.api.GlassTint

private val galleryBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0f0c29),
        Color(0xFF302b63),
        Color(0xFF24243e),
    )
)

@Composable
fun GlassGallery() {
    var selectedTint by remember { mutableStateOf<GlassTint>(GlassTint.Clear) }
    var blurRadius by remember { mutableFloatStateOf(24f) }
    var opacity by remember { mutableFloatStateOf(0.12f) }
    var selectedElevation by remember { mutableStateOf(GlassElevation.Flat) }

    val heroStyle = GlassStyle(
        tint = selectedTint,
        blurRadius = blurRadius.dp,
        opacity = opacity,
        elevation = selectedElevation,
        cornerRadius = 20.dp,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(galleryBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hero surface
            Text(
                text = "Interactive Glass",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            GlassSurface(
                style = heroStyle,
                modifier = Modifier.fillMaxWidth().height(160.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = tintLabel(selectedTint),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "blur: ${blurRadius.toInt()}dp · opacity: ${(opacity * 100).toInt()}% · ${selectedElevation.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }

            // Tint selector
            SectionLabel("Tint")
            TintSelector(
                selected = selectedTint,
                onSelect = { selectedTint = it },
            )

            // Blur radius
            SectionLabel("Blur Radius: ${blurRadius.toInt()}dp")
            Slider(
                value = blurRadius,
                onValueChange = { blurRadius = it },
                valueRange = 0f..50f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.6f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                ),
            )

            // Opacity
            SectionLabel("Opacity: ${(opacity * 100).toInt()}%")
            Slider(
                value = opacity,
                onValueChange = { opacity = it },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.6f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                ),
            )

            // Elevation selector
            SectionLabel("Elevation")
            ElevationSelector(
                selected = selectedElevation,
                onSelect = { selectedElevation = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preset gallery
            Text(
                text = "Tint Presets",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PresetCard("Clear", GlassTint.Clear, Modifier.weight(1f))
                PresetCard("Electric\nPurple", GlassTint.ElectricPurple, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PresetCard("Cerebral\nBlue", GlassTint.CerebralBlue, Modifier.weight(1f))
                PresetCard("Signal\nAmber", GlassTint.SignalAmber, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Elevation variants
            Text(
                text = "Elevation Variants",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassElevation.entries.forEach { elev ->
                    GlassSurface(
                        style = GlassStyle.tinted(
                            tint = GlassTint.CerebralBlue,
                            elevation = elev,
                        ),
                        modifier = Modifier.weight(1f).height(100.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = elev.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Socket tab bar preview
            Text(
                text = "Socket Tab Bar",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            GlassSurface(
                style = GlassStyle.floatingBar(GlassTint.ElectricPurple),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Home", color = Color.White, fontWeight = FontWeight.Medium)
                    Text("Search", color = Color.White.copy(alpha = 0.6f))
                    Text("Profile", color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White.copy(alpha = 0.8f),
    )
}

private val tintOptions = listOf(
    GlassTint.Clear to Color.White,
    GlassTint.ElectricPurple to Color(0xFF7B2FBE),
    GlassTint.CerebralBlue to Color(0xFF3B82F6),
    GlassTint.SignalAmber to Color(0xFFF59E0B),
)

@Composable
private fun TintSelector(
    selected: GlassTint,
    onSelect: (GlassTint) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tintOptions.forEach { (tint, displayColor) ->
            val isSelected = selected::class == tint::class
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(displayColor.copy(alpha = 0.8f), CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, Color.White, CircleShape)
                        } else {
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        }
                    )
                    .clickable { onSelect(tint) },
            )
        }
    }
}

@Composable
private fun ElevationSelector(
    selected: GlassElevation,
    onSelect: (GlassElevation) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GlassElevation.entries.forEach { elev ->
            val isSelected = selected == elev
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else Color.Transparent,
                        RoundedCornerShape(8.dp),
                    )
                    .border(
                        1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.5f)
                        else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelect(elev) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = elev.name,
                    color = Color.White,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    label: String,
    tint: GlassTint,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        style = GlassStyle.tinted(tint),
        modifier = modifier.height(120.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun tintLabel(tint: GlassTint): String = when (tint) {
    is GlassTint.Clear -> "Clear"
    is GlassTint.ElectricPurple -> "Electric Purple"
    is GlassTint.CerebralBlue -> "Cerebral Blue"
    is GlassTint.SignalAmber -> "Signal Amber"
    is GlassTint.Custom -> "Custom"
}
