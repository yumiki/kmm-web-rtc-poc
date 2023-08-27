package fr.yudo.webrtcpoc

import androidx.compose.animation.Crossfade
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
fun App(room: RoomViewModel = viewModel(modelClass = RoomViewModel::class) { RoomViewModel() }) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebRTC KMP") }
            )
        }
    ) {

        val roomModel by room.model.collectAsState()

        Crossfade(targetState = roomModel) { model ->
            when (model.localStream) {
                null -> OpenMicrophoneAndCameraScreen(room)
                else -> VideoScreen(room)
            }
        }
    }
}