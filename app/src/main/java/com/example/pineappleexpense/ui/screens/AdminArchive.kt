package com.example.pineappleexpense.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.model.Report
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AdminArchiveScreen(navController: NavHostController, viewModel: AccessViewModel) {
    var selected by remember { mutableStateOf<List<Report>>(emptyList()) } // Reports selected using the checklist
    var showMenu by remember { mutableStateOf(false) } // For the dropdown menu after clicking generate CSV button
    val context = LocalContext.current

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
                        if (selected.isNotEmpty()) showMenu = true
                    },
                    modifier = Modifier
                        .padding(start = 15.dp, bottom = 15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected.isEmpty()) Color.Gray else Color(0xFF4E0AA6)
                    )
                ) {
                    Text("Generate CSV")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Download CSV") },
                        onClick = {
                            showMenu = false

                            val expenses = viewModel.expenseList.value.filter { expense -> selected.any { it.expenseIds.contains(expense.id) } }
                            val csvContent = buildCsvContent(expenses)

                            // Download the CSV to the user's Downloads folder
                            saveCsvToDownloads(context, csvContent)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share CSV") },
                        onClick = {
                            showMenu = false

                            val expenses = viewModel.expenseList.value.filter { expense -> selected.any { it.expenseIds.contains(expense.id) } }
                            val csvContent = buildCsvContent(expenses)

                            // Share the CSV via the system share sheet
                            shareCsvFile(context, csvContent)
                        }
                    )
                }
            }
        }
    }
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

fun buildCsvContent(expenses: List<Expense>): String {
    // CSV Header
    val header = "Title,Total,Date,Comment,Category"
    val csvLines = mutableListOf(header)

    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    // Add each expense as a line
    expenses.forEach { expense ->
        val dateStr = dateFormat.format(expense.date)
        val line =
                "${escapeCsvField(expense.title)}," +
                "${expense.total}," +
                "$dateStr," +
                "${escapeCsvField(expense.comment)}," +
                escapeCsvField(expense.category)

        csvLines.add(line)
    }

    return csvLines.joinToString("\n")
}

private fun escapeCsvField(field: String): String {
    // If the field contains a comma or quote, wrap it in quotes and escape quotes inside.
    return if (field.contains(",") || field.contains("\"")) {
        "\"" + field.replace("\"", "\"\"") + "\""
    } else {
        field
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun saveCsvToDownloads(context: Context, csvContent: String) {
    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "expenses.csv")
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val fileUri = resolver.insert(collection, contentValues)

    if (fileUri != null) {
        // Write the CSV data to the output stream
        resolver.openOutputStream(fileUri)?.use { outputStream ->
            outputStream.write(csvContent.toByteArray())
            outputStream.flush()
        }
        // Mark file as not pending
        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(fileUri, contentValues, null, null)

        Toast.makeText(
            context,
            "CSV downloaded to your Downloads folder!",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        Toast.makeText(
            context,
            "Failed to create CSV in Downloads folder",
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun shareCsvFile(context: Context, csvContent: String) {
    // Define the file name and save location (using external files directory)
    val fileName = "expenses.csv"
    val file = File(context.getExternalFilesDir(null), fileName)
    file.writeText(csvContent)

    // Obtain a content URI using FileProvider
    val fileUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    // Create and launch a share intent (acts as a download/share dialog)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Download CSV"))
}

@Preview
@Composable
fun PreviewAdminArchive() {
    UserArchiveScreen(rememberNavController(), viewModel())
}