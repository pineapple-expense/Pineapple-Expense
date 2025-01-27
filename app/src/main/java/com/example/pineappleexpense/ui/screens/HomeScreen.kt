@file:Suppress("DEPRECATION")

package com.example.pineappleexpense.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberImagePainter
import com.example.pineappleexpense.model.Expense
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.nio.file.WatchEvent
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.exp

@Composable
fun HomeScreen(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val expenses = viewModel.expenseList.value
    Scaffold (
        modifier = Modifier.fillMaxSize().testTag("HomeScreen"),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController,viewModel)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            //display the no expenses card if no image has been taken, otherwise display the image
            if(expenses.isEmpty()) {
                NoPendingExpensesCard(navController)
            }
            else {
                //display expenses
                ExpenseList(expenses, viewModel)
            }
            Button(
                onClick = {  },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 15.dp, bottom = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(expenses.isEmpty()) Color.Gray else Color(0xFF4E0AA6)
                )
            ) {
                Text("View Report")
            }
        }
    }
}

@Composable
fun NoPendingExpensesCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "You currently have no pending expenses",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        navController.navigate("Camera") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF6200EA)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "add a new item",
                        color = Color(0xFF6200EA),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseList(expenses: List<Expense>, viewModel: AccessViewModel) {
    //only one card can be expanded at a time, so it is tracked through the expense the card displays
    var expandedExpense by remember { mutableStateOf<Expense?>(null) }
    val context: Context = LocalContext.current

    LazyColumn {
        items(expenses) { expense ->
            ExpenseCard(
                expense = expense,
                isExpanded = expandedExpense == expense,
                //set the expanded card to whatever card is clicked (unless the card is already expanded, in which case un-expand the card)
                onCardClicked = { clickedExpense ->
                    expandedExpense = if (expandedExpense == clickedExpense) null else clickedExpense
                },
                onDeleteClicked = {
                    if(expense.imageUri != null) {
                        deleteImageFromInternalStorage(expense.imageUri.path ?: "")
                    }
                    viewModel.removeExpense(it)
                }
            )
        }
    }
}

fun deleteImageFromInternalStorage(filePath: String): Boolean {
    val file = File(filePath)
    return file.exists() && file.delete()
}

@Composable
fun ExpenseCard(
    expense: Expense,
    isExpanded: Boolean,
    onCardClicked: (Expense) -> Unit,
    onDeleteClicked: (Expense) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCardClicked(expense) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (isExpanded) {
            ExpandedExpenseCard(
                expense = expense,
                onDeleteClicked = onDeleteClicked
            )
        } else {
            CollapsedExpenseCard(expense)
        }
    }
}

@Composable
fun CollapsedExpenseCard(expense: Expense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //display the circle icon with first letter of category
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
            //display the title at the top
            Text(
                text = expense.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            //under the title display the category and total
            Row {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Total: $${expense.total}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        //display the image as an icon on the right of the card
        expense.imageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable
fun ExpandedExpenseCard(
    expense: Expense,
    onDeleteClicked: (Expense) -> Unit,
    onAddToReportClicked: () -> Unit = {},
    onEditClicked: () -> Unit = {}
) {
    //format the date
    val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(expense.date)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            //display the circle icon with first letter of category
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
                //display the title at the top
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                //under the title display the category and total
                Row {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total: $${expense.total}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        //display the image under the category and total
        expense.imageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        //display the formatted date
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        //display the comment
        Text(
            text = expense.comment,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
        //Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //display the add to report button
            Button(
                onClick = { onAddToReportClicked() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Add to Report",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //display the garbage icon on the far left
            IconButton(
                onClick = { onDeleteClicked(expense) },
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Expense",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.width(280.dp))
            //display the edit icon on the bottom right
            IconButton(
                onClick = onEditClicked,
                modifier = Modifier
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Expense"
                )
            }
        }
    }
}




@Preview
@Composable
fun PreviewHome() {
    val navController = rememberNavController()
    val viewModel: AccessViewModel = viewModel()
    HomeScreen(navController, viewModel)
}