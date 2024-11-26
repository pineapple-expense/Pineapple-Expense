package com.example.pineappleexpense.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AccessViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(UserRole.User)
    val userState: StateFlow<UserRole> = _uiState.asStateFlow()

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