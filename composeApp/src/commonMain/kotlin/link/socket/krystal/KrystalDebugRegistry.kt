package link.socket.krystal

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * A global registry to track all KrystalButton instances and their contexts
 */
object KrystalDebugRegistry {
    // Map of button ID to its current context
    private val buttonContexts = HashMap<String, KrystalContext>()

    // Mutable state to trigger recomposition when the registry changes
    private val _updateTrigger = mutableStateOf(0)
    val updateTrigger: State<Int> = _updateTrigger

    fun registerButton(id: String, context: KrystalContext) {
        buttonContexts[id] = context
        _updateTrigger.value++
        println("📋 Button registry updated: ${buttonContexts.size} buttons, trigger: $updateTrigger")
    }

    fun unregisterButton(id: String) {
        buttonContexts.remove(id)
        _updateTrigger.value++
        println("📋 Button unregistered: ${buttonContexts.size} buttons remaining, trigger: $updateTrigger")
    }

    fun getAllButtonContexts(): Map<String, KrystalContext> = buttonContexts.toMap()

    // Add a method to force updates
    fun forceUpdate() {
        _updateTrigger.value++
        println("📋 Forced registry update, trigger: $updateTrigger")
    }
}
