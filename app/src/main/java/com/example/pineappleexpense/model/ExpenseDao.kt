package com.example.pineappleexpense.model

import androidx.room.*

//data access object (turns SQL queries into callable kotlin functions)
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense_table")
    suspend fun getAllExpenses(): List<Expense>

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
