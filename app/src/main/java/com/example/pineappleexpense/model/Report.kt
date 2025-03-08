package com.example.pineappleexpense.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_table")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val expenseIds: List<Int>, // Store the IDs of related Expense objects
    val status: String = "Unsubmitted"
)
