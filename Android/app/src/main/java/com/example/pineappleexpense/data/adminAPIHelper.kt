package com.example.pineappleexpense.data

import android.os.Handler
import android.os.Looper
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
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

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


/**
 * 1) fetches the presigned URL via getCSVUploadURL(...)
 * 2) PUTs the csvContent up to S3
 * 3) notifies you via onSuccess/onFailure
 */
fun uploadCsv(
    viewModel: AccessViewModel,
    fileName: String,
    csvContent: String,
    onSuccess: () -> Unit = {},
    onFailure: (String) -> Unit = {}
) {
    getCSVUploadURL(
        viewModel,
        fileName,
        onSuccess = { rawUrl ->
            val uploadUrl = rawUrl.trim().trim('"')   // strip stray quotes

            val mediaType = "text/csv".toMediaType()
            val body      = csvContent.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(uploadUrl)
                .put(body)
                .addHeader("Content-Type", "text/csv") // MUST match what was signed
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) =
                    onFailure("Upload failed: ${e.message}")

                override fun onResponse(call: Call, resp: Response) {
                    if (resp.isSuccessful) onSuccess()
                    else onFailure("Upload returned HTTP ${resp.code}")
                    resp.close()
                }
            })
        },
        onFailure = onFailure
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
        onSuccess = { responseBody -> onSuccess(responseBody) },
        onFailure = { error -> onFailure("Request failed: $error") }
    )
}

private const val LIST_CSVS_URL =
    "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/GetCSVFileNames-Approver"

fun downloadAllCsv(
    viewModel: AccessViewModel,
    onComplete: (List<Pair<String, String>>) -> Unit,
    onFailure: (String) -> Unit
) {
    val mainHandler = Handler(Looper.getMainLooper())
    val token       = viewModel.getAccessToken()

    // 1) fetch the list of filenames
    makeApiRequest(
        url     = LIST_CSVS_URL,
        method  = "GET",
        headers = mapOf("Authorization" to "Bearer $token"),
        onSuccess = { body ->
            // parse JSON { "files": [ "a.csv", "b.csv", … ] }
            val names = try {
                JsonParser
                    .parseString(body)
                    .asJsonObject
                    .getAsJsonArray("files")
                    .map { it.asString }
            } catch (e: Exception) {
                mainHandler.post { onFailure("Invalid list response: ${e.message}") }
                return@makeApiRequest
            }

            // nothing to do?
            if (names.isEmpty()) {
                mainHandler.post { onComplete(emptyList()) }
                return@makeApiRequest
            }

            // 2) download each file in parallel
            val results   = mutableListOf<Pair<String, String>>()
            var remaining = names.size
            val client    = OkHttpClient()

            names.forEach { fileName ->
                getCSVDownloadURL(
                    viewModel,
                    fileName,
                    onSuccess = { downloadUrl ->
                        // raw GET to S3
                        val req = Request.Builder()
                            .url(downloadUrl.trim().trim('"'))
                            .get()
                            .build()

                        client.newCall(req).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                mainHandler.post {
                                    onFailure("Failed to download $fileName: ${e.message}")
                                }
                            }

                            override fun onResponse(call: Call, resp: Response) {
                                if (!resp.isSuccessful) {
                                    mainHandler.post {
                                        onFailure("Download $fileName HTTP ${resp.code}")
                                    }
                                    return
                                }
                                val text = resp.body?.string().orEmpty()
                                synchronized(results) {
                                    results.add(fileName to text)
                                    remaining--
                                    if (remaining == 0) {
                                        mainHandler.post { onComplete(results) }
                                    }
                                }
                            }
                        })
                    },
                    onFailure = { err ->
                        mainHandler.post {
                            onFailure("Could not get URL for $fileName: $err")
                        }
                    }
                )
            }
        },
        onFailure = { err ->
            mainHandler.post { onFailure("Could not list CSVs: $err") }
        }
    )
}


