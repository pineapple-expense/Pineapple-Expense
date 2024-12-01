package com.example.pineappleexpense.model

import android.net.Uri
import java.io.Serializable
import java.util.Date

data class Expense(
    val title: String,
    val total: Float,
    val date: Date,
    val comment: String,
    val category: String,
    val imageUri: Uri? = null
) : Serializable
