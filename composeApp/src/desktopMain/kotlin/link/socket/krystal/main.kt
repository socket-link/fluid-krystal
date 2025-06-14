package link.socket.krystal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import krystal.composeapp.generated.resources.Res
import krystal.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "krystal",
    ) {
        MaterialTheme {
            val scrollState = rememberScrollState()
            
            KrystalContainer(scrollState = scrollState) {
                // Main app content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.requiredHeight(64.dp))
                    Text("This is regular content")
                    Image(
                        modifier = Modifier.requiredHeight(256.dp),
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "Compose Multiplatform icon"
                    )
                    Image(
                        modifier = Modifier.requiredHeight(256.dp),
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "Compose Multiplatform icon"
                    )
                    Spacer(Modifier.requiredHeight(128.dp))
                    Image(
                        modifier = Modifier.requiredHeight(256.dp),
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "Compose Multiplatform icon"
                    )
                    Spacer(Modifier.requiredHeight(64.dp))
                    Text("This is regular content")
                    Spacer(Modifier.requiredHeight(64.dp))
                    Text("This is regular content")
                    Text("This is regular content")
                    Spacer(Modifier.requiredHeight(64.dp))
                }

                // Bottom row with buttons
                Row(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Debug toggle button
                    KrystalDebugToggle()

                    // Add multiple KrystalButtons with different IDs for testing
                    KrystalButton(
                        onClick = { },
                    ) {
                        Text("Main Button")
                    }

                    KrystalButton(
                        onClick = { },
                    ) {
                        Text("Secondary Button")
                    }
                }
            }
        }
    }
}
