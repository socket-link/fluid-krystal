@file:OptIn(ExperimentalUuidApi::class)

package link.socket.krystal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import link.socket.krystal.debug.AppForegroundContent
import link.socket.krystal.debug.DemoTab
import link.socket.krystal.debug.DemoTabSelector
import link.socket.krystal.debug.GlassGallery
import link.socket.krystal.debug.MusicDarkContent
import link.socket.krystal.debug.MusicLightContent
import link.socket.krystal.debug.SimpleListContent
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi

@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedTab by remember { mutableStateOf(DemoTab.GLASS_GALLERY) }

        Column {
            when (selectedTab) {
                DemoTab.SIMPLE_LIST -> {
                    val scrollState = rememberScrollState()

                    KrystalContainer(
                        modifier = Modifier.fillMaxHeight(0.9f),
                        scrollState = scrollState,
                        backgroundContent = { SimpleListContent(scrollState) },
                        foregroundContent = { AppForegroundContent() },
                        baseContainerStyle = krystalContainerStyleFor(ElevationLevel.L3),
                    )
                }

                DemoTab.MUSIC_LIGHT -> {
                    val scrollState = rememberScrollState()

                    KrystalContainer(
                        modifier = Modifier.fillMaxHeight(0.9f),
                        scrollState = scrollState,
                        backgroundContent = { MusicLightContent(scrollState) },
                        foregroundContent = { AppForegroundContent() },
                        baseContainerStyle = krystalContainerStyleFor(ElevationLevel.L4),
                    )
                }

                DemoTab.MUSIC_DARK -> {
                    val scrollState = rememberScrollState()

                    CompositionLocalProvider(LocalKrystalTokens provides KrystalTokens.DefaultDark) {
                        KrystalContainer(
                            modifier = Modifier.fillMaxHeight(0.9f),
                            scrollState = scrollState,
                            backgroundContent = { MusicDarkContent(scrollState) },
                            foregroundContent = { AppForegroundContent() },
                            baseContainerStyle = krystalContainerStyleFor(ElevationLevel.L4),
                        )
                    }
                }

                DemoTab.GLASS_GALLERY -> {
                    GlassGallery()
                }
            }

            DemoTabSelector(
                modifier = Modifier.requiredHeightIn(min = 56.dp),
                selectedTab = selectedTab,
                onTabSelected = { newTab ->
                    selectedTab = newTab
                    KrystalDebug.forceUpdate()
                }
            )
        }
    }
}
