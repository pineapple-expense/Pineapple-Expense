package com.example.pineappleexpense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.theme.PineappleExpenseTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pineappleexpense.ui.screens.AdminProfile
import com.example.pineappleexpense.ui.screens.AdminSettings
import com.example.pineappleexpense.ui.screens.UserArchiveScreen
import com.example.pineappleexpense.ui.screens.HomeScreen
import com.example.pineappleexpense.ui.screens.Registration
import com.example.pineappleexpense.ui.screens.Settings
import com.example.pineappleexpense.ui.screens.SignIn
import com.example.pineappleexpense.ui.screens.UserProfile
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PineappleExpenseTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val viewModel = AccessViewModel()

    // This maps out the layout of the pages
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "SignIn"
    ) {
        composable("SignIn") {
            SignIn(navController, viewModel)
        }
        composable("Registration") {
            Registration(navController, viewModel)
        }
        composable("Home") {
            HomeScreen(navController, viewModel)
        }
        composable("Archive") {
            UserArchiveScreen(navController, viewModel)
        }
        composable("Profile") {
            UserProfile(navController, viewModel)
        }
        composable("Settings") {
            Settings(navController, viewModel)
        }
        composable("Admin Profile") {
            AdminProfile(navController,viewModel)
        }
        composable("adminSettings") {
            AdminSettings(navController, viewModel)  // Will probably delete this
        }
        composable("camera") {
            //CameraScreen(navController, viewModel)
        }
    }
}
