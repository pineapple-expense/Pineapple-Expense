package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.pineappleexpense.ui.components.UserScreenTemplate
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
    UserScreenTemplate(navController, viewModel) {
        UserArchiveList(archiveRows)
    }
}

// This will probably also require it's own NavHost implementation
// to dynamically add and navigate to the detailed previous expenses

@Preview
@Composable
fun PreviewArchive() {
    UserArchiveScreen(rememberNavController(), AccessViewModel())
}