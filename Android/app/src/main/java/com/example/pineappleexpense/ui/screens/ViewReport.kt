package com.example.pineappleexpense.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.model.Report
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.components.deleteImageFromInternalStorage
import com.example.pineappleexpense.ui.components.expenseCardsList
import com.example.pineappleexpense.ui.components.expensesDateRange
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun ViewReportScreen(
    navController: NavHostController,
    viewModel: AccessViewModel,
    reportName: String,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var report: Report? = null
    val context = LocalContext.current
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
                ReportEmptyContent(viewModel, reportName, navController, true)
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

                // Check the comment left on the report
                val showDialog = remember { mutableStateOf(false) }
                val savedComment = report?.comment
                if (showDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        title = {
                            Text(
                                "Comment from Admin:",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        text = {
                            Text(
                                savedComment.toString(),
                                fontSize = 16.sp
                            )
                        },
                        confirmButton = {
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
                        dismissButton = {
                        },
                    )
                }

                if (report != null) {
                    if (report.comment != "") {
                            BadgedBox(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clickable { showDialog.value = true },
                                badge = {
                                    Badge(
                                        containerColor = Color.Red
                                    ) {
                                        val badgeNum = "!"
                                        Text(
                                            badgeNum,
                                            color = Color.White
                                        )
                                    }
                                })
                            {
                                Icon(
                                    imageVector = Icons.Default.MailOutline,
                                    contentDescription = "View report comment"
                                )
                            }
                    }
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

                if (reportName == "current") {
                    Button(
                        enabled = !isLoading,
                        onClick = {
                            isLoading = true                          // start spinner immediately
                            viewModel.submitReport { success ->
                                isLoading = false                    // stop spinner
                                if (success) {
                                    navController.navigate("Home") {
                                        launchSingleTop = true
                                        restoreState   = true
                                    }
                                } else {
                                    Toast
                                        .makeText(context, "submit report unsuccessful", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(38, 114, 42, 255)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) { Text("Submit Report for Review") }
                } else if (!viewModel.acceptedReports.contains(report)){
                    Column {
                        Button(
                            onClick = {
                                isLoading = true
                                viewModel.unsendAndDeleteReport(reportName) { success ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, "Report Removed Successfully", Toast.LENGTH_SHORT).show()
                                        navController.navigate("Home") {
                                            launchSingleTop = true
                                            restoreState   = true
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to delete report", Toast.LENGTH_SHORT).show()
                                    }
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

    if (isLoading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ReportEmptyContent(viewModel: AccessViewModel, reportName: String, navController: NavHostController, showUnsendButton: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(Modifier.size(20.dp))
        Text(
            text = "No expenses in this report",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.weight(1f))
        if(showUnsendButton) {
            Button(
                onClick = {
                    viewModel.unsendAndDeleteReport(reportName) { success ->
                        if (success) {
                            navController.navigate("Home") {
                                launchSingleTop = true
                                restoreState   = true
                            }
                        }
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
