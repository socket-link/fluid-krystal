package link.socket.krystal

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import krystal.composeapp.generated.resources.Res
import krystal.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scrollState = rememberScrollState()

        KrystalContainer(
            scrollState = scrollState,
            foregroundContent = { AppForegroundContent() },
            backgroundContent = { AppBackgroundContent(scrollState) },
        )
    }
}

@Composable
fun AppForegroundContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        KrystalButton(
            modifier = Modifier
                .weight(1f),
            onClick = { },
        ) {
            Text("Main Button")
        }

        KrystalButton(
            modifier = Modifier
                .weight(1f),
            onClick = { },
        ) {
            Text("Secondary Button")
        }
    }
}

@Composable
fun AppBackgroundContent(
    scrollState: ScrollState,
) {
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

        KrystalButton(
            modifier = Modifier.weight(1f),
            onClick = { },
        ) {
            Text("Main Button")
        }
    }
}
