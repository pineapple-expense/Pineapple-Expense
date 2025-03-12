@file:Suppress("DEPRECATION")

package com.example.pineappleexpense.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.BuildConfig
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.components.deleteImageFromInternalStorage
import com.example.pineappleexpense.ui.components.expenseCardsList
import com.example.pineappleexpense.ui.components.reportCardsList
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun HomeScreen(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val expenses = viewModel.displayExpenses
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
            val reportCards = reportCardsList (viewModel.pendingReports, navController, viewModel)
            val expenseCards = expenseCardsList(
                expenses = expenses,
                onCardClick = { },
                onDelete = { expense ->
                    expense.imageUri?.let { uri ->
                        deleteImageFromInternalStorage(uri.path ?: "")
                    }
                    viewModel.removeExpense(expense)
                    viewModel.removeFromCurrentReport(expense.id)
                },
                onAddToReport = { expense ->
                    viewModel.addToCurrentReport(expense.id)
                },
                onRemoveFromReport = { expense ->
                    viewModel.removeFromCurrentReport(expense.id)
                },
                isExpenseInReport = { expense ->
                    viewModel.currentReportExpenses.value.any { it.id == expense.id }
                },
                navController,
                viewModel
            )
            if (expenses.isEmpty()) {
                NoPendingExpensesCard(navController)
            }
            //put both the expense cards and report cards in the lazy column
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(expenseCards) { expenseCard ->
                    expenseCard()
                }

                items(reportCards) { reportCard ->
                    reportCard()
                }
            }

            Button(
                onClick = {
                    navController.navigate("viewReport/current") {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 15.dp, bottom = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentReportExpenses.value.isEmpty()) Color.Gray else Color(0xFF4E0AA6)
                )
            ) {
                Text("View Current Report")
            }
            if (BuildConfig.DEBUG) {
                Button(
                    onClick = {
                        val idToken = viewModel.getIdToken()
                        Log.d("DEBUG", "ID Token: $idToken")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 15.dp, bottom = 15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("TEST BUTTON")
                }
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

@Preview
@Composable
fun PreviewHome() {
    val navController = rememberNavController()
    val viewModel: AccessViewModel = viewModel()
    HomeScreen(navController, viewModel)
}