package link.socket.krystal.debug

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import krystal.composeapp.generated.resources.Res
import krystal.composeapp.generated.resources.demo_dark
import org.jetbrains.compose.resources.painterResource

@Composable
fun MusicDarkContent(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(Res.drawable.demo_dark),
            contentDescription = "Demo Light Image"
        )
    }
}
