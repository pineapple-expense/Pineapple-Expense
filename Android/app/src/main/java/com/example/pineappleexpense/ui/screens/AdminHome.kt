@file:Suppress("DEPRECATION")

package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.components.reportCardsList
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdminHome(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {

    // 1)  Refresh state ─ bind this to whatever your ViewModel exposes
    val refreshing by viewModel.isRefreshing.collectAsState(initial = false)

    // 2)  Remember a PullRefreshState
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { viewModel.fetchPendingReports() }   // <-- your refresh call
    )

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
            val reportCards = reportCardsList(viewModel.pendingReports, navController, viewModel)
            if (reportCards.isEmpty()) {
                AdminNoPendingReportsCard(navController)
            }
            //put both the expense cards and report cards in the lazy column
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(reportCards) { reportCard ->
                    reportCard()
                }
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
fun AdminNoPendingReportsCard(navController: NavHostController) {
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
                    text = "There are currently no pending reports to review.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewAdminHome() {
    val navController = rememberNavController()
    val viewModel: AccessViewModel = viewModel()
    HomeScreen(navController, viewModel)
}