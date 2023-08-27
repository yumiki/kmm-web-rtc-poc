package fr.yudo.webrtcpoc

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.shepeliev.webrtckmp.VideoStreamTrack
import com.shepeliev.webrtckmp.WebRtc
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    media: Any,
    scalingTypeMatchOrientation: ScalingType,
    scalingTypeMismatchOrientation: ScalingType
) {
    val renderer = rememberVideoRenderer(media)

    val lifecycleEventObserver = remember(renderer, media) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    renderer.also {
                        it.prepare(media)
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    renderer.release()
                }

                else -> {
                    // ignore other events
                }
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, lifecycleEventObserver) {
        lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            renderer.release()
            lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            renderer.apply {
                setScalingType(scalingTypeMatchOrientation, scalingTypeMismatchOrientation)
            }.getView()
        },
    )
}
@Composable
fun <T>rememberVideoRenderer(media: T): VideoRenderer<T> {
    val context = LocalContext.current
    return remember(media) {
        createVideoRenderer(context, media)
    }
}


fun <T> createVideoRenderer(context: Context, media: T): VideoRenderer<T> =
    when (media) {
        is VideoStreamTrack -> WebRTCRenderer(context) as VideoRenderer<T>
        else -> DummyRenderer() as VideoRenderer<T>
    }

class DummyRenderer : VideoRenderer<Nothing> {
    override fun prepare(media: Nothing) {
        TODO("Not yet implemented")
    }

    override fun setScalingType(
        scalingTypeMatchOrientation: ScalingType,
        scalingTypeMismatchOrientation: ScalingType
    ) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun getView(): View {
        TODO("Not yet implemented")
    }
}

interface VideoRenderer<in T> {
    fun prepare(media: T)

    fun setScalingType(
        scalingTypeMatchOrientation: ScalingType,
        scalingTypeMismatchOrientation: ScalingType
    )

    fun release()
    fun getView(): View
}

class WebRTCRenderer(context: Context) : VideoRenderer<VideoStreamTrack>,
    SurfaceViewRenderer(context) {
    private var playingMedia: VideoStreamTrack? = null

    override fun prepare(media: VideoStreamTrack) {
        init(WebRtc.rootEglBase.eglBaseContext, null)
        playingMedia = media.also {
            it.addSinkCatching(this)
        }
    }

    override fun setScalingType(
        scalingTypeMatchOrientation: ScalingType,
        scalingTypeMismatchOrientation: ScalingType
    ) {
        super.setScalingType(
            scalingTypeMatchOrientation.toWebRTCScalingType(),
            scalingTypeMismatchOrientation.toWebRTCScalingType()
        )
    }

    override fun release() {
        playingMedia?.removeSinkCatching(this)
        playingMedia = null
        super.release()
    }

    override fun getView() = this

    private fun VideoStreamTrack.addSinkCatching(sink: VideoSink) {
        // runCatching as track may be disposed while activity was in pause mode
        runCatching { addSink(sink) }
    }

    private fun VideoStreamTrack.removeSinkCatching(sink: VideoSink) {
        // runCatching as track may be disposed while activity was in pause mode
        runCatching { removeSink(sink) }
    }

    private fun ScalingType.toWebRTCScalingType(): RendererCommon.ScalingType = when (this) {
        ScalingType.SCALE_ASPECT_BALANCED -> RendererCommon.ScalingType.SCALE_ASPECT_BALANCED
        ScalingType.SCALE_ASPECT_FIT -> RendererCommon.ScalingType.SCALE_ASPECT_FIT
        ScalingType.SCALE_ASPECT_FILL -> RendererCommon.ScalingType.SCALE_ASPECT_FILL
    }

}



