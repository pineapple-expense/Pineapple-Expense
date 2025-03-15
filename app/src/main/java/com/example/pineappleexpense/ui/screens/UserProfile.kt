package com.example.pineappleexpense.ui.screens
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.MainActivity
import com.example.pineappleexpense.model.SharedPrefs
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import java.security.AccessController.getContext


@Composable
fun UserProfile(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier, logout : ()->Unit) {
    // Retrieve values from SharedPreferences
    val myemail = viewModel.getUserEmail()
    val name = viewModel.getUserName()
    val company = viewModel.getCompanyName()

    Scaffold (
        modifier = Modifier.fillMaxSize().testTag("UserProfile"),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController,viewModel)
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
                tint = Color(0xFF384B68),
                contentDescription = "" // Add a valid content description
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text (
                text = name.toString(),
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text (
                text = company.toString(),
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text (
                text = myemail.toString(),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(96.dp))
            Button(
                onClick = {
                    navController.navigate("Admin Profile")
                    viewModel.toggleAccess("Admin")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC56666), // Background color
                    contentColor = Color.White // Text or icon color
                )
            ) {
                Text(
                    text = "Switch to Admin View",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = logout,
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


/*
@Preview
@Composable
fun PreviewUserProfile() {
    val navController = rememberNavController()
    val viewModel = AccessViewModel()
    UserProfile(navController, viewModel)
}*/
