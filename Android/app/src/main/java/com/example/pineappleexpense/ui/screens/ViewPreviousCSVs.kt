package com.example.pineappleexpense.ui.screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel.CsvFile
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ViewPreviousCSVsScreen (navController: NavHostController, viewModel: AccessViewModel) {
    val csvFiles = viewModel.csvFiles
    val context = LocalContext.current
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController, viewModel)
        }
    ) {innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (csvFiles.isEmpty()) {
                // Show info card when no CSVs
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                    ) {
                        Text(
                            text = "No CSV files to display",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(csvFiles.size) { csvFile ->
                        CsvFileCard(csvFiles[csvFile], context)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvFileCard(
    csvFile: CsvFile,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }

    // Parse CSV contents: skip header, split lines
    val dataLines = csvFile.contents
        .lineSequence()
        .filter { it.isNotBlank() }
        .drop(1)
        .toList()

    // Totals
    val totalSum = dataLines.mapNotNull { line ->
        line.split(',').getOrNull(1)?.toFloatOrNull()
    }.sum()

    // Dates parsing
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val dates = dataLines.mapNotNull { line ->
        line.split(',').getOrNull(2)?.let { dateStr ->
            try {
                dateFormat.parse(dateStr)
            } catch (e: Exception) {
                null
            }
        }
    }
    val dateRangeText = if (dates.isNotEmpty()) {
        val minDate = dates.minOrNull()!!
        val maxDate = dates.maxOrNull()!!
        val fmt = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        "${fmt.format(minDate)} - ${fmt.format(maxDate)}"
    } else {
        ""
    }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date range first
            if (dateRangeText.isNotEmpty()) {
                Text(
                    text = dateRangeText,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            // Total
            Text(
                text = "Total: ${"%.2f".format(totalSum)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Number of rows
            Text(
                text = "Rows: ${dataLines.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { saveCsvToDownloads(context, csvFile.contents) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Download")
                    }
                    Button(
                        onClick = { shareCsvFile(context, csvFile.contents) }
                    ) {
                        Text("Share")
                    }
                }
            }
        }
    }
}


