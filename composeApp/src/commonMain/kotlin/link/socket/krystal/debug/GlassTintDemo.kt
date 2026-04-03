package link.socket.krystal.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import link.socket.krystal.api.GlassStyle
import link.socket.krystal.api.GlassSurface
import link.socket.krystal.api.GlassTint

@Composable
fun GlassTintDemo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460),
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "GlassSurface Tint Presets",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassTintCard(
                    label = "Clear",
                    style = GlassStyle.tinted(GlassTint.Clear),
                    modifier = Modifier.weight(1f),
                )
                GlassTintCard(
                    label = "Electric\nPurple",
                    style = GlassStyle.tinted(GlassTint.ElectricPurple),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassTintCard(
                    label = "Cerebral\nBlue",
                    style = GlassStyle.tinted(GlassTint.CerebralBlue),
                    modifier = Modifier.weight(1f),
                )
                GlassTintCard(
                    label = "Signal\nAmber",
                    style = GlassStyle.tinted(GlassTint.SignalAmber),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Socket Tab Bar",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )

            GlassSurface(
                style = GlassStyle.floatingBar(GlassTint.ElectricPurple),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Home", color = Color.White)
                    Text("Search", color = Color.White)
                    Text("Profile", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GlassTintCard(
    label: String,
    style: GlassStyle,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        style = style,
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
            )
        }
    }
}
