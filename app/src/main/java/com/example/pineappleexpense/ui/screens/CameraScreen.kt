package com.example.pineappleexpense.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.pineappleexpense.R
import com.example.pineappleexpense.data.PredictedDate
import com.example.pineappleexpense.data.Prediction
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.CameraPreview
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.rememberAsyncImagePainter

@Composable
fun CameraScreen(navController: NavHostController, viewModel: AccessViewModel) {
    val context = LocalContext.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var imageUriForPrediction by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(imageUriForPrediction) {
        imageUriForPrediction?.let { uri ->
            isLoading = true
            // Call the suspend function to simulate network delay and get prediction
            val prediction = mockGetPrediction(uri.toString())
            viewModel.currentPrediction = prediction
            isLoading = false
            // Navigate to the receipt preview page after prediction completes
            navController.navigate("Receipt Preview") {
                launchSingleTop = true
                restoreState = true
            }
            // Reset the trigger state
            imageUriForPrediction = null
        }
    }

    Scaffold (
        modifier = Modifier.fillMaxSize().testTag("CameraScreen"),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController,viewModel)
        }
    ) { innerPadding ->
        Column {
            Spacer(modifier = Modifier.height(80.dp))
            //display the camera preview and get the ImageCapture instance (if not loading)
            if (!isLoading) {
                CameraPreview(onImageCapture = { imageCapture = it })
            }
            //if loading display static image
            else if (viewModel.latestImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = viewModel.latestImageUri),
                    contentDescription = "Captured Image",
                    modifier = Modifier.fillMaxWidth().height(450.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
        //contains camera shutter button
        if(!isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(656.dp))
                IconButton(
                    //logic for capturing and saving a picture when the shutter button is pressed
                    onClick = {
                        //do nothing if imageCapture is null
                        val imageCapture = imageCapture ?: return@IconButton

                        //create the file to store the image
                        val photoFile = createImageFile(context)

                        //specify the file to save the image to
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        //initiate the image capture process and handle the result with a callback
                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            //create an anonymous class implementing ImageCapture.OnImageSavedCallback
                            //this allows us to define custom behavior for when the image is saved or if an error occurs
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    // Handle the saved image
                                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                    // Update ViewModel with the image URI
                                    viewModel.latestImageUri = savedUri

                                    imageUriForPrediction = savedUri
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    // Handle the error
                                    exception.printStackTrace()
                                }
                            }
                        )
                    },
                    modifier = Modifier.size(80.dp)
                ) {
                    //shutter icon
                    Icon(
                        painter = painterResource(id = R.drawable.camera_shutter),
                        contentDescription = "take picture",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }

        //gallery photo picker
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                // Create a new file into which the image will be copied.
                val newImageFile = createImageFile(context)
                try {
                    // Copy the image data from the gallery URI to the new file.
                    context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                        FileOutputStream(newImageFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    // Update the ViewModel with the new file's URI.
                    Uri.fromFile(newImageFile).let {
                        viewModel.latestImageUri = it
                        imageUriForPrediction = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        //gallery icon
        if(!isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(666.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(40.dp))
                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = { launcher.launch("image/*") }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.gallery_icon),
                            contentDescription = "choose from gallery",
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }

        //loading icon and text
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(300.dp))
                    androidx.compose.material3.CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Analyzing Receipt...", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// helper function to create the file object to store an image

private fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
    val filename = "IMG_$timestamp.jpg"
    val storageDir = File(context.filesDir, "images")
    if (!storageDir.exists()) {
        storageDir.mkdir()
    }
    return File(storageDir, filename)
}

suspend fun mockGetPrediction(imageURI: String): Prediction {
    delay(5000L)
    return Prediction(
        key = "",
        userId = "",
        category = "Dining",
        date = PredictedDate("2/26/2025", "2", "2025", "26"),
        amount = "128.23"
    )
}