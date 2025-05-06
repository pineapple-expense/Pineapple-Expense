package com.example.pineappleexpense.data

import android.util.Log
import com.example.pineappleexpense.BuildConfig
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import java.util.concurrent.TimeUnit

fun makeApiRequest(
    url: String,
    method: String = "POST",
    headers: Map<String, String> = emptyMap(),
    body: Map<String, Any>? = null,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    val jsonMediaType = "application/json".toMediaType()
    val jsonBody = body?.let { Gson().toJson(it).toRequestBody(jsonMediaType) }

    val requestBuilder = Request.Builder().url(url)

    headers.forEach { (key, value) ->
        requestBuilder.addHeader(key, value)
    }

    when (method.uppercase()) {
        "POST" -> requestBuilder.post(jsonBody ?: "".toRequestBody(jsonMediaType))
        "PUT" -> requestBuilder.put(jsonBody ?: "".toRequestBody(jsonMediaType))
        "PATCH" -> requestBuilder.patch(jsonBody ?: "".toRequestBody(jsonMediaType))
        "GET" -> requestBuilder.get()
        "DELETE" -> requestBuilder.delete()
    }

    val request = requestBuilder.build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            onFailure(e.message ?: "Network error")
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                val responseBodyString = it.body.string()
                if (!it.isSuccessful) {
                    onFailure("Error: ${it.code} - $responseBodyString")
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d("makeApiRequest", "Response: $responseBodyString")
                    }
                    onSuccess(responseBodyString)
                }
            }
        }
    })
}