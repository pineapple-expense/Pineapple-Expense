package com.example.pineappleexpense.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date
import java.util.UUID

//data class to store information about an expense
@Entity(tableName = "expense_table")
data class Expense(
    var title: String,
    var total: Float,
    var date: Date,
    var comment: String,
    var category: String,
    var imageUri: Uri? = null,
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
) : Serializable
