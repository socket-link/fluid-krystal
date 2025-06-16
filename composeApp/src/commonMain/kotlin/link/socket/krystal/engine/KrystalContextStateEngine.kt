package link.socket.krystal.engine

import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import link.socket.krystal.KrystalStyle

class KrystalContextStateEngine(
    val scope: CoroutineScope,
    val baseHazeState: HazeState,
    val baseKrystalSurfaceStyle: KrystalStyle.Surface,
) {
    private val _surfaceContextsFlow: MutableStateFlow<Map<String, KrystalSurfaceContext?>> =
        MutableStateFlow(mutableMapOf())

    fun getSurfaceContextFlow(id: String): StateFlow<KrystalSurfaceContext> =
        _surfaceContextsFlow.asStateFlow().map { contexts ->
            contexts[id] ?: KrystalSurfaceContext.empty(id, baseHazeState, baseKrystalSurfaceStyle)
        }.stateIn(
            scope,
            SharingStarted.Lazily,
            KrystalSurfaceContext.empty(id, baseHazeState, baseKrystalSurfaceStyle),
        )

    fun updateSurfaceContext(
        id: String,
        surfaceContent: ContentInfo? = null,
        surfaceStyle: KrystalStyle.Surface? = null,
    ) {
        val currentContext = _surfaceContextsFlow.value[id]
        val updatedContext = if (surfaceContent != null || surfaceStyle != null) {
            currentContext?.copy(
                surfaceId = id,
                surfaceContent = surfaceContent ?: currentContext.surfaceContent,
                surfaceStyle = surfaceStyle ?: currentContext.surfaceStyle,
            ) ?: KrystalSurfaceContext.empty(id, baseHazeState, baseKrystalSurfaceStyle)
        } else {
            currentContext
        }
        _surfaceContextsFlow.value = _surfaceContextsFlow.value.toMutableMap().apply {
            put(id, updatedContext)
        }
    }

    fun unregisterSurfaceContext(id: String) {
        _surfaceContextsFlow.value = _surfaceContextsFlow.value.toMutableMap().apply {
            remove(id)
        }
    }
}
