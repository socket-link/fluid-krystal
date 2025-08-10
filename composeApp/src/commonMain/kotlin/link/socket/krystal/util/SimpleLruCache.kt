package link.socket.krystal.util

internal class SimpleLruCache<K, V>(private val limit: Int) {

    private val map = mutableMapOf<K, CacheEntry<V>>()

    operator fun get(key: K): V? = map[key]?.also { it.updateAccessTime() }?.value

    operator fun set(key: K, value: V) {
        map[key] = CacheEntry(value)

        while (map.size > limit) {
            map.minByOrNull { it.value.lastAccessTime }?.also { map.remove(it.key) }
        }
    }
}

private class CacheEntry<V>(val value: V) {
    var lastAccessTime: Long = epochTimeMillis()

    fun updateAccessTime() {
        lastAccessTime = epochTimeMillis()
    }
}
