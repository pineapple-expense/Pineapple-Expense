package com.example.pineappleexpense.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

//data class to store information about an expense
@Entity(tableName = "expense_table")
data class Expense(
    val title: String,
    val total: Float,
    val date: Date,
    val comment: String,
    val category: String,
    val imageUri: Uri? = null,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Serializable
