package com.example.pineappleexpense.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
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
import com.example.pineappleexpense.ui.components.*
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import java.util.Date

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditExpense(navController: NavHostController, viewModel: AccessViewModel, expenseID: String) {
    val scrollState = rememberScrollState()
    val expense = viewModel.expenseList.value.find { it.id == expenseID }
    val focusManager = LocalFocusManager.current

    //navigate home if there is no expense with the passed in ID
    if (expense == null) {
        LaunchedEffect(Unit) {
            navController.navigate("Home") {
                launchSingleTop = true
                restoreState = true
            }
        }
        return
    }

    val imageUri = expense.imageUri
    var category = expense.category
    var date: Date? = expense.date
    var total: Float? = expense.total
    var comment = expense.comment
    var title = expense.title

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
            title = titleBox(title)
            Spacer(modifier = Modifier.height(10.dp))
            category = categoryBox(category)
            Spacer(modifier = Modifier.height(10.dp))
            Row (
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(5.dp))
                date = dateBox(date)
                Spacer(modifier = Modifier.width(10.dp))
                total = totalBox(total)
            }
            Spacer(modifier = Modifier.height(10.dp))
            comment = commentBox(comment)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    Log.d("pineapple", "updating expense")
                    date?.let { expense.date = it }
                    expense.category = category
                    total?.let { expense.total = it }
                    expense.title = title
                    expense.comment = comment
                    //update expense in viewmodel
                    viewModel.updateExpense(expense)
                    //navigate back
                    navController.popBackStack()
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
                    text = "SAVE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
@Preview
@Composable
fun EditExpensePreview() {
    EditExpense(rememberNavController(), viewModel(), "0")
}