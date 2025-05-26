package com.example.pineappleexpense.model

import androidx.room.*

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("SELECT * FROM report_table")
    suspend fun getAllReports(): List<Report>

    @Query("SELECT * FROM report_table WHERE id = :id")
    suspend fun getReportById(id: String): Report?

    @Query("SELECT * FROM report_table WHERE name = :reportName")
    suspend fun getReportByName(reportName: String): Report?

    @Query("UPDATE report_table SET expenseIds = :newExpenseIds WHERE id = :reportId")
    suspend fun updateExpensesForReport(reportId: String, newExpenseIds: List<String>)

    @Query("UPDATE report_table SET status = :newStatus WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, newStatus: String)

    @Query("UPDATE report_table SET comment = :newComment WHERE id = :reportId")
    suspend fun updateComment(reportId: String, newComment: String)

    @Delete
    suspend fun deleteReport(report: Report)
}
