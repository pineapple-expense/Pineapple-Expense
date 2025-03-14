package com.example.pineappleexpense.data

import android.util.Log
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import org.json.JSONArray

// Create a new empty report on the server
fun createReportRemote(
    viewModel: AccessViewModel,
    reportID: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/CreateReport"
    val token = viewModel.getAccessToken()
    val name = viewModel.getUserName() ?: "Unknown"

    makeApiRequest(
        url = url,
        method = "PUT",
        headers = mapOf("Authorization" to "Bearer $token"),
        body = mapOf(
            "report_number" to reportID,
            "name" to name
            ),
        onSuccess = {
            Log.d("createReportRemote", "Successfully created report number $reportID")
            onSuccess()
        },
        onFailure = onFailure
    )
}

// Add a receipt to the current report on the server
fun addReceiptToReportRemote(
    viewModel: AccessViewModel,
    receiptID: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
)
{
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/AttachReceiptToCurrentReport"
    val token = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "PATCH",
        headers = mapOf("Authorization" to "Bearer $token"),
        body = mapOf("receipt_id" to receiptID),
        onSuccess = {
            Log.d("addReceiptToReportRemote", "Successfully added receipt $receiptID to current report")
            onSuccess()
        },
        onFailure = onFailure
    )
}

// Submit a report that's on the server
fun submitReport(
    viewModel: AccessViewModel,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/SubmitReport"
    val accessToken = viewModel.getAccessToken()

    makeApiRequest(
        url = url,
        method = "PATCH",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf(),
        onSuccess = {
            Log.d("submitReport", "Successfully submitted report")
            onSuccess()
        },
        onFailure = {
            Log.e("submitReport", "Failed to submit report: $it")
            onFailure(it)
        }
    )
}

// Delete a report from the server
fun deleteReportRemote(
    viewModel: AccessViewModel,
    reportID: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/DeleteReport"
    val accessToken = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "DELETE",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf("report_number" to reportID),
        onSuccess = {
            Log.d("deleteReport", "Successfully deleted report number $reportID")
            onSuccess()
        },
        onFailure = onFailure
    )
}

// Un-submit a report on the server
fun recallReport(
    viewModel: AccessViewModel,
    reportID: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/RecallReport"
    val accessToken = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "PATCH",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf("report_number" to reportID),
        onSuccess = {
            Log.d("recallReport", "Successfully recalled report number $reportID")
            onSuccess()
        },
        onFailure = onFailure
    )
}

// Get reports that have either been submitted or returned but not approved
fun getSubmittedAndReturnedReports(
    viewModel: AccessViewModel,
    onSuccess: (List<Pair<String, Double>>) -> Unit, // Returns a list of (reportNumber, totalAmount)
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/RetrieveSubmittedAndReturnedReports"
    val accessToken = viewModel.getAccessToken()

    makeApiRequest(
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        onSuccess = { responseBody ->
            try {
                val jsonArray = JSONArray(responseBody)
                val reports = mutableListOf<Pair<String, Double>>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val reportNumber = jsonObject.getString("report_number")
                    val totalAmount = jsonObject.getDouble("total")
                    reports.add(Pair(reportNumber, totalAmount))
                }
                onSuccess(reports)
            } catch (e: Exception) {
                onFailure("Invalid response format: ${e.message}")
            }
        },
        onFailure = onFailure
    )
}

// Get a list of approved reports
fun getApprovedReports(
    viewModel: AccessViewModel,
    onSuccess: (List<Pair<String, Double>>) -> Unit, // Returns a list of (reportNumber, totalAmount)
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/RetrieveApprovedReports"
    val accessToken = viewModel.getAccessToken()

    makeApiRequest(
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        onSuccess = { responseBody ->
            try {
                val jsonArray = JSONArray(responseBody)
                val reports = mutableListOf<Pair<String, Double>>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val reportNumber = jsonObject.getString("report_number")
                    val totalAmount = jsonObject.getDouble("total")
                    reports.add(Pair(reportNumber, totalAmount))
                }
                onSuccess(reports)
            } catch (e: Exception) {
                onFailure("Invalid response format: ${e.message}")
            }
        },
        onFailure = onFailure
    )
}

