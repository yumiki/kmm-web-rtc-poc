package fr.yudo.webrtcpoc

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthDataService(loginOnCreate: Boolean = true) {

    private val authDataSource by lazy { Firebase.auth }

    private val _isLogged = MutableStateFlow(authDataSource.currentUser != null)
    val isLogged = _isLogged.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("AuthContext"))
    init {
        if (loginOnCreate) {
            login()
        }
    }

    fun login() {
        if (isLogged.value) {
            return
        }
        scope.launch {
            internalLogin()
        }
    }

    private suspend fun internalLogin() {
        authDataSource.signInAnonymously()
    }
}