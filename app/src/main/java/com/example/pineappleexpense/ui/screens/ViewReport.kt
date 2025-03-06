package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.ExpenseList
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.components.deleteImageFromInternalStorage
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun ViewReportScreen(
    navController: NavHostController,
    viewModel: AccessViewModel,
    reportName: String,
    modifier: Modifier = Modifier
) {
    val reportExpenses = if (reportName == "current") {
        viewModel.currentReportList.value
    } else {
        val report = viewModel.pendingReports.value.firstOrNull { it.name == reportName }
        if (report != null) {
            viewModel.expenseList.value.filter { expense ->
                report.expenseIds.contains(expense.id)
            }
        } else {
            emptyList()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ReportScreen"),
        topBar = { TopBar(navController, viewModel) },
        bottomBar = { BottomBar(navController, viewModel) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (reportExpenses.isEmpty()) {
                // If the report is empty, show a placeholder.
                ReportEmptyContent()
            } else {
                // Find date range and total
                val earliestDate = reportExpenses.minByOrNull { it.date }?.date
                val latestDate = reportExpenses.maxByOrNull { it.date }?.date

                val dateFormat = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                val earliestDateStr = earliestDate?.let { dateFormat.format(it) } ?: ""
                val latestDateStr = latestDate?.let { dateFormat.format(it) } ?: ""

                val totalSum = reportExpenses.map { it.total }.sum()

                // Display date range and total
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = modifier.height(8.dp))
                    Text(
                        text = "$earliestDateStr - $latestDateStr",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Total: $%.2f".format(totalSum),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Display the report expenses in a list.
                ExpenseList(
                    expenses = reportExpenses,
                    onCardClick = { },
                    onDelete = { expense ->
                        expense.imageUri?.let { uri ->
                            deleteImageFromInternalStorage(uri.path ?: "")
                        }
                        // Remove the expense from both the main list and the report list
                        viewModel.removeExpense(expense)
                        if (reportName == "current") {
                            viewModel.removeFromCurrentReport(expense.id)
                        }
                    },
                    onAddToReport = { },
                    onRemoveFromReport = { expense ->
                        if (reportName == "current") {
                            viewModel.removeFromCurrentReport(expense.id)
                        }
                    },
                    isExpenseInReport = { expense ->
                        reportExpenses.contains(expense)
                    },
                    navController
                )

                Spacer(modifier = Modifier.weight(1f))

                if (reportName == "current") {
                    Button(
                        onClick = {
                            viewModel.submitReport()
                            navController.navigate("Home") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(38, 114, 42, 255)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Submit Report for Review")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.unsendAndDeleteReport(reportName)
                            navController.navigate("Home") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Unsend and Delete Report")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportEmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No expenses in this report",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
