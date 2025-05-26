@file:Suppress("DEPRECATION")

package com.example.pineappleexpense.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.BuildConfig
import com.example.pineappleexpense.data.updateReceiptRemote
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.components.deleteImageFromInternalStorage
import com.example.pineappleexpense.ui.components.expenseCardsList
import com.example.pineappleexpense.ui.components.reportCardsList
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val refreshing by viewModel.isRefreshing.collectAsState(initial = false)

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { viewModel.updatePendingReports() }
    )

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
                .pullRefresh(pullRefreshState)
        ) {
            val pendingReports = reportCardsList (viewModel.pendingReports, navController, viewModel)
            val rejectedReports = reportCardsList (viewModel.rejectedReports, navController, viewModel)
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
            if (expenses.isEmpty() && rejectedReports.isEmpty() && pendingReports.isEmpty()) {
                NoPendingExpensesCard(navController)
            } else {
                //put both the expense cards and report cards in the lazy column
                // Rejected reports will appear at the top
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(rejectedReports) { reportCard ->
                        reportCard()
                    }
                    items(pendingReports) { reportCard ->
                        reportCard()
                    }
                    items(expenseCards) { expenseCard ->
                        expenseCard()
                    }
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
                DebugMenu(viewModel, navController)
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter) // sits just under the app bar
            )
        }
    }
}

@Composable
fun NoPendingExpensesCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "You currently have no pending expenses",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.height(24.dp).width(100.dp).clickable{
                    navController.navigate("Camera") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Text(
                    text = "add a new item",
                    color = Color(0xFF6200EA),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun DebugMenu(viewModel: AccessViewModel, navController: NavHostController) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(end = 15.dp, bottom = 15.dp),
        contentAlignment = Alignment.BottomEnd
    ) {

        Box {

            Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        ) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.align(AbsoluteAlignment.TopRight)
        ) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Log Auth Token") },
                onClick = {
                    Log.d("DevMode", "${viewModel.getAccessToken()}")
                    expanded = false
                }
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Log ID Token") },
                onClick = {
                    Log.d("DevMode", "${viewModel.getIdToken()}")
                    expanded = false
                }
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Reprogrammable Button") },
                onClick = {
                    updateReceiptRemote(
                        viewModel = viewModel,
                        receiptId = "IMG_20250505_200647_126.jpg",
                        amount = "19.99",
                        date = "05/19/1999",
                        category = "Travel",
                        title = "hihello",
                        comment = "Please work",
                        onSuccess = { Log.d("DevMenu", "Success") },
                        onFailure = { Log.e("DevMenu", it) }
                    )
                    expanded = false
                }
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Add other debug features here!") },
                onClick = {
                    Toast.makeText(context, "I'm a toast, I don't do anything yet!", Toast.LENGTH_SHORT).show()
                    expanded = false
                }
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