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
import com.example.pineappleexpense.ui.screens.ArchiveScreen
import com.example.pineappleexpense.ui.screens.HomeScreen
import com.example.pineappleexpense.ui.screens.Settings
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
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController, viewModel)
        }
        composable("archive") {
            ArchiveScreen(navController, viewModel)
        }
        composable("userProfile") {
            UserProfile(navController, viewModel)
        }
        composable("settings") {
            Settings(navController, viewModel)
        }
        composable("adminProfile") {
            AdminProfile(navController,viewModel)
        }
        composable("adminSettings") {
            AdminSettings(navController, viewModel)
        }
    }
}

//currently hardcoded to always be the review page, will be changed
