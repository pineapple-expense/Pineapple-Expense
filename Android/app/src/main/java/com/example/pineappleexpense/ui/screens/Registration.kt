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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun Registration(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.height(128.dp))
            Text(
                text = "Pineapple Expense",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = "Enter your information below",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
            FirstNameBox()

            Spacer(modifier = Modifier.height(32.dp))
            LastNameBox()

            Spacer(modifier = Modifier.height(32.dp))
            EmailAddressBox()

            Spacer(modifier = Modifier.height(32.dp))
            PasswordBox()

            Spacer(modifier = Modifier.height(32.dp))
            ConfirmPasswordBox()

            Spacer(modifier = Modifier.height(16.dp))
            RegisterButton(onClick = { Log.d("Register button", "Register button clicked.") })

        }
    }
}

@Composable
fun RegisterButton(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Register")
    }
}

@Composable
fun FirstNameBox() {
    var text by remember { mutableStateOf("") }
    TextField (
        value = text,
        onValueChange = { newText ->
            text = newText
        },
        label = { Text(text = "First Name") },
        placeholder = { Text(text = "")},
    )
}

@Composable
fun LastNameBox() {
    var text by remember { mutableStateOf("") }
    TextField (
        value = text,
        onValueChange = { newText ->
            text = newText
        },
        label = { Text(text = "Last Name") },
        placeholder = { Text(text = "")},
    )
}

@Composable
fun EmailAddressBox() {
    var text by remember { mutableStateOf("") }
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
fun PasswordBox() {
    var text by remember { mutableStateOf("") }
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
fun ConfirmPasswordBox() {
    var text by remember { mutableStateOf("") }
    TextField (
        value = text,
        onValueChange = { newText ->
            text = newText
        },
        label = { Text(text = "Confirm Password") },
        placeholder = { Text(text = "")},
    )
}