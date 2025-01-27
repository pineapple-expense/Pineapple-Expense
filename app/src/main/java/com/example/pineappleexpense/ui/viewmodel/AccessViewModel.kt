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
import kotlinx.coroutines.launch


class AccessViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UserRole.User)
    val userState: StateFlow<UserRole> = _uiState.asStateFlow()

    //room database
    private val database = DatabaseInstance.getDatabase(application)
    private val expenseDao = database.expenseDao()

    //local expense list
    private val _expenseList = mutableStateOf<List<Expense>>(emptyList())
    val expenseList: State<List<Expense>> get() = _expenseList

    //load get all stored expenses from the room database on app start
    init {
        loadExpenses()
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