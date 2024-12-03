package com.example.pineappleexpense.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReceiptPreview(navController: NavHostController, viewModel: AccessViewModel) {
    val imageUri = viewModel.latestImageUri
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
            category = categoryBox()
            Spacer(modifier = Modifier.height(10.dp))
            Row (
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(5.dp))
                date = dateBox()
                Spacer(modifier = Modifier.width(10.dp))
                total = totalBox()
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

@Composable
fun titleBox(): String {
    var title by remember { mutableStateOf("") }
    TextField(
        value = title,
        onValueChange = {title = it},
        label = {Text("Title")},
        trailingIcon = {
            if (title.isNotEmpty()) {
                IconButton(onClick = { title = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                }
            }
        },
        modifier = Modifier.width(400.dp)
    )
    return title
}

@Composable
fun categoryBox(): String {
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
    return category
}

@Composable
fun dateBox(): Date? {
    var date by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Date format parser
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    Box(
        modifier = Modifier
            .width(190.dp)
            .height(56.dp)
            .background(Color(117, 118, 127), RectangleShape),
    ) {
        Box(
            modifier = Modifier
                .width(190.dp)
                .height(55.dp)
                .background(Color(225, 226, 236), RectangleShape)
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (date.isEmpty()) "Date" else date,
                color = if (date.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    // Overlay Date Picker
    if (showDatePicker) {
        Dialog(onDismissRequest = { showDatePicker = false }) {
            DatePickerContent(
                onDateSelected = { year, month, day ->
                    date = dateFormat.format(
                        Calendar.getInstance().apply {
                            set(year, month, day)
                        }.time
                    )
                    showDatePicker = false
                },
                try {
                    dateFormat.parse(date)
                } catch (_: Exception) {
                    null
                }
            )
        }
    }

    // Return the parsed date or null if invalid
    return try {
        dateFormat.parse(date)
    } catch (_: Exception) {
        null
    }
}

@Composable
fun DatePickerContent(
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    currentDate: Date?
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 8.dp,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val initialYear: Int
            val initialMonth: Int
            val initialDay: Int
            if(currentDate != null) {
                initialYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(currentDate).toInt()
                initialMonth = SimpleDateFormat("MM", Locale.getDefault()).format(currentDate).toInt() - 1
                initialDay = SimpleDateFormat("dd", Locale.getDefault()).format(currentDate).toInt()
            }
            else {
                val calendar = Calendar.getInstance()
                initialYear = calendar.get(Calendar.YEAR)
                initialMonth = calendar.get(Calendar.MONTH)
                initialDay = calendar.get(Calendar.DAY_OF_MONTH)
            }

            AndroidView(
                factory = { context ->
                    android.widget.DatePicker(context).apply {
                        init(initialYear, initialMonth, initialDay) { _, year, month, day ->
                            onDateSelected(year, month, day)
                        }
                    }
                },
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}


@Composable
fun totalBox(): Float? {
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
    return try {
        total.toFloat()
    } catch(_: Exception) {
        null
    }
}

@Composable
fun commentBox(): String {
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
    return comment
}

@Preview
@Composable
fun PreviewReceiptPreview() {
    ReceiptPreview(rememberNavController(), AccessViewModel())
}