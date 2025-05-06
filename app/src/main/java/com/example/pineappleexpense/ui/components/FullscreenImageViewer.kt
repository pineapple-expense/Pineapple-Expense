package com.example.pineappleexpense.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.rememberImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Composable
fun FullscreenImageViewer(
    imageUri: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            // Fullscreen image
            Image(
                painter = rememberImagePainter(
                    data = imageUri,
                    builder = {
                        crossfade(true)
                    }
                ),
                contentDescription = "Fullscreen Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top bar with close and download buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Download button
                IconButton(
                    onClick = {
                        if (!isDownloading) {
                            isDownloading = true
                            scope.launch {
                                try {
                                    val success = saveImage(context, imageUri)
                                    if (success) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Picture downloaded to Photos", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } finally {
                                    isDownloading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Download Image",
                            tint = Color.White,
                            modifier = Modifier.graphicsLayer(
                                rotationZ = 90f
                            )
                        )
                    }
                }

                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private suspend fun saveImage(context: Context, imageUri: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val drawable = context.imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(imageUri)
                    .build()
            ).drawable

            val bitmap = (drawable as? BitmapDrawable)?.bitmap

            bitmap?.let {
                val filename = "PineappleExpense_${System.currentTimeMillis()}.jpg"
                var fos: OutputStream? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Use MediaStore for Android 10 and above
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    context.contentResolver.let { resolver ->
                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        uri?.let { fos = resolver.openOutputStream(it) }
                    }
                } else {
                    // Use direct file access for older Android versions
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val image = File(imagesDir, filename)
                    fos = FileOutputStream(image)
                }

                fos?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                true
            } == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 