package com.example.pineappleexpense
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.model.Report
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.example.pineappleexpense.ui.viewmodel.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
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

class ViewModelTests {
    private lateinit var application: Application
    private lateinit var viewModel: AccessViewModel

    @Before
    fun setup() = runBlocking {
        // Get the app context from the instrumentation
        application = ApplicationProvider.getApplicationContext()
        viewModel = AccessViewModel(application)

        // Wait a moment for the initial loadExpenses() and loadReports() to complete.
        delay(1000)
    }

    @Test
    fun testToggleAccessChangesUserState() = runBlocking {
        // Initially, user role should be User.
        assertEquals(UserRole.User, viewModel.userState.value)

        // Toggle to Admin.
        viewModel.toggleAccess("Admin")
        delay(500)
        assertEquals(UserRole.Admin, viewModel.userState.value)

        // Toggle back to User.
        viewModel.toggleAccess("User")
        delay(500)
        assertEquals(UserRole.User, viewModel.userState.value)
    }

    @Test
    fun testAddAndRemoveExpense() = runBlocking {
        // Create a sample expense.
        val date = createDate(2022, 1, 1)
        val expense = Expense(
            title = "Test Expense",
            total = 10f,
            date = date,
            comment = "Test comment",
            category = "Food"
        )

        // Capture the initial count of expenses.
        val initialCount = viewModel.expenseList.value.size

        // Add the expense.
        viewModel.addExpense(expense)
        delay(1000)

        // Get the added expense from the expense list,
        // which now contains the auto-generated id.
        val addedExpense = viewModel.expenseList.value.find { it.title == expense.title }
        assertTrue("Expense should be added", addedExpense != null)

        // Now remove the expense using the one with the correct id.
        viewModel.removeExpense(addedExpense!!)
        delay(1000)

        // Check that the expense list size returns to the initial value.
        val listAfterRemove = viewModel.expenseList.value
        assertEquals("After removal, expense count should return to initial", initialCount, listAfterRemove.size)
        assertFalse("Expense should be removed", listAfterRemove.any { it.title == expense.title })
    }


    @Test
    fun testAddToAndRemoveFromCurrentReport() = runBlocking {
        // Test by adding and then removing an expense ID from the "current" report.
        val testExpenseId = 999

        viewModel.addToCurrentReport(testExpenseId)
        delay(1000)
        val currentReport: Report? = viewModel.reportList.value.firstOrNull { it.name == "current" }
        assertTrue("Current report should contain the test expense ID",
            currentReport?.expenseIds?.contains(testExpenseId) == true)

        viewModel.removeFromCurrentReport(testExpenseId)
        delay(1000)
        val updatedReport: Report? = viewModel.reportList.value.firstOrNull { it.name == "current" }
        if (updatedReport != null) {
            assertFalse("Current report should no longer contain the test expense ID",
                updatedReport.expenseIds.contains(testExpenseId))
        }
    }

    @Test
    fun testSubmitReportCreatesNewReport() {
        runBlocking {
            val testExpenseId = 1001
            // Add an expense ID to the "current" report.
            viewModel.addToCurrentReport(testExpenseId)
            delay(1000)

            // Submit the current report.
            viewModel.submitReport()
            delay(1500)

            // Find the submitted report (it should not have the name "current").
            val submittedReport = viewModel.reportList.value.firstOrNull { it.name != "current" }
            assertTrue("Submitted report must have status 'Under Review'",
                submittedReport?.status == "Under Review")
            assertTrue("Submitted report should contain the test expense ID",
                submittedReport?.expenseIds?.contains(testExpenseId) == true)

            // --- Cleanup: Remove the submitted report so it won't persist ---
            submittedReport?.let {
                val reportName = it.name
                viewModel.unsendAndDeleteReport(reportName)
                // Wait for the deletion to complete.
                delay(1000)
                // Retrieve the updated report list and check that the report is gone.
                val updatedReportList = viewModel.reportList.value
                val reportStillExists = updatedReportList.any { report -> report.name == reportName }
                assertFalse("Cleanup failed: submitted report '$reportName' still exists", reportStillExists)
            }
        }
    }
}