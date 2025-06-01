package com.example.pineappleexpense.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
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
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val imageUri = viewModel.latestImageUri
    val prediction = viewModel.currentPrediction
    var category: String
    var date: Date? = null
    var total: Float? = null
    var comment: String
    var title: String

    Scaffold (
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navController, viewModel)
        },
        topBar = {
            TopBar(navController,viewModel)
        }
    ) {
        Column (
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 125.dp),
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
                    val receipt = Expense(title, total?:0f, date?:SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("0000-00-00"), comment, category, imageUri, id = imageUri?.lastPathSegment.toString())
                    //add the receipt to the viewmodel
                    viewModel.addExpense(receipt)
                    //navigate back to home
                    navController.navigate("Home") {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBB86FC),
                    contentColor   = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .height(40.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "DONE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
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
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
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