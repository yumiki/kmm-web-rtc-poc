package fr.yudo.webrtcpoc

import WebRTC.RTCMTLVideoView
import WebRTC.RTCVideoRendererProtocol
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIViewContentMode

@Composable
actual fun rememberRTCVideoView(): RTCVideoRendererProtocol = remember {
    RTCMTLVideoView().apply {
        setContentMode(UIViewContentMode.UIViewContentModeScaleAspectFill)
    }
}