package com.example.pineappleexpense.ui.screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.produceState
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
fun ViewPreviousCSVsScreen(
    navController: NavHostController,
    viewModel: AccessViewModel
) {
    val csvFiles = viewModel.csvFiles
    val context  = LocalContext.current

    Scaffold(
        modifier       = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        topBar         = { TopBar(navController, viewModel) },
        bottomBar      = { BottomBar(navController, viewModel) }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (csvFiles.isEmpty()) {
                // empty‐state info card
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                    ) {
                        Text(
                            "No CSV files to display",
                            Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(csvFiles.size) { idx ->
                        CsvFileCard(csvFile = csvFiles[idx], context = context)
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

    // 1) Load the CSV text from the content Uri
    val csvText by produceState(initialValue = "") {
        val inStream = context.contentResolver.openInputStream(csvFile.fileUri)
        value = inStream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    // 2) Split into data lines (skip header)
    val dataLines = csvText
        .lineSequence()
        .filter { it.isNotBlank() }
        .drop(1)
        .toList()

    // 3) Compute sum of column 1 and parse dates in column 2
    val totalSum = dataLines.mapNotNull { line ->
        line.split(',').getOrNull(1)?.toFloatOrNull()
    }.sum()

    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val dates = dataLines.mapNotNull { line ->
        line.split(',').getOrNull(2)?.let { dateStr ->
            runCatching { dateFormat.parse(dateStr) }.getOrNull()
        }
    }
    val dateRangeText = if (dates.isNotEmpty()) {
        val fmt     = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val minDate = dates.minOrNull()!!
        val maxDate = dates.maxOrNull()!!
        "${fmt.format(minDate)} – ${fmt.format(maxDate)}"
    } else {
        ""
    }

    Card(
        onClick  = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Date range
            if (dateRangeText.isNotEmpty()) {
                Text(dateRangeText, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            // Total
            Text("Total: ${"%.2f".format(totalSum)}",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            // Row count
            Text("Rows: ${dataLines.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray)

            // Expandable actions
            AnimatedVisibility(visible = expanded) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            // now pass the Uri instead of raw content
                            saveCsvToDownloads(context, csvFile.fileUri.toString())
                        },
                        Modifier.padding(end = 8.dp)
                    ) {
                        Text("Download")
                    }
                    Button(
                        onClick = {
                            shareCsvFile(context, csvFile.fileUri.toString())
                        }
                    ) {
                        Text("Share")
                    }
                }
            }
        }
    }
}
