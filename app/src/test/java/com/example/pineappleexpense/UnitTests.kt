package com.example.pineappleexpense

import com.example.pineappleexpense.data.PredictedDate
import com.example.pineappleexpense.ui.screens.predictionDatetoDate
import org.junit.Test
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull


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

        val expected = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, 11) // December is 11 (0-indexed)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val resultCal = Calendar.getInstance().apply { time = result!! }
        resultCal.set(Calendar.HOUR_OF_DAY, 0)
        resultCal.set(Calendar.MINUTE, 0)
        resultCal.set(Calendar.SECOND, 0)
        resultCal.set(Calendar.MILLISECOND, 0)

        assertEquals(expected, resultCal.time)
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