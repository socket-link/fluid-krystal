package link.socket.krystal

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect

object KrystalDebug {
    private val buttonBounds = HashMap<String, Rect>()

    private val _updateTrigger = mutableStateOf(0)
    val updateTrigger: State<Int> = _updateTrigger

    fun registerButton(
        id: String,
        bounds: Rect,
    ) {
        buttonBounds[id] = bounds
        _updateTrigger.value++
        println("📋 Button registry updated: ${buttonBounds.size} buttons, trigger: $updateTrigger")
    }

    fun unregisterButton(id: String) {
        buttonBounds.remove(id)
        _updateTrigger.value++
        println("📋 Button unregistered: ${buttonBounds.size} buttons remaining, trigger: $updateTrigger")
    }

    fun getAllButtonBounds(): Map<String, Rect> = buttonBounds.toMap()

    fun forceUpdate() {
        _updateTrigger.value++
        println("📋 Forced registry update, trigger: $updateTrigger")
    }
}
