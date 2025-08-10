package link.socket.krystal.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun epochTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
