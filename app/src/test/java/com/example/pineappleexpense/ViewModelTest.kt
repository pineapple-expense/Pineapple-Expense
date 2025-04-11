package com.example.pineappleexpense
/*
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.example.pineappleexpense.ui.viewmodel.UserRole
import org.junit.Test
import java.util.Date

 class ViewModelTest {
    val application = ApplicationProvider.getApplicationContext<Application>()
    private var viewModel = AccessViewModel(application)

    //test that the adding and removing expenses functionality works
    @Test
    fun testAddRemoveExpense() {
        val testExpense = Expense(
            title = "test",
            total = 0f,
            date = Date(0),
            comment = "",
            category = ""
        )
        viewModel.addExpense(testExpense)

        assert(testExpense == viewModel.expenseList.value[0])

        viewModel.removeExpense(testExpense)

        assert(viewModel.expenseList.value.isEmpty())
    }

    //test switching user state functionality
    @Test
    fun testUserState() {
        assert(viewModel.userState.value == UserRole.User)
        viewModel.toggleAccess("Admin")
        assert(viewModel.userState.value == UserRole.Admin)
        viewModel.toggleAccess("User")
        assert(viewModel.userState.value == UserRole.User)
    }
}
*/