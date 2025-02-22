package com.example.pineappleexpense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun titleBox(initialText: String? = null): String {
    var title by remember { mutableStateOf(initialText ?: "") }
    TextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Title") },
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
fun categoryBox(initialText: String? = null): String {
    var category by remember { mutableStateOf(initialText ?: "") }
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
fun dateBox(initialDate: Date? = null): Date? {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    var date by remember { mutableStateOf(initialDate?.let { dateFormat.format(it) } ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

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
fun totalBox(initialValue: Float? = null): Float? {
    var total by remember { mutableStateOf(initialValue?.toString() ?: "") }
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
fun commentBox(initialText: String? = null): String {
    var comment by remember { mutableStateOf(initialText ?: "") }
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
