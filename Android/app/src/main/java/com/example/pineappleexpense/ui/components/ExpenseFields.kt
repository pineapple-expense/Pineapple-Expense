package com.example.pineappleexpense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.TextFieldValue
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

    if (showDatePicker) {
        Dialog(
            onDismissRequest = { 
                // Check for valid date before dismissing
                try {
                    val parts = date.split("/")
                    val month = parts[0].toInt()
                    val day = parts[1].toInt()
                    val year = parts[2].toInt()
                    if (month !in 1..12 || day !in 1..31 || year !in 1900..2100) {
                        date = initialDate?.let { dateFormat.format(it) } ?: ""
                    }
                } catch (_: Exception) {
                    date = initialDate?.let { dateFormat.format(it) } ?: ""
                }
                showDatePicker = false 
            }
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showDatePicker = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                DatePickerContent(
                    onDateSelected = { year, month, day ->
                        date = dateFormat.format(
                            Calendar.getInstance().apply {
                                set(year, month, day)
                            }.time
                        )
                    },
                    try {
                        dateFormat.parse(date)
                    } catch (_: Exception) {
                        null
                    }
                )
            }
        }
    }

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

    var selectedMonth by remember { mutableStateOf((initialMonth + 1).toString().padStart(2, '0')) }
    var selectedDay by remember { mutableStateOf(initialDay.toString().padStart(2, '0')) }
    var selectedYear by remember { mutableStateOf(initialYear.toString()) }
    var monthTextFieldValue by remember { mutableStateOf(TextFieldValue(selectedMonth)) }
    var dayTextFieldValue by remember { mutableStateOf(TextFieldValue(selectedDay)) }
    var yearTextFieldValue by remember { mutableStateOf(TextFieldValue(selectedYear)) }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { context ->
                android.widget.DatePicker(context).apply {
                    calendarViewShown = true
                    spinnersShown = false
                    init(selectedYear.toInt(), selectedMonth.toInt() - 1, selectedDay.toInt()) { _, year, month, day ->
                        // Only update when user clicks a day in the calendar
                        val newMonth = (month + 1).toString().padStart(2, '0')
                        val newDay = day.toString().padStart(2, '0')
                        val newYear = year.toString()
                        
                        selectedMonth = newMonth
                        selectedDay = newDay
                        selectedYear = newYear
                        monthTextFieldValue = TextFieldValue(newMonth)
                        dayTextFieldValue = TextFieldValue(newDay)
                        yearTextFieldValue = TextFieldValue(newYear)
                        
                        onDateSelected(year, month, day)
                    }
                }
            },
            modifier = Modifier.wrapContentSize()
        )

        // Manual date entry fields
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),  // Reduced from 16.dp to 8.dp
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Month field
            TextField(
                value = monthTextFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 2 && newValue.text.all { it.isDigit() }) {
                        monthTextFieldValue = newValue
                        val numValue = newValue.text.toIntOrNull()
                        if (numValue in 1..12) {
                            selectedMonth = newValue.text.padStart(2, '0')
                            try {
                                onDateSelected(
                                    selectedYear.toInt(),
                                    selectedMonth.toInt() - 1,
                                    selectedDay.toInt()
                                )
                            } catch (_: Exception) {}
                        }
                    }
                },
                modifier = Modifier.width(60.dp),
                label = { Text("MM") },
                singleLine = true
            )

            Text("/", style = MaterialTheme.typography.titleLarge)

            // Day field
            TextField(
                value = dayTextFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 2 && newValue.text.all { it.isDigit() }) {
                        dayTextFieldValue = newValue
                        val numValue = newValue.text.toIntOrNull()
                        if (numValue in 1..31) {
                            selectedDay = newValue.text.padStart(2, '0')
                            try {
                                onDateSelected(
                                    selectedYear.toInt(),
                                    selectedMonth.toInt() - 1,
                                    selectedDay.toInt()
                                )
                            } catch (_: Exception) {}
                        }
                    }
                },
                modifier = Modifier.width(60.dp),
                label = { Text("DD") },
                singleLine = true
            )

            Text("/", style = MaterialTheme.typography.titleLarge)

            // Year field
            TextField(
                value = yearTextFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 4 && newValue.text.all { it.isDigit() }) {
                        yearTextFieldValue = newValue
                        val numValue = newValue.text.toIntOrNull()
                        if (numValue in 1900..2100) {
                            selectedYear = newValue.text
                            try {
                                onDateSelected(
                                    selectedYear.toInt(),
                                    selectedMonth.toInt() - 1,
                                    selectedDay.toInt()
                                )
                            } catch (_: Exception) {}
                        }
                    }
                },
                modifier = Modifier.width(80.dp),
                label = { Text("YYYY") },
                singleLine = true
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
        modifier = Modifier.width(400.dp).height(120.dp)
    )
    return comment
}
