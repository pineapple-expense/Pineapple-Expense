package com.example.pineappleexpense.data

import android.content.Context
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.result.Credentials
import com.example.pineappleexpense.R
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import com.auth0.android.callback.Callback as auth0Callback
import okhttp3.Callback as okhttp3Callback


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

fun getReceiptUploadURL(context: Context, fileName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
    fetchAccessToken(
        context,
        onSuccess = { token ->
            val client = OkHttpClient()
            val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/s3-presigned-url"

            val jsonMediaType = "application/json".toMediaType()
            val jsonBody = Gson().toJson(mapOf("fileName" to fileName))
            val requestBody = jsonBody.toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : okhttp3Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("NETWORK", "Request failed: ${e.message}")
                    onFailure(e.message ?: "Network error")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            val errorBody = it.body?.string()
                            Log.e("NETWORK", "Request failed: $errorBody")
                            onFailure("Error: ${it.code} - $errorBody")
                            return
                        }

                        val responseBody = it.body?.string()
                        Log.d("NETWORK", "Presigned URL: $responseBody")
                        responseBody?.let(onSuccess) ?: onFailure("Empty response")
                    }
                }
            })
        },
        onFailure = { error ->
            onFailure("Failed to get access token: $error")
        }
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

fun getPrediction(context: Context, receiptId: String, callback: (Prediction?) -> Unit) {
    fetchAccessToken(
        context,
        onSuccess = { token ->
            val client = OkHttpClient()
            val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/predictions"

            val jsonMediaType = "application/json".toMediaType()
            val jsonBody = Gson().toJson(mapOf("receipt_id" to receiptId))
            val requestBody = jsonBody.toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : okhttp3Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("NETWORK", "Request failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            val errorBody = it.body?.string()
                            Log.e("NETWORK", "Get prediction failed: $errorBody")
                            return
                        }

                        val json = JSONObject(response.body!!.string())

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
        },
        onFailure = { error ->
            Log.e("NETWORK","Failed to get access token: $error")
        }
    )
}
