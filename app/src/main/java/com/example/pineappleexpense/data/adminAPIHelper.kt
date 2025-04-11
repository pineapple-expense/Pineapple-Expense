package com.example.pineappleexpense.data

import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import org.json.JSONArray

data class AdminReport(
    val reportNumber: String,
    val totalAmount: Double,
    val name: String,
    val comment: String
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