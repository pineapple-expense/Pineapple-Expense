package com.example.pineappleexpense.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Text
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHost
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.screens.Settings

// Testing the sidebar pop out here. Not sure how to implement good yet.
@Composable
fun SettingsSideBar(navController: NavHostController, viewModel: AccessViewModel, open:Boolean = false, modifier: Modifier = Modifier) {
        ModalDrawer(
            drawerContent = {
                    Column(
                        modifier = Modifier.fillMaxSize(.5f)
                    ) {
                            Text("Settings")
                            Text("PlaceHolder")
                            Text("PlaceHolder2")
                    }
            },
            drawerState = rememberDrawerState(if(open) DrawerValue.Open else DrawerValue.Closed) ,
            drawerBackgroundColor = Color(0xFFF9EEFF),
            content = {

            }
        )
}

@Preview
@Composable
fun ShowDrawer() {
    val navHost = rememberNavController()
    val viewModel = AccessViewModel()
    SettingsSideBar(navHost,viewModel, open = true)
}