package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.example.pineappleexpense.model.Report
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.components.deleteImageFromInternalStorage
import com.example.pineappleexpense.ui.components.expenseCardsList
import com.example.pineappleexpense.ui.components.expensesDateRange
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun AdminViewReportScreen(
    navController: NavHostController,
    viewModel: AccessViewModel,
    reportName: String,
    modifier: Modifier = Modifier
) {
    var report: Report? = null
    val reportExpenses = if (reportName == "current") {
        viewModel.currentReportExpenses.value
    } else {
        report = viewModel.reportList.value.firstOrNull { it.name == reportName }
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
                ReportEmptyContent(viewModel, reportName, navController, false)
            } else {
                // Find date range and total
                val dateRange = expensesDateRange(reportExpenses)
                val totalSum = reportExpenses.map { it.total }.sum()

                // Display date range and total
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = modifier.height(8.dp))

                    // Row for date range and total on the same line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dateRange,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Total: $%.2f".format(totalSum),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Status displayed underneath
                    Text(
                        text = "Status: ${report?.status ?: "none"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Display the report expenses in a list.
                val expenseCards = expenseCardsList(
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
                    navController,
                    viewModel
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(expenseCards) { expenseCard ->
                        expenseCard()
                    }
                }

                // Dialog box for admin to add a comment to report
                val showDialog = remember { mutableStateOf(false) }

                var savedComment = report?.comment /** Store savedComment in report */
                if (showDialog.value) {
                    var comment by remember { mutableStateOf(savedComment.toString())}
                    AlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        title = {
                            Text("Comment")
                        },
                        text = {
                            OutlinedTextField(
                                value = comment,
                                onValueChange = { comment = it }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Blue
                                ),
                                onClick = {
                                    viewModel.setReportComment(report?.name.toString(), comment)
                                    showDialog.value = false
                                })
                            {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                ),
                                onClick = {
                                    showDialog.value = false
                                })
                            {
                                Text("Exit")
                            }
                        },
                    )
                }
                // Add comment button
                Button(
                    onClick = {
                        showDialog.value = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Comment on this report")
                }

                Row(
                    modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Approve button
                    Button(
                        onClick = {
                            report?.let { viewModel.acceptReport(it) } // Approve report, send to archive
                            navController.navigate("Admin Home") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text("Approve Report")
                    }
                    // Reject button
                    Button(
                        onClick = {
                            report?.let { viewModel.rejectReport(it) } // Reject report, user will see it on home screen
                            navController.navigate("Admin Home") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text("Reject Report")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportEmptyContent() {
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
