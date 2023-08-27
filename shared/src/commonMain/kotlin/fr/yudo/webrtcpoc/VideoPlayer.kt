package fr.yudo.webrtcpoc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.VideoStreamTrack
@Composable
expect fun VideoPlayer(
    modifier: Modifier = Modifier,
    media: Any,
    scalingTypeMatchOrientation: ScalingType,
    scalingTypeMismatchOrientation: ScalingType
)

enum class ScalingType {
    SCALE_ASPECT_BALANCED,
    SCALE_ASPECT_FIT,
    SCALE_ASPECT_FILL
}