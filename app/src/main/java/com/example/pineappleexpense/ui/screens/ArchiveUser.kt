package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.model.Report
import com.example.pineappleexpense.ui.components.UserScreenTemplate
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun UserArchiveCard(report: Report, viewModel: AccessViewModel, navController: NavHostController) {
    val expenses = viewModel.expenseList.value.filter { report.expenseIds.contains(it.id) }

    ListItem(
        modifier = Modifier.clickable{
            navController.navigate("viewReport/${report.name}") {
                launchSingleTop = true
                restoreState = true
            }
        },
        headlineContent = {Text(report.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)},
        supportingContent = {Text("Total: \$${expenses.map { it.total }.sum()}")},
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFEDE7F6),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                    tint = Color.Black
                )
            }
        }
    )
}

@Composable
fun UserArchiveList(archiveRows: List<Report>, viewModel: AccessViewModel, navController: NavHostController) {
    LazyColumn {
        items(archiveRows) { report ->
            UserArchiveCard(report, viewModel, navController)
        }
    }
}

@Composable
fun UserArchiveScreen(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    UserScreenTemplate(navController, viewModel) {
        UserArchiveList(viewModel.acceptedReports, viewModel, navController)
    }
}

// This will probably also require it's own NavHost implementation
// to dynamically add and navigate to the detailed previous expenses

@Preview
@Composable
fun PreviewArchive() {
    UserArchiveScreen(rememberNavController(), viewModel())
}