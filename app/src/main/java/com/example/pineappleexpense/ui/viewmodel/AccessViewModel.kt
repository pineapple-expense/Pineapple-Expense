package com.example.pineappleexpense.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.example.pineappleexpense.data.Prediction
import com.example.pineappleexpense.data.addReceiptToReportRemote
import com.example.pineappleexpense.data.createReportRemote
import com.example.pineappleexpense.data.getReportExpenses
import com.example.pineappleexpense.data.retrieveSubmittedReports
import com.example.pineappleexpense.data.updateReceiptRemote
import com.example.pineappleexpense.model.Auth0Manager
import com.example.pineappleexpense.model.CategoryMapping
import com.example.pineappleexpense.model.DatabaseInstance
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.model.Report
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.exp
import com.example.pineappleexpense.data.submitReport as submitReportRemote


class AccessViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UserRole.User)
    val userState: StateFlow<UserRole> = _uiState.asStateFlow()

    var latestImageUri by mutableStateOf<Uri?>(null)
    var currentPrediction by mutableStateOf<Prediction?>(null)

    //refresh pending reports state (for admin home)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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
    fun addToCurrentReport(expenseId: String) {
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

    //add the following expense to the current report
    fun addToReport(reportName: String, expenseId: String) {
        Log.d("pineapple", "adding id $expenseId to report $reportName")
        viewModelScope.launch() {
            val report = reportDao.getReportByName(reportName)

            if (report != null) {
                // Report exists; Append new expenseId
                reportDao.updateExpensesForReport("current", report.expenseIds + expenseId)
            } else {
                throw Exception("report $reportName does not exist")
            }
            loadReports()
        }
    }

    //remove the following expense from the current report
    fun removeFromCurrentReport(expenseId: String) {
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
     * Submits the “current” report through a multi-step process:
     * 1. Loads the local “current” Report and validates it has expenses.
     * 2. Creates a new remote report container (via createReportRemote).
     * 3. For each expense in the report:
     *    a. Uploads the receipt data (updateReceiptRemote).
     *    b. Attaches the uploaded receipt to the remote report (addReceiptToReportRemote).
     * 4. Once *all* uploads and attachments succeed, finalizes the report on the server
     *    (submitReportRemote).
     * 5. Mirrors the completed report back into the local database and clears “current.”
     *
     * All success/failure outcomes are reported via the onFinished callback,
     * which is always invoked on the main thread with true = success, or false = any failure.
     */
    fun submitReport(onFinished: (Boolean) -> Unit) {

        /** Always hand result back to UI thread. */
        fun complete(result: Boolean) =
            viewModelScope.launch(Dispatchers.Main) { onFinished(result) }

        viewModelScope.launch {
            val TAG = "submitReport"
            Log.d(TAG, "▶️ submitReport() start")

            // ───────────────── 1) fetch “current” report ────────────────────────
            val current = reportDao.getReportByName("current")
            if (current == null) {
                Log.w(TAG, "⚠ No current report found")
                complete(false); return@launch
            }
            if (current.expenseIds.isEmpty()) {
                Log.w(TAG, "⚠ Current report has no expenses")
                complete(false); return@launch
            }

            // ───────────────── 2) create remote container ───────────────────────
            val remoteId = UUID.randomUUID().toString()
            Log.d(TAG, "→ createReportRemote($remoteId)")
            createReportRemote(
                viewModel = this@AccessViewModel,
                reportID  = remoteId,

                onSuccess = {
                    Log.d(TAG, "✔ createReportRemote success")

                    /* Each receipt now needs TWO async ops:
                     * (1) updateReceiptRemote
                     * (2) addReceiptToReportRemote
                     * So the total remaining tasks = expenseIds.size * 2  */
                    var remaining = current.expenseIds.size * 2
                    var hadError  = false

                    fun taskDone(error: Boolean) {
                        if (error) hadError = true
                        remaining--
                        if (remaining == 0) {
                            if (hadError) {
                                Log.e(TAG, "✗ One or more tasks failed – aborting final submit")
                                complete(false)
                            } else {
                                // ── 3) submit the fully‑populated remote report ──
                                Log.d(TAG, "→ submitReportRemote($remoteId)")
                                submitReportRemote(
                                    viewModel = this@AccessViewModel,
                                    onSuccess = {
                                        Log.d(TAG, "✔ submitReportRemote success")

                                        // ── 4) Mirror to local DB (IO) & finish ──
                                        val timestamp = SimpleDateFormat(
                                            "yyyyMMdd_HHmmss",
                                            Locale.getDefault()
                                        ).format(Date())

                                        val newReport = Report(
                                            name       = timestamp,
                                            expenseIds = current.expenseIds,
                                            status     = "Under Review",
                                            userName   = manager.getName().orEmpty()
                                        )

                                        viewModelScope.launch(Dispatchers.IO) {
                                            reportDao.insertReport(newReport)
                                            reportDao.updateExpensesForReport("current", emptyList())
                                            Log.d(TAG, "✔ Local DB updated – new report '$timestamp'")
                                            loadReports()
                                            complete(true)     // SUCCESS ✅
                                        }
                                    },
                                    onFailure = { err ->
                                        Log.e(TAG, "✗ submitReportRemote failed: $err")
                                        complete(false)
                                    }
                                )
                            }
                        }
                    }

                    // ───────────────── 2a) loop receipts ───────────────────────
                    current.expenseIds.forEach { id ->
                        val exp = expenseList.value.firstOrNull { it.id == id }
                        val receiptId = exp?.imageUri?.lastPathSegment

                        if (exp == null || receiptId == null) {
                            Log.e(TAG, "✗ Missing expense or URI for id=$id")
                            taskDone(true)  // update failed
                            taskDone(true)  // attach would also fail
                        } else {

                            // (1) updateReceiptRemote ───────────────────────────
                            Log.d(TAG, "→ updateReceiptRemote($receiptId)")
                            updateReceiptRemote(
                                viewModel = this@AccessViewModel,
                                receiptId = receiptId,
                                amount    = exp.total.toString(),
                                date      = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                    .format(exp.date),
                                category  = exp.category,
                                title     = exp.title,
                                comment   = exp.comment,

                                onSuccess = {
                                    Log.d(TAG, "  • receipt $receiptId updated")
                                    taskDone(false)   // update ok

                                    // (2) addReceiptToReportRemote ──────────────
                                    Log.d(TAG, "→ addReceiptToReportRemote($receiptId)")
                                    addReceiptToReportRemote(
                                        viewModel = this@AccessViewModel,
                                        receiptID = receiptId,
                                        onSuccess = {
                                            Log.d(TAG, "  • receipt $receiptId attached to report")
                                            taskDone(false)   // attach ok
                                        },
                                        onFailure = { err ->
                                            Log.e(TAG, "  ✗ attach receipt $receiptId failed: $err")
                                            taskDone(true)
                                        }
                                    )
                                },

                                onFailure = { err ->
                                    Log.e(TAG, "  ✗ updateReceiptRemote failed: $err")
                                    taskDone(true)  // update failed
                                    taskDone(true)  // attach deemed failed
                                }
                            )
                        }
                    }
                },

                onFailure = { err ->
                    Log.e(TAG, "✗ createReportRemote failed: $err")
                    complete(false)
                }
            )
        }
    }

    fun fetchPendingReports() = viewModelScope.launch {
        _isRefreshing.value = true
        try {
            retrieveSubmittedReports(
                this@AccessViewModel, onSuccess = { it.forEach { adminReport ->
                    //create a new report
                    val newReport = Report(
                        id = adminReport.reportNumber,
                        name = SimpleDateFormat(
                            "yyyyMMdd_HHmmss",
                            Locale.getDefault()
                        ).format(Date()),
                        expenseIds = emptyList(),
                        status = "pending",
                        userName = adminReport.name,
                        comment = adminReport.comment
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        reportDao.insertReport(newReport)
                        loadReports()
                    }
                    //download expenses
                    getReportExpenses(
                        this@AccessViewModel, newReport.id,
                        onSuccess = { it.forEach { expense ->

                            //todo: download receipt image and URI

                            val newExpense = Expense(
                                title = expense.title ?: "untitled expense",
                                total = expense.actAmount.toFloatOrNull() ?: throw IllegalArgumentException("Invalid total: ${expense.actAmount}"),
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.actDate) ?: throw IllegalArgumentException("Invalid date: ${expense.actDate}"),
                                comment = expense.comment ?: "",
                                category = expense.actCategory,
                                imageUri = null,
                                id = expense.receiptId
                            )
                            //add expense to local db
                            addExpense(newExpense)
                            //add expense to local report
                            addToReport(newReport.name, newExpense.id)

                        } },
                        onFailure = {throw Exception("network error: $it")}
                    )
                } },
                onFailure = {throw Exception("network error: $it")}
            )
        } catch (e: Exception) {
            val context = getApplication<Application>()
            Log.d("pineapple", "error retrieving reports: ${e.message}")
            Toast.makeText(context, "error retrieving reports", Toast.LENGTH_LONG).show()
        } finally {
            _isRefreshing.value = false
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