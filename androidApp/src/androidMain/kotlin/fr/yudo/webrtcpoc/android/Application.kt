package fr.yudo.webrtcpoc.android

import android.app.Application
import com.google.firebase.FirebaseApp
import fr.yudo.webrtcpoc.initKoin
import org.koin.android.ext.koin.androidContext

class PocApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initKoin {
            androidContext(androidContext = this@PocApplication)
        }
    }

}