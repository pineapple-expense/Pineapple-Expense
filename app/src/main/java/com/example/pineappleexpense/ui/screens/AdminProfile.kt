package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel



@Composable
fun AdminProfile(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier, logout: ()->Unit) {
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        topBar = {
            TopBar(navController, viewModel)
        },
        bottomBar = {
            BottomBar(navController, viewModel)  // Placeholder until we have our Admin screens laid out
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(96.dp))
            Icon(
                Icons.Default.AccountCircle,
                modifier = Modifier.size(144.dp),
                tint = Color(0xFFC56666),
                contentDescription = "User Icon"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text (
                text = "User FName",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text (
                text = "User LName",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text (
                text = "useremail@email.com",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(96.dp))
            Button(
                onClick = {
                    navController.navigate("Profile")
                    viewModel.toggleAccess("User")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF384B68), // Background color
                    contentColor = Color.White // Text or icon color
                )
            ) {
                Text(
                    text = "Switch to User View",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick =
                    logout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF548235), // Background color
                    contentColor = Color.White // Text or icon color
                )
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

            }
        }

    }
}



