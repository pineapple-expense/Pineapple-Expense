package com.example.pineappleexpense.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import java.math.RoundingMode
import java.util.*

//returns a card for each expense passed in
@Composable
fun expenseCardsList(
    expenses: List<Expense>,
    onCardClick: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    onAddToReport: (Expense) -> Unit,
    onRemoveFromReport: (Expense) -> Unit,
    isExpenseInReport: (Expense) -> Boolean,
    navController: NavHostController,
    viewModel: AccessViewModel
): List<@Composable () -> Unit> {
    // Local state for expanded card
    var expandedExpense by remember { mutableStateOf<Expense?>(null) }
    // Local state for fullscreen image
    var fullscreenImageUri by remember { mutableStateOf<String?>(null) }

    // Return a list of composable lambdas (each lambda represents one card)
    return expenses.map { expense ->
        {
            val inReport = isExpenseInReport(expense)
            Box {
                ExpenseCard(
                    expense = expense,
                    isExpanded = expandedExpense == expense,
                    onCardClicked = { clickedExpense ->
                        // Toggle expansion
                        expandedExpense = if (expandedExpense == clickedExpense) null else clickedExpense
                        onCardClick(clickedExpense)
                    },
                    onDeleteClicked = onDelete,
                    onAddToReportClicked = {
                        onAddToReport(expense)
                        expandedExpense = null
                    },
                    onRemoveFromReportClicked = { onRemoveFromReport(expense) },
                    inReport = inReport,
                    navController,
                    viewModel,
                    onImageClick = { uri -> fullscreenImageUri = uri }
                )

                // Show fullscreen image viewer if an image is selected
                fullscreenImageUri?.let { uri ->
                    FullscreenImageViewer(
                        imageUri = uri,
                        onClose = { fullscreenImageUri = null }
                    )
                }
            }
        }
    }
}


/**
 * A single expense card that toggles between a collapsed and expanded view.
 *
 * @param expense The expense data.
 * @param isExpanded True if the card is expanded.
 * @param onCardClicked Called when the card is clicked.
 * @param onDeleteClicked Called when the delete icon is clicked.
 * @param onAddToReportClicked Called when the "Add to Report" button is clicked.
 * @param onRemoveFromReportClicked Called when the "Remove from Report" button is clicked.
 * @param inReport True if the expense is currently in the report.
 */

@Composable
fun ExpenseCard(
    expense: Expense,
    isExpanded: Boolean,
    onCardClicked: (Expense) -> Unit,
    onDeleteClicked: (Expense) -> Unit,
    onAddToReportClicked: () -> Unit = {},
    onRemoveFromReportClicked: () -> Unit = {},
    inReport: Boolean,
    navController: NavHostController,
    viewModel: AccessViewModel,
    onImageClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCardClicked(expense) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = if (inReport)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inversePrimary)
        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (isExpanded) {
            ExpandedExpenseCard(
                expense = expense,
                onDeleteClicked = onDeleteClicked,
                onAddToReportClicked = onAddToReportClicked,
                onRemoveFromReportClicked = onRemoveFromReportClicked,
                inReport = inReport,
                onEditClicked = {
                    val expenseID = expense.id
                    navController.navigate("editExpense/$expenseID") {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel,
                onImageClick = onImageClick
            )
        } else {
            CollapsedExpenseCard(expense = expense, onImageClick = onImageClick)
        }
    }
}

//The collapsed (compact) view of an expense.
@Composable
fun CollapsedExpenseCard(expense: Expense, onImageClick: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular icon displaying the first letter of the category.
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = expense.category.firstOrNull()?.toString() ?: "",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Row {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Total: $${decimalFormat(expense.total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        expense.imageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(data = uri),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageClick(uri.toString()) },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}

//The expanded (detailed) view of an expense.
@Composable
fun ExpandedExpenseCard(
    expense: Expense,
    onDeleteClicked: (Expense) -> Unit,
    onAddToReportClicked: () -> Unit,
    onRemoveFromReportClicked: () -> Unit,
    inReport: Boolean,
    onEditClicked: () -> Unit = {},
    viewModel: AccessViewModel,
    onImageClick: (String) -> Unit = {}
) {
    val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(expense.date)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = expense.category.firstOrNull()?.toString() ?: "",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Row {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total: $${decimalFormat(expense.total)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        expense.imageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(data = uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageClick(uri.toString()) },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = expense.comment,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (inReport) {
                if (viewModel.currentReportExpenses.value.contains(expense)) {
                    Button(
                        onClick = onRemoveFromReportClicked,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Remove from Current Report",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAddToReportClicked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Add to Current Report",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        if (viewModel.getCurrentRole() != "Admin" &&
            viewModel.displayExpenses.contains(expense))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onDeleteClicked(expense) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Expense",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onEditClicked,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Expense"
                    )
                }
            }
    }
}
fun decimalFormat(value: Float): String {
    val df = DecimalFormat("#,###.##")
    df.roundingMode = RoundingMode.FLOOR
    val formatted = df.format(value)
    return formatted
}
//Deletes an image file from internal storage.
fun deleteImageFromInternalStorage(filePath: String): Boolean {
    val file = File(filePath)
    return file.exists() && file.delete()
}
