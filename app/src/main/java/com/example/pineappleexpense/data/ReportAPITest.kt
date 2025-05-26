// change to trigger github action
package com.example.pineappleexpense.data

import android.util.Log
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

fun testFullReportLifecycle(viewModel: AccessViewModel) {
    val reportID = "test-report-" + System.currentTimeMillis()
    val receiptID = "test-receipt-" + System.currentTimeMillis()

    // Step 1: Create a remote report
    createReportRemote(viewModel, reportID, onSuccess = {
        Log.d("TestLifecycle", "Step 1: Report $reportID created successfully")

        // Step 2: Create a remote receipt
        createNewReceiptRemote(viewModel, receiptID, "25.00", "04/13/2024", "Test", "Test receipt",
            onSuccess = {
                Log.d("TestLifecycle", "Step 2: Receipt $receiptID created successfully")

                // Step 3: Get all unassigned receipts
                getUnassignedReceipts(viewModel, onSuccess = { receipts ->
                    Log.d("TestLifecycle", "Step 3: Retrieved ${receipts.size} unassigned receipts")

                    // Step 4: Add receipt to report
                    addReceiptToReportRemote(viewModel, receiptID, onSuccess = {
                        Log.d("TestLifecycle", "Step 4: Receipt $receiptID added to report $reportID")

                        // Step 5: Submit the report
                        submitReport(viewModel, onSuccess = {
                            Log.d("TestLifecycle", "Step 5: Report $reportID submitted successfully")

                            // Step 6: Get all submitted/returned reports
                            getSubmittedAndReturnedReports(viewModel, onSuccess = { reports ->
                                Log.d("TestLifecycle", "Step 6: Retrieved ${reports.size} submitted/returned reports")

                                // Step 7: Recall the report
                                recallReport(viewModel, reportID, onSuccess = {
                                    Log.d("TestLifecycle", "Step 7: Report $reportID recalled successfully")

                                    // Step 8: Try to get submitted/returned reports again (expect shorter list)
                                    getSubmittedAndReturnedReports(viewModel, onSuccess = { reportsAfterRecall ->
                                        if (reportsAfterRecall.size < reports.size) {
                                            Log.d("TestLifecycle", "Step 8: Retrieved ${reportsAfterRecall.size} submitted/returned reports after recall (expected)")

                                            // Step 9: Delete the report
                                            deleteReportRemote(viewModel, reportID, onSuccess = {
                                                Log.d("TestLifecycle", "Step 9: Report $reportID deleted successfully")

                                                // Step 10: Delete the receipt
                                                deleteReceiptRemote(viewModel, receiptID, onSuccess = {
                                                    Log.d("TestLifecycle", "Step 10: Receipt $receiptID deleted successfully")
                                                }, onFailure = { error ->
                                                    Log.e("TestLifecycle", "Step 10: Failed to delete receipt $receiptID: $error")
                                                })

                                            }, onFailure = { error ->
                                                Log.e("TestLifecycle", "Step 9: Failed to delete report $reportID: $error")
                                            })
                                        } else {
                                            Log.d("TestLifecycle", "Step 8: Retrieved ${reportsAfterRecall.size} submitted/returned reports after recall (unexpected)")
                                        }
                                    }, onFailure = { error ->
                                        Log.d("TestLifecycle", "Step 8: Failed to retrieve submitted reports after recall: $error")
                                    })
                                }, onFailure = { error ->
                                    Log.e("TestLifecycle", "Step 7: Failed to recall report $reportID: $error")
                                })
                            }, onFailure = { error ->
                                Log.e("TestLifecycle", "Step 6: Failed to get submitted reports: $error")
                            })
                        }, onFailure = { error ->
                            Log.e("TestLifecycle", "Step 5: Failed to submit report $reportID: $error")
                        })
                    }, onFailure = { error ->
                        Log.e("TestLifecycle", "Step 4: Failed to add receipt $receiptID to report $reportID: $error")
                    })
                }, onFailure = { error ->
                    Log.e("TestLifecycle", "Step 3: Failed to get unassigned receipts: $error")
                })
            }, onFailure = { error ->
                Log.e("TestLifecycle", "Step 2: Failed to create receipt $receiptID: $error")
            })
    }, onFailure = { error ->
        Log.e("TestLifecycle", "Step 1: Failed to create report $reportID: $error")
    })
}
