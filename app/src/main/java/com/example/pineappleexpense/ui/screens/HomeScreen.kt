@file:Suppress("DEPRECATION")

package com.example.pineappleexpense.ui.screens

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
import androidx.compose.ui.Alignment
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.example.pineappleexpense.model.Expense
import java.nio.file.WatchEvent
import kotlin.math.exp

@Composable
fun HomeScreen(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val expenses = viewModel.expenseList.value
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController,viewModel)
        }
    ) { innerPadding ->

        val imageUri = viewModel.latestImageUri
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            //display the no expenses card if no image has been taken, otherwise display the image
            //TODO: make the home screen able to display multiple images
            if(imageUri == null) {
                NoPendingExpensesCard()
            }
            else {
                //display expenses
                ExpenseList(expenses)
            }
        }
    }
}

@Composable
fun NoPendingExpensesCard() {
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
                    onClick = { /* Handle add new item action */ },
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
fun ExpenseList(expenses: List<Expense>) {
    LazyColumn {
        items(expenses) { expense ->
            ExpenseItem(expense) // Render each item
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    Row (
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column (
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(expense.title)
            val total = expense.total
            Text("total:\t$$total")
            Text(expense.category)

            //vvv doesn't show the correct date for some reason
            //val date: String = expense.date.year.toString() + "-" + expense.date.month.toString() + "-" + expense.date.day.toString()

            //vvv currently shows the date really weirdly + the time, will fix later
            Text(expense.date.toString())
            Text(expense.comment)
        }
        Image(
            painter = rememberImagePainter(expense.imageUri),
            contentDescription = "Receipt",
            modifier = Modifier
                .size(100.dp)
                .padding(16.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview
@Composable
fun PreviewHome() {
    val navController = rememberNavController()
    val viewModel = AccessViewModel()
    HomeScreen(navController, viewModel)
}