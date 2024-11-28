package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

data class ArchiveRow(val startDate: String, val endDate: String, val total: String, val isApproved: Boolean)

@Composable
fun UserArchiveCard(archiveRow: ArchiveRow) {
    //Text(text = "Start Date: ${archiveRow.startDate}")
    ListItem(
        headlineContent = {Text("${archiveRow.startDate} - ${archiveRow.endDate}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)},
        supportingContent = {Text("Total: \$${archiveRow.total}")},
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
                Icon(
                    imageVector = if (archiveRow.isApproved) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = "Checked",
                    tint = Color.Black
                )
            }
        }
    )
}

@Composable
fun UserArchiveList(archiveRows: List<ArchiveRow>) {
    LazyColumn {
        items(archiveRows.size) { index ->
            UserArchiveCard(archiveRow = archiveRows[index])
        }
    }
}

@Composable
fun UserArchiveScreen(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    // Test data for the archive rows
    // Should get data from viewmodel once that has backend implementation
    val archiveRows = listOf(
        ArchiveRow("2023-01-01", "2023-01-31", "100.00", true),
        ArchiveRow("2023-02-01", "2023-02-28", "150.00", false)
    )
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
    ) { innerPadding ->
        TopBar(navController, viewModel, Modifier.padding(innerPadding))
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            UserArchiveList(archiveRows)
        }
    }
}
