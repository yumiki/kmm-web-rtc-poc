package fr.yudo.webrtcpoc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun OpenMicrophoneAndCameraScreen(room: Room) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        OpenCameraAndMicrophoneButton(onClick = room::openUserMedia)
    }
}

@Composable
private fun OpenCameraAndMicrophoneButton(onClick: () -> Unit) {

    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var isRationaleVisible by remember { mutableStateOf(false) }


    if (isRationaleVisible) {
        AlertDialog(
            text = { Text("Please grant camera and microphone permissions") },
            onDismissRequest = { isRationaleVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        with(controller) {
                            if (!isPermissionGranted(Permission.CAMERA)) {
                                providePermission(Permission.CAMERA)
                            }
                            if (!controller.isPermissionGranted(Permission.RECORD_AUDIO)) {
                                providePermission(Permission.RECORD_AUDIO)
                            }
                        }
                    }
                    isRationaleVisible = false
                }) {
                    Text("Grant permissions")
                }
            }
        )
    }

    Button(onClick = {
        coroutineScope.launch {
            when {
                controller.isPermissionGranted(Permission.CAMERA) && controller.isPermissionGranted(Permission.RECORD_AUDIO) -> {
                    //preferences.edit { putBoolean("should_open_app_settings", false) }
                    onClick()
                }

                else -> isRationaleVisible = true
            }
        }

    }) {
        Text("Open camera and microphone")
    }
}
