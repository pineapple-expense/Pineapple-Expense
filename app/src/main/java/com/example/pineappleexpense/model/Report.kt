package com.example.pineappleexpense.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "report_table")
data class Report(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val expenseIds: List<String>, // Store the IDs of related Expense objects
    val status: String = "Unsubmitted",
    val userName: String = "",
    val comment: String = ""
)
