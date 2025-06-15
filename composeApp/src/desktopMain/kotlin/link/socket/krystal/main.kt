package link.socket.krystal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Krystal Demo",
    ) {
        MaterialTheme {
            val scrollState = rememberScrollState()
            
            KrystalContainer(
                modifier = Modifier
                    .padding(horizontal = 80.dp)
                    .fillMaxSize(),
                scrollState = scrollState,
                backgroundContent = {
                    AppBackgroundContent(scrollState)
                },
                foregroundContent = {
                    AppForegroundContent()
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        KrystalDebugToggle()
                    }
                },
            )
        }
    }
}
