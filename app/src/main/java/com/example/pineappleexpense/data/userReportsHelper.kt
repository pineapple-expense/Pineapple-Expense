package com.example.pineappleexpense.data

import android.util.Log
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

// Create a new empty report on the server
fun createReportRemote(
    viewModel: AccessViewModel,
    reportID: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/CreateReport"
    val token = viewModel.getAccessToken()
    makeApiRequest(
        url = url,
        method = "PUT",
        headers = mapOf("Authorization" to "Bearer $token"),
        body = mapOf("report_number" to reportID),
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
    reportID: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/SubmitReport"
    val accessToken = viewModel.getAccessToken()

    makeApiRequest(
        url = url,
        method = "PATCH",
        headers = mapOf("Authorization" to "Bearer $accessToken"),
        body = mapOf("report_number" to reportID),
        onSuccess = {
            Log.d("submitReport", "Successfully submitted report number $reportID")
            onSuccess()
        },
        onFailure = onFailure
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

//fun getReturnedReports(
//    viewModel: AccessViewModel,
//    onSuccess: (List<String>) -> Unit,
//    onFailure: (String) -> Unit
//) {
//    val url = "https://mrmtdao1qh.execute-api.us-east-1.amazonaws.com/user/GetReturnedReports"
//}