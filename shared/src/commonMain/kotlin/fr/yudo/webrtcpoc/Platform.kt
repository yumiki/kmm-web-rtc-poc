package fr.yudo.webrtcpoc

interface Platform {
    val name: PlatformName
}

enum class PlatformName {
    ANDROID,
    IOS
}

expect fun getPlatform(): Platform