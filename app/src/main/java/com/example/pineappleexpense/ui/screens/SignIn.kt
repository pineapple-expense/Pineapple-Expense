package com.example.pineappleexpense.ui.screens


import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.components.TopBar
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel


@Composable
fun SignIn(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.height(256.dp))
            Text(
                text = "Pineapple Expense",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(64.dp))
            EmailTextField()

            Spacer(modifier = Modifier.height(32.dp))
            PasswordTextField()

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Don't have an account? Sign up here",
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            SignInButton(
                onClick = {
                    if (navController.currentDestination?.route != "Home") {
                        navController.navigate("Home") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                       }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun PreviewSignIn() {
    SignIn(rememberNavController(), AccessViewModel())
}


@Composable
fun EmailTextField() {
    var text by remember { mutableStateOf("")}
    TextField (
        value = text,
        onValueChange = { newText ->
            text = newText
        },
        label = { Text(text = "Email Address") },
        placeholder = { Text(text = "")},
    )
}

@Composable
fun PasswordTextField() {
    var text by remember { mutableStateOf("")}
    TextField (
        value = text,
        onValueChange = { newText ->
            text = newText
        },
        label = { Text(text = "Password") },
        placeholder = { Text(text = "")},
    )
}

@Composable
fun SignInButton(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Sign In")
    }
}




