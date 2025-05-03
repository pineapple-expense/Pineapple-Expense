package com.example.pineappleexpense.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.example.pineappleexpense.data.Prediction
import com.example.pineappleexpense.model.Auth0Manager
import com.example.pineappleexpense.model.DatabaseInstance
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.model.Report
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableStateMapOf
import com.example.pineappleexpense.model.CategoryMapping


class AccessViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UserRole.User)
    val userState: StateFlow<UserRole> = _uiState.asStateFlow()

    var latestImageUri by mutableStateOf<Uri?>(null)
    var currentPrediction by mutableStateOf<Prediction?>(null)

    // Initialize Auth0 credentials manager
    private val manager = Auth0Manager(getApplication<Application>().applicationContext, application)

    //room database
    private val database = DatabaseInstance.getDatabase(application)
    private val expenseDao = database.expenseDao()
    private val reportDao = database.reportDao()
    private val categoryMappingDao = database.categoryMappingDao()

    //local expense list
    private val _expenseList = mutableStateOf<List<Expense>>(emptyList())
    val expenseList: State<List<Expense>> get() = _expenseList

    //local current report expense list
    private val _currentReportExpenses = mutableStateOf<List<Expense>>(emptyList())
    val currentReportExpenses: State<List<Expense>> get() = _currentReportExpenses

    // local report list (contains all reports)
    private val _reportList = mutableStateOf<List<Report>>(emptyList())
    val reportList: State<List<Report>> get() = _reportList

    // Computed property that returns pending reports (excluding "current")
    val pendingReports: List<Report>
        get() = _reportList.value.filter { it.name != "current" && it.status == "Under Review" }

    // Computed property that returns accepted reports (excluding "current")
    val acceptedReports: List<Report>
        get() = _reportList.value.filter { it.name != "current" && it.status == "Accepted" }

    // Computed property that returns rejected reports (excluding "current")
    val rejectedReports: List<Report>
        get() = _reportList.value.filter { it.name != "current" && it.status == "Rejected" }

    //computed property that returns expenses not in any pending, accepted report, or rejected reports (for displaying on the home page)
    val displayExpenses: List<Expense>
        get() {
            val pendingIds = pendingReports.flatMap { it.expenseIds }.toSet()
            val acceptedExpenses = acceptedReports.flatMap { it.expenseIds }.toSet()
            val rejectedExpenses = rejectedReports.flatMap { it.expenseIds }.toSet()
            return expenseList.value.filter { expense ->
                expense.id !in pendingIds && expense.id !in acceptedExpenses && expense.id !in rejectedExpenses
            }
        }

    //local category mappings
    private val _accountMappings = mutableStateMapOf<String, String>()
    val accountMappings: Map<String, String> get() = _accountMappings

    //load get all stored expenses from the room database on app start
    init {
        loadExpenses()
        loadReports()
        loadMappings()
    }

    private fun loadMappings() {
        viewModelScope.launch {
            categoryMappingDao.getAllMappings().collect { mappings ->
                _accountMappings.clear()
                mappings.forEach { mapping ->
                    _accountMappings[mapping.category] = mapping.accountCode
                }
            }
        }
    }

    fun addAccountMapping(category: String, accountCode: String) {
        viewModelScope.launch {
            categoryMappingDao.insertMapping(CategoryMapping(category, accountCode))
        }
    }

    fun removeAccountMapping(category: String) {
        viewModelScope.launch {
            categoryMappingDao.getMappingByCategory(category)?.let { mapping ->
                categoryMappingDao.deleteMapping(mapping)
            }
        }
    }

    fun updateAccountMapping(oldCategory: String, newCategory: String, accountCode: String) {
        viewModelScope.launch {
            categoryMappingDao.getMappingByCategory(oldCategory)?.let { oldMapping ->
                categoryMappingDao.deleteMapping(oldMapping)
            }
            categoryMappingDao.insertMapping(CategoryMapping(newCategory, accountCode))
        }
    }

    //add the following expense to the current report
    fun addToCurrentReport(expenseId: Int) {
        Log.d("pineapple", "adding id $expenseId to report")
        viewModelScope.launch() {
            viewModelScope.launch() {
                val report = reportDao.getReportByName("current")

                if (report != null) {
                    // Report exists; Append new expenseId
                    reportDao.updateExpensesForReport("current", report.expenseIds + expenseId)
                } else {
                    // Report doesn't exist; Create a new report
                    val newReport = Report(name = "current", expenseIds = listOf(expenseId), userName = manager.getName() ?: "")
                    reportDao.insertReport(newReport)
                }
                loadReports()
            }
        }
    }

    //remove the following expense from the current report
    fun removeFromCurrentReport(expenseId: Int) {
        viewModelScope.launch {
            reportDao.getReportByName("current")?.expenseIds?.let {
                reportDao.updateExpensesForReport("current", it.filter { it != expenseId })
            }
            loadReports()
        }
    }

    /**
     * Loads all reports, separating out the "current" report and storing the rest in _pendingReports.
     * - _currentReportExpenses is updated with the expenses for the "current" report (if it exists).
     * - _pendingReports is updated with every other report.
     */
    private fun loadReports() {
        viewModelScope.launch {
            val allReports = reportDao.getAllReports()
            _reportList.value = allReports
            // Find the "current" report (if any)
            val currentReport = allReports.firstOrNull { it.name == "current" }
            if (currentReport != null) {
                // Update the local current report list with its expenses
                _currentReportExpenses.value = expenseDao.getExpensesByIds(currentReport.expenseIds)
            } else {
                // If no current report, clear current report list
                _currentReportExpenses.value = emptyList()
            }
        }
    }

    /**
     * Submits the current report by moving all its expenses into a new report whose name
     * is based on the current date and time. The new report is then stored in the local
     * pendingReports variable.
     */
    fun submitReport() {
        viewModelScope.launch {
            // Fetch the current report
            val currentReport = reportDao.getReportByName("current")
            if (currentReport != null) {
                // Generate a new report name based on current date and time
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val newReportName = sdf.format(Date())

                // Create a new report with the new name and the same expense IDs as the current report
                val newReport = Report(name = newReportName, expenseIds = currentReport.expenseIds, status = "Under Review", userName = manager.getName().toString())

                // Insert the new report into the database
                reportDao.insertReport(newReport)

                // Clear the current report (or you could delete it if preferred)
                reportDao.updateExpensesForReport("current", emptyList())
                loadReports()

                Log.d("pineapple", "Submitted report '$newReportName' with expenses: ${newReport.expenseIds}")
            } else {
                Log.d("pineapple", "No current report found to submit")
            }
        }
    }

    // Changes the status of a report to be accepted
    fun acceptReport(report: Report) {
        viewModelScope.launch {
            reportDao.updateReportStatus(report.name, "Accepted")
            loadReports()
        }
    }

    // Changes the status of a report to be rejected
    fun rejectReport(report: Report) {
        viewModelScope.launch {
            reportDao.updateReportStatus(report.name, "Rejected")
            loadReports()
        }
    }

    //deletes a report (todo: make this unsend the report as well)
    fun unsendAndDeleteReport(reportName: String) {
        viewModelScope.launch {
            // Retrieve the report by name
            val report = reportDao.getReportByName(reportName)
            if (report != null) {
                // Delete the report only (expenses remain intact)
                reportDao.deleteReport(report)
                // Reload reports to update UI state
                loadReports()
                loadExpenses()
            } else {
                Log.d("pineapple", "Report '$reportName' not found for deletion")
            }
        }
    }

    // Change the comment of a report
    fun setReportComment(reportName: String, newComment: String) {
        viewModelScope.launch {
            // Make sure report exists
            if (reportDao.getReportByName(reportName) != null) {
                reportDao.updateComment(reportName, newComment)
                // Reload reports to update UI state
                loadReports()
                loadExpenses()
            } else {
                Log.d("pineapple", "Report '$reportName' not found")
            }
        }
    }

    //load expenses from the room database into the expense list
    private fun loadExpenses() {
        viewModelScope.launch {
            _expenseList.value = expenseDao.getAllExpenses()
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.insertExpense(expense)
            loadExpenses()
        }
    }

    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
            loadExpenses()
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
            loadExpenses()
            loadReports()
        }
    }

    fun toggleAccess(s:String = "User") {
        manager.changeRole(s)
//        if(s=="User") {
//            manager.changeRole(s)
//        } else {
//            _uiState.value = UserRole.Admin
//        }
    }

    fun getCurrentRole(): String? {
        return manager.getRole()
    }

    fun isAdmin(): String? {
        return manager.isAdmin()
    }
    /**
     * Returns the credentials manager.
     */
    fun getManager(): SecureCredentialsManager {
        return manager.getCredentialsManager()
    }

    fun getAccessToken(): String? {
        return manager.getAccess()
    }

    fun getIdToken(): String? {
        return manager.getIDToken()
    }

    fun getExpireTime(): String? {
        return manager.getExpireTime()
    }

    fun getUserEmail(): String? {
        return manager.getEmail()
    }

    fun getUserName(): String? {
        return manager.getName()
    }

    fun getUniqueID(): String? {
        return manager.getId()
    }

    fun getCompanyName(): String? {
        return manager.getCompany()
    }

    /**
     * Checks if the user is in a new session.
     */
    fun isNewSession(): Boolean {
        return manager.getSessionStatus()
    }

    /**
     * Check if credentials are still valid during application use.
     * */
    fun validateCredentials(): Boolean {
        return manager.validate()
    }
}

enum class UserRole {
    User, Admin
}