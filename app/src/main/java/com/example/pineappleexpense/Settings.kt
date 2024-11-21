package com.example.pineappleexpense

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.pineappleexpense.components.BottomBar
import com.example.pineappleexpense.components.TopBar

// Place Holder
@Composable
fun Settings(navController: NavHostController) {
    Scaffold (
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController)
        },
    ) { innerPadding ->
        TopBar(navController, androidx.compose.ui.Modifier.padding(innerPadding))
        Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Spacer(modifier = androidx.compose.ui.Modifier.height(64.dp))
        }
        Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This is the Settings Screen",
                modifier = Modifier.padding(16.dp),
                fontSize = 24.sp
            )
        }
    }
}