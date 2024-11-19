package com.example.pineappleexpense.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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