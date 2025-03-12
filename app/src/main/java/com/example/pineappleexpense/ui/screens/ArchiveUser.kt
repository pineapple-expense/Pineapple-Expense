package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val total: Float = remember(report, viewModel.expenseList.value) {
        viewModel.expenseList.value
            .filter { report.expenseIds.contains(it.id) }
            .map { it.total }.sum()
    }

    ListItem(
        modifier = Modifier.clickable{
            navController.navigate("viewReport/${report.name}") {
                launchSingleTop = true
                restoreState = true
            }
        },
        headlineContent = {Text(report.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)},
        supportingContent = {Text("Total: $total")},
    )
}

@Composable
fun UserArchiveList(archiveRows: List<Report>, viewModel: AccessViewModel, navController: NavHostController) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Accepted Reports:",
                style = MaterialTheme.typography.titleMedium
            )
        }
        if(archiveRows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You have no accepted reports at this time",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        else {
            LazyColumn {

                items(archiveRows) { report ->
                    UserArchiveCard(report, viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun UserArchiveScreen(navController: NavHostController, viewModel: AccessViewModel) {
    UserScreenTemplate(navController, viewModel) {
        Column {
            UserArchiveList(viewModel.acceptedReports, viewModel, navController)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview
@Composable
fun PreviewArchive() {
    UserArchiveScreen(rememberNavController(), viewModel())
}