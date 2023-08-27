package fr.yudo.webrtcpoc

import WebRTC.RTCEAGLVideoView
import WebRTC.RTCVideoRendererProtocol
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberRTCVideoView(): RTCVideoRendererProtocol = remember {
    RTCEAGLVideoView()
}