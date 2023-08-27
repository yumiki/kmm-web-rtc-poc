package fr.yudo.webrtcpoc

import WebRTC.RTCVideoRendererProtocol
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.shepeliev.webrtckmp.VideoStreamTrack
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    media: Any,
    scalingTypeMatchOrientation: ScalingType,
    scalingTypeMismatchOrientation: ScalingType
) {
    when(media) {
        is VideoStreamTrack -> WebRTCPlayer(modifier, media)
    }
}
@Composable
expect fun rememberRTCVideoView(): RTCVideoRendererProtocol

@OptIn(ExperimentalForeignApi::class)
@Composable
fun WebRTCPlayer(
    modifier: Modifier,
    media: VideoStreamTrack
) {
    val videoView = rememberRTCVideoView()

    UIKitView(
        factory = {
            UIView().apply {
                insertSubview(videoView as UIView,0)
                videoView.translatesAutoresizingMaskIntoConstraints = false
                videoView.leadingAnchor.constraintEqualToAnchor(this.leadingAnchor).active = true
                videoView.trailingAnchor.constraintEqualToAnchor(this.trailingAnchor).active = true
                videoView.bottomAnchor.constraintEqualToAnchor(this.bottomAnchor).active = true

                videoView.topAnchor.constraintEqualToAnchor(this.topAnchor).active = true
            }
        },
        update = {
            media.addRenderer(videoView)
        },
        modifier = modifier
    )
}