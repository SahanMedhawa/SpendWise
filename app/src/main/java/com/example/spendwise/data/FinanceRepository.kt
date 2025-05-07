package com.example.spendwise.data

import com.example.spendwise.models.Budget
import com.example.spendwise.models.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    // Transaction operations
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(category)

    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun getTotalAmountByType(type: String): Double {
        return transactionDao.getTotalAmountByType(type) ?: 0.0
    }

    suspend fun getTotalAmountByCategory(category: String, type: String): Double {
        return transactionDao.getTotalAmountByCategory(category, type) ?: 0.0
    }

    // Budget operations
    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudgetsByCategory(category: String): Flow<List<Budget>> =
        budgetDao.getBudgetsByCategory(category)

    fun getBudgetsByPeriod(period: String): Flow<List<Budget>> =
        budgetDao.getBudgetsByPeriod(period)

    fun getBudgetByCategory(category: String): Flow<Budget?> = 
        budgetDao.getBudgetsByCategory(category).map { budgets -> budgets.firstOrNull() }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteAllBudgets() {
        budgetDao.deleteAllBudgets()
    }
} 