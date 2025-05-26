package com.example.pineappleexpense.ui.components

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.material3.Button

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreview(onImageCapture: (ImageCapture) -> Unit) {

    //check if the app has camera permissions, if not then request camera permissions
    val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    if(!cameraPermissionState.status.isGranted) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "Please grant permission for the application to use the camera")
            Button(onClick = cameraPermissionState::launchPermissionRequest) {
                Text(text = "Grant permission")
            }
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // State to manage the PreviewView
    val previewView = remember { PreviewView(context) }

    // initialize an ImageCapture use case for capturing a picture
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Set up the CameraX UseCase
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        // Select the back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            // pass the ImageCapture instance to the caller after setting up the camera
            onImageCapture(imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Embed the PreviewView into Compose
    AndroidView(factory = { previewView })
}