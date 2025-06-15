package link.socket.krystal

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
