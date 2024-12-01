package com.example.pineappleexpense.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.pineappleexpense.model.Expense


class AccessViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(UserRole.User)
    val userState: StateFlow<UserRole> = _uiState.asStateFlow()
    //store the uri of the current image to be displayed
    //TODO: reinitialize when the app is closed and reopened so images persist through multiple sessions
    private val _expenseList = mutableStateOf<List<Expense>>(emptyList())
    val expenseList: State<List<Expense>> get() = _expenseList

    fun addExpense(expense: Expense) {
        _expenseList.value = _expenseList.value + expense
    }

    fun removeExpense(expense: Expense) {
        _expenseList.value = _expenseList.value - expense
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