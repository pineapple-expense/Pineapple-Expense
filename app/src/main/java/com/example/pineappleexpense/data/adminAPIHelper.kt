package com.example.pineappleexpense.data

import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

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
        method = "POST",
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
        method = "POST",
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
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/admin/CsvPresignedURL"
    val accessToken = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf("fileName" to fileName),
        onSuccess = { responseBody -> onSuccess(responseBody) },
        onFailure = { error -> onFailure("Request failed: $error") }
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
