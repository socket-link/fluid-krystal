package link.socket.krystal.engine

import dev.chrisbanes.haze.HazeState
import link.socket.krystal.KrystalStyle

data class KrystalSurfaceContext(
    val surfaceId: String,
    val surfaceHazeState: HazeState,
    val surfaceContent: ContentInfo,
    val surfaceStyle: KrystalStyle,
) {
    companion object {
        fun empty(
            id: String,
            baseHazeState: HazeState,
            baseKrystalStyle: KrystalStyle,
        ) = KrystalSurfaceContext(
            surfaceId = id,
            surfaceHazeState = baseHazeState,
            surfaceContent = ContentInfo.EMPTY,
            surfaceStyle = baseKrystalStyle
        )
    }
}
