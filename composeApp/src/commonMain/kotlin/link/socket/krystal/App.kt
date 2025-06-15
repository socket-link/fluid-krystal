@file:OptIn(ExperimentalUuidApi::class)

package link.socket.krystal

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import krystal.composeapp.generated.resources.Res
import krystal.composeapp.generated.resources.compose_multiplatform
import krystal.composeapp.generated.resources.test
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scrollState = rememberScrollState()

        KrystalContainer(
            scrollState = scrollState,
            backgroundContent = { AppBackgroundContent(scrollState) },
            foregroundContent = { AppForegroundContent() },
        )
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
        Spacer(Modifier.requiredHeight(200.dp))
        Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                modifier = Modifier
                    .weight(1f)
                    .requiredHeight(256.dp),
                painter = painterResource(Res.drawable.compose_multiplatform),
                contentDescription = "Compose Multiplatform icon"
            )
            Image(
                modifier = Modifier
                    .weight(1f)
                    .requiredHeight(256.dp),
                painter = painterResource(Res.drawable.compose_multiplatform),
                contentDescription = "Compose Multiplatform icon"
            )
        }
        Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        Spacer(Modifier.requiredHeight(128.dp))
        Image(
            modifier = Modifier.requiredHeight(512.dp),
            painter = painterResource(Res.drawable.test),
            contentDescription = "Compose Multiplatform icon"
        )
        Spacer(Modifier.requiredHeight(64.dp))
        Text("This is regular content")
        Image(
            modifier = Modifier.requiredHeight(256.dp),
            painter = painterResource(Res.drawable.test),
            contentDescription = "Compose Multiplatform icon"
        )
        Spacer(Modifier.requiredHeight(64.dp))
        Text("This is regular content")
        Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        Spacer(Modifier.requiredHeight(64.dp))
    }
}

@Composable
fun BoxScope.AppForegroundContent() {
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .wrapContentWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KrystalButton(
            modifier = Modifier
                .padding(vertical = 8.dp),
            onClick = { },
        ) {
            Icon(
                modifier = Modifier
                    .requiredSize(32.dp),
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back"
            )
        }

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = "Screen Title",
            style = MaterialTheme.typography.titleLarge,
        )

        KrystalButton(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .wrapContentWidth(),
            onClick = { },
        ) {
            Row {
                Icon(
                    modifier = Modifier
                        .requiredSize(32.dp),
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back"
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Icon(
                    modifier = Modifier
                        .requiredSize(32.dp),
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
        }
    }
}
