package fr.yudo.webrtcpoc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform