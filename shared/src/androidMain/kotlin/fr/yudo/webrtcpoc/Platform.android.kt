package fr.yudo.webrtcpoc

class AndroidPlatform : Platform {
    override val name: PlatformName = PlatformName.ANDROID
}

actual fun getPlatform(): Platform = AndroidPlatform()