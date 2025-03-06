package com.example.pineappleexpense.model

import androidx.room.*
import com.example.pineappleexpense.model.Expense

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("SELECT * FROM report_table")
    suspend fun getAllReports(): List<Report>

    @Query("SELECT * FROM report_table WHERE id = :id")
    suspend fun getReportById(id: Int): Report?

    @Query("SELECT * FROM report_table WHERE name = :reportName")
    suspend fun getReportByName(reportName: String): Report?

    @Query("UPDATE report_table SET expenseIds = :newExpenseIds WHERE name = :reportName")
    suspend fun updateExpensesForReport(reportName: String, newExpenseIds: List<Int>)

    @Delete
    suspend fun deleteReport(report: Report)
}
