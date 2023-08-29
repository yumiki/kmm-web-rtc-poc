package fr.yudo.webrtcpoc

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration


fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(emptyList())
    }.apply {
        if (getPlatform().name == PlatformName.IOS) {
            Firebase.initialize()
        }
        AuthDataService()
    }

// called by iOS etc
// fun initKoin() = initKoin(enableNetworkLogs = false) {}

fun KoinApplication.Companion.start(): KoinApplication = initKoin { }