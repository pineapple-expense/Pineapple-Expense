package com.example.pineappleexpense.data

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody
import okio.IOException
import okio.buffer
import okio.source
import org.json.JSONObject
import okhttp3.Callback as okhttp3Callback
import java.io.File

fun getReceiptUploadURL(viewModel: AccessViewModel, fileName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/s3-presigned-url"
    val token = viewModel.getAccessToken()

    makeApiRequest(
        url = url,
        method = "POST",
        headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json"
        ),
        body = mapOf("fileName" to fileName),
        onSuccess = { responseBody ->
            try {
                val jsonObject = JSONObject(responseBody)
                onSuccess(jsonObject.getString("presignedUrl"))
            } catch (e: Exception) {
                onFailure("Invalid response format: ${e.message}")
            }
        },
        onFailure = onFailure
    )
}


data class PredictedDate(
    val fullDate: String,
    val month: String,
    val year: String,
    val day: String
)

data class Prediction(
    val key: String,
    val userId: String,
    val category: String,
    val date: PredictedDate,
    val amount: String
)

fun getPrediction(viewModel: AccessViewModel, receiptId: String, callback: (Prediction?) -> Unit) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/predictions"
    val token = viewModel.getAccessToken()
    val name = viewModel.getUserName() ?: "Unknown"

    makeApiRequest(
        url = url,
        method = "POST",
        headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json"
        ),
        body = mapOf<String, Any>(
            "receipt_id" to receiptId,
            "name" to name
        ),
        onSuccess = { responseBody ->
            try {
                val json = JSONObject(responseBody)
                val predictedDateJson = json.getJSONObject("predicted_date")

                val prediction = Prediction(
                    key = json.getString("key"),
                    userId = json.getString("user_id"),
                    category = json.getString("predicted_category"),
                    date = PredictedDate(
                        fullDate = predictedDateJson.getString("full_date"),
                        month = predictedDateJson.getString("month"),
                        year = predictedDateJson.getString("year"),
                        day = predictedDateJson.getString("day")
                    ),
                    amount = json.getString("predicted_amount")
                )
                callback(prediction)
            } catch (e: Exception) {
                Log.e("getPrediction", "Parsing error: ${e.message}")
                callback(null)
            }
        },
        onFailure = { error ->
            Log.e("getPrediction", "Request failed: $error")
            callback(null)
        }
    )
}

fun uploadFileToS3(
    presignedUrl: String,
    fileUri: Uri,
    contentResolver: ContentResolver,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        val file = File(fileUri.path ?: throw IOException("Invalid file"))
        val fileSize = file.length()

        val requestBody = object : RequestBody() {
            override fun contentType() = contentResolver.getType(fileUri)?.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()

            override fun contentLength(): Long = fileSize

            override fun writeTo(sink: okio.BufferedSink) {
                contentResolver.openInputStream(fileUri)?.source()?.buffer()?.use { source ->
                    sink.writeAll(source)
                } ?: throw IOException("Unable to open input stream")
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .removeHeader("Expect")
                    .build()
                chain.proceed(request)
            }
            .build()

        val request = Request.Builder()
            .url(presignedUrl)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure("Upload failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMessage = response.body.string()
                    onFailure("Upload failed with error: $errorMessage")
                }
            }
        })

    } catch (e: Exception) {
        onFailure("Error uploading file: ${e.message}")
    }
}

fun processImageAndGetPrediction(
    viewModel: AccessViewModel,
    imageUri: Uri,
    contentResolver: ContentResolver,
    callback: (Prediction?) -> Unit
) {
    val fileName = imageUri.lastPathSegment ?: "image.jpg"
    getReceiptUploadURL(viewModel, fileName, onSuccess = { presignedUrl ->
        uploadFileToS3(presignedUrl, imageUri, contentResolver, onSuccess = {
            getPrediction(viewModel, fileName) { prediction ->
                callback(prediction)
            }
        }, onFailure = { error ->
            Log.e("UPLOAD", "Upload failed: $error")
        })
    }, onFailure = { error -> Log.e("PRESIGNED", "Presigned URL failed: $error") })
}