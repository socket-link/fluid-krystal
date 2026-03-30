package link.socket.krystal.debug

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import krystal.composeapp.generated.resources.Res
import krystal.composeapp.generated.resources.compose_multiplatform
import krystal.composeapp.generated.resources.test
import link.socket.krystal.KrystalButton
import link.socket.krystal.KrystalStyle
import link.socket.krystal.api.GlassStyle
import link.socket.krystal.baseKrystalSurfaceStyle
import link.socket.krystal.engine.LocalKrystalContainerContext
import org.jetbrains.compose.resources.painterResource

@Composable
fun SimpleListContent(
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

// Replace this with cache management in the engine
private const val ONE_ID = "button_one"
private const val TWO_ID = "button_two"

@Composable
fun BoxScope.AppForegroundContent(
    baseSurfaceStyle: KrystalStyle.Surface = baseKrystalSurfaceStyle(),
) {
    Row(
        modifier = Modifier
            .align(TopCenter)
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 56.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val krystalContext = LocalKrystalContainerContext.current

        LaunchedEffect(baseSurfaceStyle) {
            krystalContext?.registerSurface(ONE_ID, GlassStyle.clear())
            krystalContext?.registerSurface(TWO_ID, GlassStyle.clear())
        }

        // Move capture into engine
        var isOnePressed by remember { mutableStateOf(false) }

        KrystalButton(
            modifier = Modifier
                .requiredSize(64.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isOnePressed = true
                            tryAwaitRelease()
                            isOnePressed = false
                        }
                    )
                },
            onClick = { },
        ) {
            Image(
                modifier = Modifier
                    .requiredSize(24.dp),
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back"
            )
        }

        // Move capture into engine
        var isTwoPressed by remember { mutableStateOf(false) }

        KrystalButton(
            modifier = Modifier
                .requiredWidth(112.dp)
                .requiredHeight(64.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isOnePressed = true
                            tryAwaitRelease()
                            isOnePressed = false
                        }
                    )
                },
            onClick = { },
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Image(
                    modifier = Modifier
                        .requiredSize(24.dp)
                        .rotate(-90f),
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back",
                )
                Spacer(modifier = Modifier.requiredWidth(24.dp))
                Image(
                    modifier = Modifier
                        .requiredSize(24.dp)
                        .rotate(90f),
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
        }
    }

    Text(
        modifier = Modifier
            .padding(top = 80.dp)
            .align(TopCenter),
        text = "Screen Title",
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
}
