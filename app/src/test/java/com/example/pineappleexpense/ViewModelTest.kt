package com.example.pineappleexpense

import com.example.pineappleexpense.model.Expense
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.example.pineappleexpense.ui.viewmodel.UserRole
import org.junit.Test
import java.util.Date

class ViewModelTest {
    private var viewModel = AccessViewModel()

    //test that the adding and removing expenses functionality works
    @Test
    fun testAddRemoveExpense() {
        val testExpense = Expense("test", 0f, Date(0), "", "")
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
