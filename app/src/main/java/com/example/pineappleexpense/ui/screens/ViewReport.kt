package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
    modifier: Modifier = Modifier
) {
    val reportExpenses = viewModel.currentReportList.value

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ReportScreen"),
        topBar = { TopBar(navController, viewModel) },
        bottomBar = { BottomBar(navController, viewModel) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (reportExpenses.isEmpty()) {
                // If the report is empty, show a placeholder.
                ReportEmptyContent()
            } else {
                // Otherwise, display the report expenses in a list.
                ExpenseList(
                    expenses = reportExpenses,
                    onCardClick = { },
                    onDelete = { expense ->
                        expense.imageUri?.let { uri ->
                            deleteImageFromInternalStorage(uri.path ?: "")
                        }
                        // Remove the expense from both the main list and the report list
                        viewModel.removeExpense(expense)
                        viewModel.removeFromCurrentReport(expense.id)
                    },
                    onAddToReport = { },
                    onRemoveFromReport = { expense ->
                        viewModel.removeFromCurrentReport(expense.id)
                    },
                    isExpenseInReport = { expense ->
                        reportExpenses.contains(expense)
                    },
                    navController
                )
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
