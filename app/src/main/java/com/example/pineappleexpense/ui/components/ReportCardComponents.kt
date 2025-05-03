package com.example.pineappleexpense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.model.Report
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.example.pineappleexpense.ui.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.Locale


//returns a card for each report passed in
@Composable
fun reportCardsList(
    reports: List<Report>,
    navController: NavHostController,
    viewModel: AccessViewModel
): List<@Composable () -> Unit> {
    // Each report just maps to a ReportCard composable
    return reports.map { report ->
        {
            ReportCard(
                report = report,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}



// A single report card showing:
// - The report name,
// - The total amount of all expenses in the report,
// - The date range of the expenses in the report,
// - Navigates to a screen showing expenses in the report when clicked
@Composable
fun ReportCard(
    report: Report,
    navController: NavHostController,
    viewModel: AccessViewModel
) {
    val totalAmount = viewModel.expenseList.value.filter { report.expenseIds.contains(it.id) }.map { it.total }.sum()
    val dateRangeText = expensesDateRange(viewModel.expenseList.value.filter { report.expenseIds.contains(it.id) })
    val userState = viewModel.getCurrentRole()
    if (userState == "Admin") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    navController.navigate("adminViewReport/${report.name}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Report icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Folder Icon",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Report details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "report: ${report.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: $${"%.2f".format(totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateRangeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
    else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    navController.navigate("viewReport/${report.name}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Report icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (report.status == "Rejected")
                        Icon(
                            imageVector =  Icons.Default.Close,
                            contentDescription = "X Icon",
                            tint = Color.Red
                        )
                    else
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Folder Icon",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Report details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "report: ${report.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: $${"%.2f".format(totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateRangeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

//generate the date range string for a given report
fun expensesDateRange(reportExpenses: List<Expense>): String {
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val sortedExpenses = reportExpenses.sortedBy { it.date }

    return if (sortedExpenses.isNotEmpty()) {
        val startDate = dateFormatter.format(sortedExpenses.first().date)
        val endDate = dateFormatter.format(sortedExpenses.last().date)
        "$startDate - $endDate"
    } else {
        "No expenses"
    }
}
