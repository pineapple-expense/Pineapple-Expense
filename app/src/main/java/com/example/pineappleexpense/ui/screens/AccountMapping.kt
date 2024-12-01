package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

// Definitely not done yet.
@Composable
fun AccountMapping(navHost: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val userRole = viewModel.userState.collectAsState().value
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navHost, viewModel)
        },
        topBar = {
            TopBar(navHost, viewModel)
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .padding(bottom = 8.dp)
            .background(Color(0xFFF9EEFF))
            //.height(100.dp)
            .fillMaxWidth()
        ) {
            AccountCard(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun AccountCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFB8FF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 20.dp
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.fillMaxWidth()
//            .padding(top = 8.dp)
//            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column() {
                //TextBoxCategory()
                CategoryTextBoxDropdown()
                TextBoxAccount()
            }
        }
    }
}

@Composable
fun TextBoxAccount(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("")}
    Box(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color.Black, shape = CutCornerShape(8.dp))
        .background(Color(0xFFF095FF), shape = CutCornerShape(8.dp))
        .padding(horizontal = 8.dp)
        .padding(top = 8.dp)

    ) {
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            label = { Text(text = "Account") },
            placeholder = { Text(text = "") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )

        )

        //Button(onClick = {}, modifier = Modifier.align(Alignment.CenterEnd)) { }

    }
}

@Composable
fun TextBoxCategory(modifier: Modifier=Modifier) {
    var text by remember { mutableStateOf("")}
    Box(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color.Black, shape = CutCornerShape(8.dp))
        .background(Color(0xFFF095FF), shape = CutCornerShape(8.dp))
        .padding(horizontal = 8.dp)

    ) {
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            label = { Text(text = "Category") },
            placeholder = { Text(text = "") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )

        )

        //Button(onClick = {}, modifier = Modifier.align(Alignment.CenterEnd)) { }

    }
}

@Composable
fun CategoryTextBoxDropdown() {
    val categories = listOf("Meals", "Travel", "Supplies", "Safety", "Other")
    var itemPosition by remember {mutableStateOf(0)}
    var expanded by remember {mutableStateOf(false)}

        Box(modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, shape = CutCornerShape(8.dp))
            .background(Color(0xFFF095FF), shape = CutCornerShape(8.dp))
            .padding(horizontal = 8.dp)
            .padding(vertical = 8.dp)

        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expanded = true }
            ) {
                Text(text = categories[itemPosition])
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "DropDown Icon"
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    categories.forEachIndexed { index, category ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = category) },
                            onClick = {
                                expanded = false
                                itemPosition = index
                            }
                        )

                    }
                }
            }
        }
}


@Composable
fun EditButton(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
    }
}


@Preview
@Composable
fun PreviewComposable() {
    val navHost = rememberNavController()
    val viewModel = AccessViewModel()
    //AccountCard()
    AccountMapping(navHost, viewModel)
}