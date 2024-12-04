package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.components.UserScreenTemplate
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

data class ExpenseReport(val startDate: String, val endDate: String, val total: String)

@Composable
fun ExpenseReportCard(expenseReport: ExpenseReport) {
    //Text(text = "Start Date: ${archiveRow.startDate}")
    ListItem(
        headlineContent = {Text("${expenseReport.startDate} - ${expenseReport.endDate}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)},
        supportingContent = {Text("Total: \$${expenseReport.total}")},
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFEDE7F6),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                var isChecked = false
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
            }
        }
    )
}

@Composable
fun AdminCreateCSV(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val expenseReports = listOf(
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-01-01", "2023-01-31", "100.00"),
        ExpenseReport("2023-02-01", "2023-02-28", "150.00")
    )
    UserScreenTemplate(navController, viewModel) { innerPadding ->
        Box() {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(expenseReports.size) { index ->
                    ExpenseReportCard(expenseReport = expenseReports[index])
                }
            }
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(text = "Create CSV")
            }
        }
    }
}

@Preview
@Composable
fun PreviewCreateCSVBar() {
    val navController = rememberNavController()
    val viewModel = AccessViewModel()
    AdminCreateCSV(navController, viewModel)
}