package com.example.pineappleexpense.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReceiptPreview(navController: NavHostController, viewModel: AccessViewModel) {
    val imageUri = viewModel.latestImageUri

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController,viewModel)
        }
    ) {
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberImagePainter(imageUri),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxWidth().height(500.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            CategoryBox()
            Spacer(modifier = Modifier.height(10.dp))
            Row (
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(5.dp))
                DateBox()
                Spacer(modifier = Modifier.width(10.dp))
                TotalBox()
            }
            Spacer(modifier = Modifier.height(10.dp))
            CommentBox()
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                //TODO: save the text put into the various text fields and display it on the home page
                onClick = {
                    navController.navigate("Home") {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF6200EA)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = "done",
                    color = Color(0xFF6200EA),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CategoryBox() {
    var category by remember { mutableStateOf("") }
    TextField(
        value = category,
        onValueChange = {category = it},
        label = {Text("Category")},
        trailingIcon = {
            if (category.isNotEmpty()) {
                IconButton(onClick = { category = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                }
            }
        },
        modifier = Modifier.width(400.dp)
    )
}

@Composable
fun DateBox() {
    var date by remember { mutableStateOf("") }
    TextField(
        value = date,
        onValueChange = {date = it},
        label = {Text("Date")},
        trailingIcon = {
            if (date.isNotEmpty()) {
                IconButton(onClick = { date = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                }
            }
        },
        modifier = Modifier.width(190.dp)
    )
}

@Composable
fun TotalBox() {
    var total by remember { mutableStateOf("") }
    TextField(
        value = total,
        onValueChange = {total = it},
        label = {Text("Total")},
        trailingIcon = {
            if (total.isNotEmpty()) {
                IconButton(onClick = { total = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                }
            }
        },
        modifier = Modifier.width(200.dp)
    )
}

@Composable
fun CommentBox() {
    var comment by remember { mutableStateOf("") }
    TextField(
        value = comment,
        onValueChange = {comment = it},
        label = {Text("Comment")},
        trailingIcon = {
            if (comment.isNotEmpty()) {
                IconButton(onClick = { comment = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                }
            }
        },
        modifier = Modifier.width(400.dp).height(130.dp)
    )
}

@Preview
@Composable
fun PreviewReceiptPreview() {
    ReceiptPreview(rememberNavController(), AccessViewModel())
}