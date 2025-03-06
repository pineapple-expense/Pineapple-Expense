package com.example.pineappleexpense.ui.components
import android.app.Application
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel


@Composable
fun TopBar(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val pagesWithBackButton = setOf("Receipt Preview", "Settings", "User Profile", "Profile", "Account Mapping", "Edit Expense")
    NavigationBar(

        modifier = Modifier.height(76.dp),
        containerColor = Color(0xFFF3DDFF),

        ) {
        NavigationBarItem(
            icon = {
                if(currentRoute in pagesWithBackButton || currentRoute?.startsWith("editExpense") == true || currentRoute?.startsWith("viewReport") == true){
                    //back button
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.padding(top = 32.dp).testTag("BackButton")
                    )
                } else {
                    //setting icon
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.padding(top = 32.dp).testTag("SettingsIcon")
                    )
                }
            },
            selected = false, //navController.currentDestination?.route == "Settings",
            onClick = {
                if(currentRoute in pagesWithBackButton || currentRoute?.startsWith("editExpense") == true || currentRoute?.startsWith("viewReport") == true) {
                    //navigate back
                    navController.popBackStack()
                } else {
                    if (navController.currentDestination?.route != "Settings") {
                        navController.navigate("Settings") {
                            //popUpTo(navController.graph.startDestinationId) { saveState = true } //seems to cause issues with the back button
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF3DDFF)),
            )
        Text(
            text = when {
                currentRoute?.startsWith("editExpense") == true -> "Edit Expense"
                currentRoute?.startsWith("viewReport") == true -> "View Report"
                else                                            -> "$currentRoute"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(top = 24.dp)
                .align(alignment = Alignment.CenterVertically)
        )
        //render account button on pages with no back button
        NavigationBarItem(
            icon = {
                if(currentRoute !in pagesWithBackButton) Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.padding(top = 32.dp).testTag("ProfileIcon")
                )
            },
            selected = navController.currentDestination?.route == "Settings",
            onClick = {
                if (navController.currentDestination?.route != "Profile" && currentRoute !in pagesWithBackButton) {
                    navController.navigate("Profile") {
                        //popUpTo(navController.graph.startDestinationId) { saveState = true } //seems to cause issues with the back button
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFF3DDFF))
        )
    } // End nav bar
}

@Composable
fun BottomBar(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier.height(80.dp),
        containerColor = Color(0xFFF3DDFF)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home", modifier.size(24.dp)) },
            label = { Text("Review") },
            selected = navController.currentDestination?.route == "Home",
            onClick = {
                if (navController.currentDestination?.route != "Home") {
                    navController.navigate("Home") {
                        //popUpTo(navController.graph.startDestinationId) { saveState = true } //seems to cause issues with the back button
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD6BBEA)),
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Add, contentDescription = "Camera") },
            label = { Text("Camera") },
            selected = navController.currentDestination?.route == "Camera",
            onClick = {
                if (navController.currentDestination?.route != "Camera") {
                    navController.navigate("Camera") {
                        //popUpTo(navController.graph.startDestinationId) { saveState = true } //seems to cause issues with the back button
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD6BBEA)),
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Archive") },
            label = { Text("Archive") },
            selected = navController.currentDestination?.route == "Archive",
            onClick = {
                if (navController.currentDestination?.route != "Archive") {
                    navController.navigate("Archive") {
                        //popUpTo(navController.graph.startDestinationId) { saveState = true } //seems to cause issues with the back button
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD6BBEA)),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewTopBotBar() {
    val navController = rememberNavController()
    val viewModel: AccessViewModel = viewModel()
    TopBar(navController, viewModel)

}