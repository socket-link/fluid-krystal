package link.socket.krystal.engine

import dev.chrisbanes.haze.HazeState
import link.socket.krystal.KrystalStyle

data class KrystalSurfaceContext(
    val surfaceId: String,
    val surfaceHazeState: HazeState,
    val surfaceContent: ContentInfo,
    val surfaceStyle: KrystalStyle.Surface,
) {
    companion object {
        fun empty(
            id: String,
            baseHazeState: HazeState,
            baseKrystalStyle: KrystalStyle.Surface,
        ) = KrystalSurfaceContext(
            surfaceId = id,
            surfaceHazeState = baseHazeState,
            surfaceContent = ContentInfo.EMPTY,
            surfaceStyle = baseKrystalStyle
        )
    }
}
