package fr.yudo.webrtcpoc

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.shepeliev.webrtckmp.videoTracks

@Composable
fun VideoScreen(room: Room) {
    val roomModel by room.model.collectAsState()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(modifier = Modifier.fillMaxSize()) {
            val remoteStream = roomModel.remoteStream

            val animatedWeight by animateFloatAsState(
                targetValue = remoteStream?.let { 1f } ?: 0.01f
            )

            remoteStream?.let {
                VideoPlayer(
                    media = it.videoTracks.first(),
                    modifier = Modifier.weight(animatedWeight),
                    scalingTypeMatchOrientation = ScalingType.SCALE_ASPECT_FILL,
                    scalingTypeMismatchOrientation = ScalingType.SCALE_ASPECT_FILL,
                )
            }

            roomModel.localStream?.let {
                VideoPlayer(
                    media = it.videoTracks.first(),
                    modifier = Modifier.weight(1f),
                    scalingTypeMatchOrientation = ScalingType.SCALE_ASPECT_FILL,
                    scalingTypeMismatchOrientation = ScalingType.SCALE_ASPECT_FILL,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Crossfade(targetState = roomModel) {
                when {
                    it.isJoining -> CircularProgressIndicator()

                    it.roomId != null -> {
                        val roomId = roomModel.roomId
                        val clipboardManager = LocalClipboardManager.current

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Room ID: $roomId", color = Color.White)

                            IconButton(onClick = {
                                val text = buildAnnotatedString { append(roomId!!) }
                                clipboardManager.setText(text)
                                /*Toast.makeText(context, "Room ID is copied.", Toast.LENGTH_SHORT)
                                    .show()*/ // TODO replace
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Copy room ID",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = room::switchCamera) {
                    Text("Switch camera")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (roomModel.roomId == null) {
                        Button(onClick = room::createRoom, enabled = !roomModel.isJoining) {
                            Text("Create")
                        }

                        JoinRoomButton(onJoin = room::joinRoom, enabled = !roomModel.isJoining)
                    }

                    Button(onClick = room::hangup) {
                        Text("Hangup")
                    }
                }
            }
        }
    }
}
