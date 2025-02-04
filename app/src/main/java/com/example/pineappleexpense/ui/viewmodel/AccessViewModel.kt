package com.example.pineappleexpense.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pineappleexpense.model.DatabaseInstance
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.model.Report
import kotlinx.coroutines.launch


class AccessViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UserRole.User)
    val userState: StateFlow<UserRole> = _uiState.asStateFlow()

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

    //load get all stored expenses from the room database on app start
    init {
        loadExpenses()
        loadCurrentReport()
    }

    //add the following expense to the current report
    fun addToCurrentReport(expenseId: Int) {
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

    var latestImageUri by mutableStateOf<Uri?>(null)

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