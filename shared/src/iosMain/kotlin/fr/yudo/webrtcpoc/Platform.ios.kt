package fr.yudo.webrtcpoc

class IOSPlatform: Platform {
    override val name: PlatformName = PlatformName.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()