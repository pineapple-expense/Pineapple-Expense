package com.example.pineappleexpense.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun UserScreenTemplate(
    navController: NavHostController,
    viewModel: AccessViewModel,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit) {
    Scaffold (
        modifier = Modifier.fillMaxSize().testTag("ArchiveScreen"),
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
            content(innerPadding)
        }
    }
}