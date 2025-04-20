package com.example.pineappleexpense.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.example.pineappleexpense.ui.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController, viewModel: AccessViewModel) {
    val currentRoute = navController.currentDestination?.route
    val pagesWithBackButton = setOf("Receipt Preview", "Settings", "Profile", "Admin Profile", "Account Mapping")
    val currentRouteHasBackButton = (currentRoute in pagesWithBackButton || currentRoute?.startsWith("editExpense") == true || currentRoute?.startsWith("viewReport") == true)
    val userState = viewModel.userState.collectAsState().value

    Box(
        modifier = Modifier
            .background(Color(0xFFF3DDFF))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        CenterAlignedTopAppBar(
            navigationIcon = {
                if (currentRouteHasBackButton) {
                    IconButton(
                        onClick = {
                            //delete image file if in receipt preview page
                            if(currentRoute == "Receipt Preview") {
                                viewModel.latestImageUri?.let { uri ->
                                    deleteImageFromInternalStorage(uri.path ?: "")
                                }
                                viewModel.latestImageUri = null
                            }
                            //navigate back
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.testTag("BackButton")
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (currentRoute != "Settings") {
                                navController.navigate("Settings") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.testTag("SettingsIcon")
                        )
                    }
                }
            },
            title = {
                Text(
                    text = when {
                        currentRoute?.startsWith("editExpense") == true -> "Edit Expense"
                        currentRoute?.startsWith("viewReport") == true -> "View Report"
                        currentRoute?.startsWith("adminViewReport") == true -> "Admin Report"
                        else -> "$currentRoute"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        if (currentRoute != "Profile" && currentRoute != "Admin Profile") {
                            if (userState == UserRole.Admin) {
                                navController.navigate("Admin Profile") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            else {
                                navController.navigate("Profile") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.testTag("ProfileIcon")
                    )
                }
            },
            colors = TopAppBarColors(
                containerColor = Color(0xFFF3DDFF),
                scrolledContainerColor = Color(0xFFF3DDFF),
                navigationIconContentColor = Color.DarkGray,
                titleContentColor = Color.DarkGray,
                actionIconContentColor = Color.DarkGray
            )
        )
    }
}


@Composable
fun BottomBar(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val userState = viewModel.userState.collectAsState().value
    NavigationBar(
        modifier = modifier.height(80.dp),
        containerColor = Color(0xFFF3DDFF)
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home", modifier.size(24.dp)) },
            label = { Text("Review") },
            selected = navController.currentDestination?.route == "Home",
            onClick = {
                if (navController.currentDestination?.route != "Home") {
                    if (userState == UserRole.Admin) {
                        navController.navigate("Admin Home") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    else {
                        navController.navigate("Home") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD6BBEA)),
            modifier = Modifier.padding(top = 16.dp)
        )
        if(userState == UserRole.User) {
            Spacer(modifier = Modifier.weight(1f))
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.Add, contentDescription = "Camera") },
                label = { Text("Camera") },
                selected = navController.currentDestination?.route == "Camera",
                onClick = {
                    if (navController.currentDestination?.route != "Camera") {
                        navController.navigate("Camera") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD6BBEA)),
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Archive") },
            label = { Text("Archive") },
            selected = navController.currentDestination?.route == "Archive",
            onClick = {
                if (navController.currentDestination?.route != "Archive") {
                    if(userState == UserRole.Admin) {
                        navController.navigate("Admin Archive") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    else if(userState == UserRole.User) {
                        navController.navigate("Archive") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD6BBEA)),
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Preview
@Composable
fun PreviewTopBotBar() {
    val navController = rememberNavController()
    val viewModel: AccessViewModel = viewModel()
    TopBar(navController, viewModel)
}