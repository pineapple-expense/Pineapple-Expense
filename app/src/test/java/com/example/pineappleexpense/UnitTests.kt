package com.example.pineappleexpense

import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.data.PredictedDate
import com.example.pineappleexpense.ui.components.expensesDateRange
import com.example.pineappleexpense.ui.screens.predictionDatetoDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

//helper function to create Date instances with the desired precision.
private fun createDate(year: Int, month: Int, day: Int): Date {
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1) // Note: Calendar months are zero-based.
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}

//tests PredictionDatetoDate function in ReceiptPreview.kt
class PredictionDatetoDateTests {

    @Test
    fun testValidPredictedDate() {
        val predictedDate = PredictedDate(
            fullDate = "2025-12-31",
            year = "2025",
            month = "12",
            day = "31"
        )

        val result = predictionDatetoDate(predictedDate)
        val expected = createDate(2025, 12, 31)

        // Compare the expected date with the result.
        assertEquals(expected, result)
    }

    @Test
    fun testNullInput() {
        val result = predictionDatetoDate(null)
        assertNull(result)
    }

    @Test
    fun testInvalidNumberFormat() {
        val predictedDate = PredictedDate(
            fullDate = "invalid",
            year = "20xx",
            month = "March",
            day = "first"
        )

        val result = predictionDatetoDate(predictedDate)
        assertNull(result)
    }
}

//test expensesDateRange function in ReportCardComponents
class ExpensesDateRangeTest {

    @Test
    fun testExpensesDateRange_emptyList() {
        // When no expenses are provided, expect a default message.
        val expenses: List<Expense> = emptyList()
        val result = expensesDateRange(expenses)
        assertEquals("No expenses", result)
    }

    @Test
    fun testExpensesDateRange_singleExpense() {
        // With one expense, the range should be that same date repeated.
        val date = createDate(2021, 1, 1)
        val expense = Expense(
            title = "Test Expense",
            total = 12.5f,
            date = date,
            comment = "No comment",
            category = "Test"
        )
        val expenses = listOf(expense)

        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val formattedDate = dateFormatter.format(date)
        val expected = "$formattedDate - $formattedDate"

        val result = expensesDateRange(expenses)
        assertEquals(expected, result)
    }

    @Test
    fun testExpensesDateRange_multipleExpenses_unsorted() {
        // Create expenses out of order. The function should sort them and extract the earliest
        // and the latest dates to create the range string.
        val date1 = createDate(2021, 1, 1)   // earliest
        val date2 = createDate(2021, 1, 15)  // latest
        val date3 = createDate(2021, 1, 5)   // middle

        val expense1 = Expense(
            title = "Expense1",
            total = 10f,
            date = date1,
            comment = "Comment",
            category = "Food"
        )
        val expense2 = Expense(
            title = "Expense2",
            total = 20f,
            date = date2,
            comment = "Comment",
            category = "Travel"
        )
        val expense3 = Expense(
            title = "Expense3",
            total = 15f,
            date = date3,
            comment = "Comment",
            category = "Misc"
        )

        // Provide the expenses in unsorted order.
        val unsortedExpenses = listOf(expense2, expense1, expense3)
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val expected = "${dateFormatter.format(date1)} - ${dateFormatter.format(date2)}"

        val result = expensesDateRange(unsortedExpenses)
        assertEquals(expected, result)
    }
}

