package com.example.pineappleexpense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pineappleexpense.ui.theme.PineappleExpenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PineappleExpenseTheme {
                HomeScreen()
            }
        }
    }
}

//currently hardcoded to always be the review page, will be changed
@Composable
fun HomeScreen() {
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar()
        },
    ) { innerPadding ->
        TopBar(Modifier.padding(innerPadding))
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            NoPendingExpensesCard()
        }
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFF3DDFF)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {}) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = "" // Add a valid content description
            )
        }
        Text(
            text = "Example Co LLC",
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = {}) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "" // Add a valid content description
            )
        }
    }
}

@Composable
fun BottomBar(modifier: Modifier = Modifier) {
    NavigationBar (
        modifier = modifier.height(80.dp),
        containerColor = Color(0xFFF3DDFF)
        ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "" // Add a valid content description
                )
            },
            label = {
                Text("Review")
            },
            selected = true,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFD6BBEA),
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "" // Add a valid content description
                )
            },
            label = {
                Text("Camera")
            },
            selected = false,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFD6BBEA),
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.DateRange,
                    contentDescription = "" // Add a valid content description
                )
            },
            label = {
                Text("Archive")
            },
            selected = false,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFD6BBEA),
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun NoPendingExpensesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "You currently have no pending expenses",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { /* Handle add new item action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF6200EA)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "add a new item",
                        color = Color(0xFF6200EA),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}