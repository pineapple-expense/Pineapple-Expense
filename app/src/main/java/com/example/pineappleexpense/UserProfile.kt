package com.example.pineappleexpense

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.pineappleexpense.components.BottomBar
import com.example.pineappleexpense.components.TopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfile(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF)
    ) { innerPadding ->
        TopBar(navController,Modifier.padding(innerPadding))
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
                    navController.navigate("adminProfile")
                }
            ) {

            }
        }
    }
}

@Composable
fun UserTopBar(navController: NavHostController, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFF3DDFF)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {

        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 24.sp,
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)

        )

    }
}

@Preview
@Composable
fun PreviewUserProfile() {
    val navController = rememberNavController()
    UserProfile(navController)
}