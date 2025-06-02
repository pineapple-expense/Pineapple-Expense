package com.example.pineappleexpense.data

import android.app.Application
import android.content.Context
import android.net.Uri
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.source
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.os.Handler
import android.os.Looper

data class AdminReport(
    val reportNumber: String,
    val totalAmount: Double,
    val name: String,
    val comment: String
)

data class Expense(
    @SerializedName("receipt_id")    val receiptId: String,
    @SerializedName("user_id")       val userId: String,
    val name: String,
    @SerializedName("act_amount")    val actAmount: String,
    @SerializedName("act_date")      val actDate: String,
    @SerializedName("act_category")  val actCategory: String,
    val title: String?,
    val comment: String?,
    @SerializedName("report_number") val reportNumber: String,
    @SerializedName("created_at")    val createdAt: String
)

fun retrieveSubmittedReports(
    viewModel: AccessViewModel,
    onSuccess: (List<AdminReport>) -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/RetrieveSubmittedReports"
    val accessToken = viewModel.getAccessToken()

    makeApiRequest(
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        onSuccess = { responseBody ->
            try {
                val jsonArray = JSONArray(responseBody)
                val reports = mutableListOf<AdminReport>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val reportNumber = jsonObject.getString("report_number")
                    val totalAmount = jsonObject.getDouble("total")
                    reports.add(
                        AdminReport(
                        reportNumber = reportNumber,
                        totalAmount = totalAmount,
                        name = jsonObject.getString("name"),
                        comment = jsonObject.getString("comment")
                    )
                    )
                }
                onSuccess(reports)
            } catch (e: Exception) {
                onFailure("Invalid response format: ${e.message}")
            }
        },
        onFailure = onFailure
    )

}

fun approveReport(
    viewModel: AccessViewModel,
    userId: String,
    reportNumber: String,
    comment: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/ApproveReport"
    val accessToken = viewModel.getAccessToken()

    makeApiRequest(
        url = url,
        method = "PATCH",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf(
            "user_id" to userId,
            "report_number" to reportNumber,
            "comment" to comment
        ),
        onSuccess = { onSuccess() },
        onFailure = onFailure,
    )
}

fun returnReport(
    viewModel: AccessViewModel,
    userId: String,
    reportNumber: String,
    comment: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/ReturnReport"
    val accessToken = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "PATCH",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf(
            "user_id" to userId,
            "report_number" to reportNumber,
            "comment" to comment
        ),
        onSuccess = { onSuccess() },
        onFailure = { error -> onFailure("Request failed: $error") }
    )
}

fun getReportExpenses(
    viewModel: AccessViewModel,
    reportNumber: String,
    onSuccess: (List<Expense>) -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/RetrieveReportExpenseInformation"
    val accessToken = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf("report_number" to reportNumber),
        onSuccess = { responseBody ->
            try {
                // Parse the whole JSON string into a JsonObject
                val jsonObj = JsonParser.parseString(responseBody).asJsonObject
                // Extract the JSON array under "receipts"
                val receiptsJson = jsonObj.getAsJsonArray("receipts")
                // Create the TypeToken for List<Receipt>
                val listType = object : TypeToken<List<Expense>>() {}.type
                // Deserialize directly to List<Receipt>
                val receipts: List<Expense> = Gson().fromJson(receiptsJson, listType)
                onSuccess(receipts)
            } catch (e: Exception) {
                onFailure("Failed to parse receipts: ${e.message}")
            }
        },
        onFailure = { error ->
            onFailure("Request failed: $error")
        }
    )
}

fun getCSVUploadURL(
    viewModel: AccessViewModel,
    fileName: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val apiUrl      = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/CsvPresignedURL"
    val accessToken = viewModel.getAccessToken()

    makeApiRequest(
        url     = apiUrl,
        method  = "POST",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body    = mapOf("fileName" to fileName),
        onSuccess = { body ->
            try {
                // If we get a JSON object, pull out "presignedUrl" or "url"
                val cleaned = if (body.trim().startsWith("{")) {
                    val obj = JsonParser.parseString(body).asJsonObject
                    obj["presignedUrl"]?.asString
                        ?: obj["url"]?.asString
                        ?: throw IllegalStateException("No URL field in JSON")
                } else {
                    // otherwise assume plain string and strip stray quotes
                    body.trim().trim('"')
                }
                onSuccess(cleaned)
            } catch (e: Exception) {
                onFailure("Failed to parse presigned URL: ${e.message}")
            }
        },
        onFailure = { err -> onFailure("Request failed: $err") }
    )
}


fun uploadCsv(
    context: Context,
    viewModel: AccessViewModel,
    fileName: String,
    fileUri: Uri,
    onSuccess: () -> Unit = {},
    onFailure: (String) -> Unit = {}
) {
    // 1) get presigned PUT URL
    getCSVUploadURL(
        viewModel,
        fileName,
        onSuccess = { raw ->
            // trim stray quotes
            val uploadUrl = raw.trim().trim('"')

            // 2) build a RequestBody that streams from the Uri
            val body = object : RequestBody() {
                override fun contentType() = "text/csv".toMediaType()
                override fun contentLength(): Long =
                    context.contentResolver.openAssetFileDescriptor(fileUri, "r")
                        ?.length ?: -1L

                override fun writeTo(sink: okio.BufferedSink) {
                    context.contentResolver.openInputStream(fileUri)?.source().use { src ->
                        if (src == null) throw IOException("Unable to open $fileUri")
                        sink.writeAll(src)
                    }
                }
            }

            // 3) PUT it up to S3
            val request = Request.Builder()
                .url(uploadUrl)
                .put(body)
                .addHeader("Content-Type", "text/csv")
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure("Upload failed: ${e.message}")
                }
                override fun onResponse(call: Call, resp: Response) {
                    if (resp.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("Upload returned HTTP ${resp.code}")
                    }
                    resp.close()
                }
            })
        },
        onFailure = { err ->
            onFailure("Could not get upload URL: $err")
        }
    )
}



fun getCSVDownloadURL(
    viewModel: AccessViewModel,
    fileName: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/RetrieveCSVURL"
    val accesToken = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $accesToken"),
        body = mapOf("fileName" to fileName),
        onSuccess = { body ->
            try {
                // If we get a JSON object, pull out "presignedUrl" or "url"
                val cleaned = if (body.trim().startsWith("{")) {
                    val obj = JsonParser.parseString(body).asJsonObject
                    obj["presignedUrl"]?.asString
                        ?: obj["url"]?.asString
                        ?: throw IllegalStateException("No URL field in JSON")
                } else {
                    // otherwise assume plain string and strip stray quotes
                    body.trim().trim('"')
                }
                onSuccess(cleaned)
            } catch (e: Exception) {
                onFailure("Failed to parse presigned URL: ${e.message}")
            }
        },
        onFailure = { err -> onFailure("Request failed: $err") }
    )
}

private const val LIST_CSVS_URL =
    "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/GetCSVFileNames-Approver"

/**
 * Lists all CSV metadata objects, pulls out the "key" field, downloads each file,
 * saves them under filesDir/csvs/, and returns List<File> via onSuccess.
 */
/**
 * Fetches the CSV list, downloads each one, saves it locally, and returns a List<Uri>.
 */
fun downloadAllCsvFiles(
    viewModel: AccessViewModel,
    onSuccess: (List<Uri>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val context     = viewModel.getApplication<Application>()
    val mainHandler = Handler(Looper.getMainLooper())
    val token       = viewModel.getAccessToken()

    makeApiRequest(
        url     = LIST_CSVS_URL,
        method  = "GET",
        headers = mapOf("Authorization" to "Bearer $token"),
        onSuccess = { body ->
            // 1) parse the JSON { files: [ { key: "...", last_modified: "..."}, ... ] }
            val keys = try {
                JSONObject(body)
                    .getJSONArray("files")
                    .let { arr ->
                        List(arr.length()) { i ->
                            arr.getJSONObject(i).getString("key")
                        }
                    }
            } catch (e: Exception) {
                mainHandler.post { onFailure(Exception("Invalid list response: ${e.message}", e)) }
                return@makeApiRequest
            }

            if (keys.isEmpty()) {
                mainHandler.post { onSuccess(emptyList()) }
                return@makeApiRequest
            }

            // 2) prepare for downloads
            val csvDir    = File(context.filesDir, "csvs").apply { if (!exists()) mkdirs() }
            val client    = OkHttpClient()
            val uriList   = mutableListOf<Uri>()
            var remaining = keys.size

            // 3) download each CSV in parallel
            keys.forEach { key ->
                getCSVDownloadURL(
                    viewModel, key,
                    onSuccess = { rawUrl ->
                        val downloadUrl = rawUrl.trim().trim('"')
                        val req = Request.Builder().url(downloadUrl).get().build()

                        client.newCall(req).enqueue(object : Callback {
                            override fun onFailure(call: okhttp3.Call, e: IOException) {
                                mainHandler.post {
                                    onFailure(IOException("Failed to download $key: ${e.message}", e))
                                }
                            }

                            override fun onResponse(call: okhttp3.Call, resp: Response) = resp.use {
                                if (!it.isSuccessful) {
                                    mainHandler.post {
                                        onFailure(IOException("Download $key HTTP ${it.code}"))
                                    }
                                    return
                                }
                                // write to disk
                                val fileName = key.substringAfterLast('/')
                                val outFile  = File(csvDir, fileName).also { f ->
                                    it.body!!.byteStream().use { input ->
                                        FileOutputStream(f).use { out -> input.copyTo(out) }
                                    }
                                }
                                synchronized(uriList) {
                                    uriList.add(Uri.fromFile(outFile))
                                    remaining--
                                    if (remaining == 0) {
                                        mainHandler.post { onSuccess(uriList) }
                                    }
                                }
                            }
                        })
                    },
                    onFailure = { err ->
                        mainHandler.post {
                            onFailure(Exception("Could not get download URL for $key: $err"))
                        }
                    }
                )
            }
        },
        onFailure = { err ->
            mainHandler.post {
                onFailure(Exception("Could not list CSVs: $err"))
            }
        }
    )
}


