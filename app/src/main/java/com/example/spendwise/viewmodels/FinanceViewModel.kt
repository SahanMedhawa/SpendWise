package com.example.spendwise.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.data.AppDatabase
import com.example.spendwise.data.FinanceRepository
import com.example.spendwise.models.Budget
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    val allTransactions: Flow<List<Transaction>>
    val allBudgets: Flow<List<Budget>>

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    private val _currentBudget = MutableLiveData<Budget?>()
    val currentBudget: LiveData<Budget?> = _currentBudget

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.transactionDao(), database.budgetDao())
        allTransactions = repository.getAllTransactions()
        allBudgets = repository.getAllBudgets()
        loadTransactions()
        loadBudgets()
        loadCurrentBudget()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _transactions.value = transactions
            }
        }
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            repository.getAllBudgets().collect { budgets ->
                _budgets.value = budgets
            }
        }
    }

    private fun loadCurrentBudget() {
        viewModelScope.launch {
            repository.getBudgetByCategory("General").collect { budget ->
                _currentBudget.value = budget
            }
        }
    }

    fun getTransactionsByType(type: String): Flow<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        return repository.getTransactionsByCategory(category)
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    suspend fun getTotalAmountByType(type: String): Double {
        return repository.getTotalAmountByType(type)
    }

    suspend fun getTotalAmountByCategory(category: String, type: String): Double {
        return repository.getTotalAmountByCategory(category, type)
    }

    suspend fun getBudgetByCategory(category: String): Budget? {
        return repository.getBudgetByCategory(category).first()
    }

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun updateBudget(budget: Budget) = viewModelScope.launch {
        repository.updateBudget(budget)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    fun deleteAllBudgets() = viewModelScope.launch {
        repository.deleteAllBudgets()
    }

    suspend fun checkBudgetWarning(category: String): Boolean {
        val budget = getBudgetByCategory(category)
        if (budget != null) {
            val spent = getTotalAmountByCategory(category, TransactionType.EXPENSE.name)
            return spent >= budget.amount * 0.8 // Warning at 80% of budget
        }
        return false
    }
} 