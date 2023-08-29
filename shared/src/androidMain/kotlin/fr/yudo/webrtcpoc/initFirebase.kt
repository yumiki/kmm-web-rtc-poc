package fr.yudo.webrtcpoc

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

actual fun Firebase.init(context: Any?) {
    Firebase.initialize(context)
}