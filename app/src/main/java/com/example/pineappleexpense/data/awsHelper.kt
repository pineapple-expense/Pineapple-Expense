package com.example.pineappleexpense.data

import android.content.ContentResolver
import android.net.Uri
import android.content.Context
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.result.Credentials
import com.example.pineappleexpense.R
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.RequestBody
import okio.IOException
import okio.buffer
import okio.source
import org.json.JSONObject
import com.auth0.android.callback.Callback as auth0Callback
import okhttp3.Callback as okhttp3Callback
import java.io.File

fun getCredentialsManager(context: Context): SecureCredentialsManager {
    val auth0 = Auth0(
        context.getString(R.string.com_auth0_client_id),
        context.getString(R.string.com_auth0_domain)
    )
    return SecureCredentialsManager(
        context.applicationContext, // Use application context to prevent leaks
        AuthenticationAPIClient(auth0),
        SharedPreferencesStorage(context.applicationContext)
    )
}

fun fetchAccessToken(context: Context, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
    val credentialsManager = getCredentialsManager(context)

    credentialsManager.getCredentials(object : auth0Callback<Credentials, CredentialsManagerException> {
        override fun onSuccess(result: Credentials) {
            val token = result.accessToken
            Log.d("AUTH", "Token retrieved: $token")
            onSuccess(token)
        }

        override fun onFailure(error: CredentialsManagerException) {
            Log.e("AUTH", "Failed to get credentials: ${error.message}")
            onFailure(error.message ?: "Unknown error")
        }
    })
}

fun getReceiptUploadURL(viewModel: AccessViewModel, fileName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
    val client = OkHttpClient()
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/s3-presigned-url"
    val jsonMediaType = "application/json".toMediaType()
    val jsonBody = Gson().toJson(mapOf("fileName" to fileName))
    val requestBody = jsonBody.toRequestBody(jsonMediaType)
    val token = viewModel.getAccessToken()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : okhttp3Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("GetUploadURL", "Request failed: ${e.message}")
            onFailure(e.message ?: "Network error")
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) {
                    val errorBody = it.body.string()
                    Log.e("getUploadURL", "Request failed: $errorBody")
                    onFailure("Error: ${it.code} - $errorBody")
                    return
                }

                val responseBody = it.body.string()
                Log.d("NETWORK", "File name: $fileName")
                Log.d("NETWORK", "Presigned URL: $responseBody")
                try {
                    val jsonObject = JSONObject(responseBody)
                    val presignedUrl = jsonObject.getString("presignedUrl")  // Extract URL correctly
                    onSuccess(presignedUrl)
                } catch (e: Exception) {
                    onFailure("Invalid response format: ${e.message}")
                }
            }
        }
    })
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
    val client = OkHttpClient()
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/predictions"

    val jsonMediaType = "application/json".toMediaType()
    val jsonBody = Gson().toJson(mapOf(
        "receipt_id" to receiptId,
        "fname" to "John",
        "lname" to "Doe"
    ))
    val requestBody = jsonBody.toRequestBody(jsonMediaType)
    val token = viewModel.getAccessToken()
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : okhttp3Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("getPrediction", "Request failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) {
                    val errorBody = it.body.string()
                    Log.e("getPrediction", "Get prediction failed: $errorBody")
                    return
                }

                val json = JSONObject(response.body.string())

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
            }
        }
    })
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