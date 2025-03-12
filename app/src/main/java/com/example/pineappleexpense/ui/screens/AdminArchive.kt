package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar

@Composable
fun AdminArchiveCard(report: Report, viewModel: AccessViewModel, navController: NavHostController, isSelected: Boolean, onSelect: (Report) -> Unit) {
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
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFEDE7F6),
                    )
                    .clickable {
                        onSelect(report)
                    },
                contentAlignment = Alignment.Center
            ) {
                if(isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = Color.Black
                    )
                }
            }
        }
    )
}

@Composable
fun AdminArchiveList(archiveRows: List<Report>, viewModel: AccessViewModel, navController: NavHostController, selected: List<Report>, onSelect: (Report) -> Unit) {
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
                    AdminArchiveCard(report, viewModel, navController, selected.contains(report), onSelect)
                }
            }
        }
    }
}

@Composable
fun AdminArchiveScreen(navController: NavHostController, viewModel: AccessViewModel) {
    var selected by remember { mutableStateOf<List<Report>>(emptyList()) }
    Scaffold (
        modifier = Modifier.fillMaxSize().testTag("AdminArchiveScreen"),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController, viewModel)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column {
                AdminArchiveList(viewModel.acceptedReports, viewModel, navController, selected, onSelect = { report ->
                    selected = if (selected.contains(report)) {
                        selected.filter { it != report }
                    } else {
                        selected + report
                    }
                })
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        //todo
                    },
                    modifier = Modifier
                        .padding(start = 15.dp, bottom = 15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected.isEmpty()) Color.Gray else Color(0xFF4E0AA6)
                    )
                ) {
                    Text("Generate CSV")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAdminArchive() {
    UserArchiveScreen(rememberNavController(), viewModel())
}