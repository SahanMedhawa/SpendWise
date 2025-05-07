package com.example.spendwise.utils

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import com.example.spendwise.data.AppDatabase
import com.example.spendwise.models.Budget
import com.example.spendwise.models.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Calendar

@Parcelize
class BudgetManager(
    private val context: @RawValue Context,
    private val transactionManager: @RawValue TransactionManager
) : Parcelable {
    private val database = AppDatabase.getDatabase(context)
    private val budgetDao = database.budgetDao()
    private val transactionDao = database.transactionDao()
    private val notificationManager = NotificationManager(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val budgets = mutableListOf<Budget>()

    init {
        checkAndResetBudgets()
    }

    private fun checkAndResetBudgets() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        coroutineScope.launch {
            val budgets = budgetDao.getAllBudgets().first()
            if (budgets.any { it.month != currentMonth }) {
                // It's a new month, clear all budgets
                budgetDao.deleteAllBudgets()
                Toast.makeText(context, "Budgets have been reset for the new month", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun addBudget(budget: Budget) {
        budgets.add(budget)
    }

    fun updateBudget(budget: Budget) {
        val index = budgets.indexOfFirst { it.id == budget.id }
        if (index != -1) {
            budgets[index] = budget
        }
    }

    fun deleteBudget(budget: Budget) {
        budgets.removeIf { it.id == budget.id }
    }

    fun getAllBudgets(): Flow<List<Budget>> = flow {
        emit(budgets.toList())
    }

    fun getBudgetByCategory(category: String): Flow<Budget?> = flow {
        emit(budgets.find { it.category == category })
    }

    fun getTotalBudget(): Flow<Double> = flow {
        emit(budgets.sumOf { it.amount })
    }

    fun saveBudget(budget: Budget) {
        coroutineScope.launch {
            budgetDao.insertBudget(budget)
        }
    }

    fun getBudgetsByCategory(category: String) = budgetDao.getBudgetsByCategory(category)

    fun getBudgetsByPeriod(period: String) = budgetDao.getBudgetsByPeriod(period)

    fun calculateBudgetProgress(budget: Budget): Double {
        var totalSpent = 0.0
        coroutineScope.launch {
            val transactions = transactionDao.getTransactionsByCategory(budget.category).first()
            totalSpent = transactions.sumOf { it.amount }
        }
        return (totalSpent / budget.amount) * 100
    }

    fun getRemainingAmount(budget: Budget): Double {
        var totalSpent = 0.0
        coroutineScope.launch {
            val transactions = transactionDao.getTransactionsByCategory(budget.category).first()
            totalSpent = transactions.sumOf { it.amount }
        }
        return budget.amount - totalSpent
    }

    fun checkBudgetAlerts() {
        coroutineScope.launch {
            val budgets = budgetDao.getAllBudgets().first()

            budgets.forEach { budget ->
                val progress = calculateBudgetProgress(budget)
                val remaining = getRemainingAmount(budget)

                when {
                    progress >= 100 -> {
                        notificationManager.showBudgetAlert(budget.category, budget.category, remaining)
                    }
                    progress >= 90 -> {
                        notificationManager.showBudgetAlert(budget.category, budget.category, remaining)
                    }
                    progress >= 75 -> {
                        notificationManager.showBudgetAlert(budget.category, budget.category, remaining)
                    }
                }
            }
        }
    }

    suspend fun getCurrentBudget(): Budget? {
        val currentMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
        return budgetDao.getBudgetsByPeriod(currentMonth).first().firstOrNull()
    }

    suspend fun getBudgetProgress(): Float {
        val currentBudget = getCurrentBudget() ?: return 0f
        val totalSpent = transactionDao.getTransactionsByCategory(currentBudget.category).first()
            .sumOf { it.amount }
        return (totalSpent.toFloat() / currentBudget.amount.toFloat()).coerceIn(0f, 1f)
    }
} 