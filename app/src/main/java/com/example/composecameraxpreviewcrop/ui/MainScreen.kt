package com.example.composecameraxpreviewcrop.ui

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import com.example.composecameraxpreviewcrop.camera.CameraScreen
import com.example.composecameraxpreviewcrop.permission.NoPermissionScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    MainContent(
        hasPermission = cameraPermissionState.status.isGranted,
        onRequestPermission = cameraPermissionState::launchPermissionRequest
    )
}

@Composable
private fun MainContent(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    if (hasPermission) {
        CameraScreen()
    } else {
        NoPermissionScreen(onRequestPermission)
    }

    Text(
        text = "",
        modifier = Modifier.onGloballyPositioned {  }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainContent() {
    MainContent(
        hasPermission = true,
        onRequestPermission = {}
    )
}