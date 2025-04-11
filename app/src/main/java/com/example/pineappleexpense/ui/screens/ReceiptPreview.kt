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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.pineappleexpense.data.PredictedDate
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.ui.components.*
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReceiptPreview(navController: NavHostController, viewModel: AccessViewModel) {
    val imageUri = viewModel.latestImageUri
    val prediction = viewModel.currentPrediction
    var category: String
    var date: Date? = null
    var total: Float? = null
    var comment: String
    var title: String

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
                modifier = Modifier.fillMaxWidth().height(450.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            title = titleBox()
            Spacer(modifier = Modifier.height(10.dp))
            category = categoryBox(prediction?.category)
            Spacer(modifier = Modifier.height(10.dp))
            Row (
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(5.dp))
                date = dateBox(predictionDatetoDate(prediction?.date))
                Spacer(modifier = Modifier.width(10.dp))
                total = totalBox(prediction?.amount?.toFloatOrNull())
            }
            Spacer(modifier = Modifier.height(10.dp))
            comment = commentBox()
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    //put the receipt information into an Expense object
                    //currently just gives default values if there is a parsing error, this will be changed to prompt the user in the future
                    val receipt = Expense(title, total?:0f, date?:SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("0000-00-00"), comment, category, imageUri)
                    //add the receipt to the viewmodel
                    viewModel.addExpense(receipt)
                    //navigate back to home
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

fun predictionDatetoDate(date: PredictedDate?): Date? {
    date?.let {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, date.year.toInt())
            calendar.set(Calendar.MONTH, date.month.toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, date.day.toInt())
            calendar.time
        } catch (e: NumberFormatException) {
            null
        }
    }
    return null
}

@Preview
@Composable
fun PreviewReceiptPreview() {
    ReceiptPreview(rememberNavController(), viewModel())
}