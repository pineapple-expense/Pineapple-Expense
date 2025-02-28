package com.example.pineappleexpense.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pineappleexpense.data.Prediction
import com.example.pineappleexpense.model.DatabaseInstance
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.model.Report
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AccessViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UserRole.User)
    val userState: StateFlow<UserRole> = _uiState.asStateFlow()

    var latestImageUri by mutableStateOf<Uri?>(null)
    var currentPrediction by mutableStateOf<Prediction?>(null)

    //room database
    private val database = DatabaseInstance.getDatabase(application)
    private val expenseDao = database.expenseDao()
    private val reportDao = database.reportDao()

    //local expense list
    private val _expenseList = mutableStateOf<List<Expense>>(emptyList())
    val expenseList: State<List<Expense>> get() = _expenseList

    //local current report list
    private val _currentReportList = mutableStateOf<List<Expense>>(emptyList())
    val currentReportList: State<List<Expense>> get() = _currentReportList

    //local pending reports list (for submitted reports)
    private val _pendingReports = mutableStateOf<List<Report>>(emptyList())
    val pendingReports: State<List<Report>> get() = _pendingReports

    //computed property that returns expenses not in any pending report (for displaying on the home page)
    val displayExpenses: List<Expense>
        get() {
            val pendingIds = pendingReports.value.flatMap { it.expenseIds }.toSet()
            return expenseList.value.filter { expense -> expense.id !in pendingIds }
        }

    //load get all stored expenses from the room database on app start
    init {
        loadExpenses()
        loadCurrentReport()
    }

    //add the following expense to the current report
    fun addToCurrentReport(expenseId: Int) {
        Log.d("pineapple", "adding id $expenseId to report")
        viewModelScope.launch() {
            viewModelScope.launch() {
                val report = reportDao.getReportByName("current")

                if (report != null) {
                    // Report exists; Append new expenseId
                    reportDao.updateExpensesForReport("current", report.expenseIds + expenseId)
                } else {
                    // Report doesn't exist; Create a new report
                    val newReport = Report(name = "current", expenseIds = listOf(expenseId))
                    reportDao.insertReport(newReport)
                }
                loadCurrentReport()
            }
        }
    }

    //remove the following expense from the current report
    fun removeFromCurrentReport(expenseId: Int) {
        viewModelScope.launch() {
            reportDao.getReportByName("current")?.expenseIds?.let {
                reportDao.updateExpensesForReport("current", it.filter { it != expenseId })
            }
            loadCurrentReport()
        }
    }

    //update the local currentReportList value with the expenses in the current report
    private fun loadCurrentReport() {
        viewModelScope.launch() {
            reportDao.getReportByName("current")?.expenseIds?.let {
                _currentReportList.value = expenseDao.getExpensesByIds(it)
            }
        }
    }

    /**
     * Submits the current report by moving all its expenses into a new report whose name
     * is based on the current date and time. The new report is then stored in the local
     * pendingReports variable.
     */
    fun submitReport() {
        viewModelScope.launch {
            // Fetch the current report
            val currentReport = reportDao.getReportByName("current")
            if (currentReport != null) {
                // Generate a new report name based on current date and time
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val newReportName = sdf.format(Date())

                // Create a new report with the new name and the same expense IDs as the current report
                val newReport = Report(name = newReportName, expenseIds = currentReport.expenseIds)

                // Insert the new report into the database
                reportDao.insertReport(newReport)

                // Clear the current report (or you could delete it if preferred)
                reportDao.updateExpensesForReport("current", emptyList())
                loadCurrentReport()

                // Update the local pendingReports variable
                _pendingReports.value = _pendingReports.value + newReport

                Log.d("pineapple", "Submitted report '$newReportName' with expenses: ${newReport.expenseIds}")
            } else {
                Log.d("pineapple", "No current report found to submit")
            }
        }
    }

    //load expenses from the room database into the expense list
    private fun loadExpenses() {
        viewModelScope.launch {
            _expenseList.value = expenseDao.getAllExpenses()
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.insertExpense(expense)
            loadExpenses()
        }
    }

    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
            loadExpenses()
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
            loadExpenses()
            loadCurrentReport()
        }
    }

    fun toggleAccess(s:String = "User") {
        if(s=="User") {
            _uiState.value = UserRole.User
        } else {
            _uiState.value = UserRole.Admin
        }
    }
}

enum class UserRole {
    User, Admin
}