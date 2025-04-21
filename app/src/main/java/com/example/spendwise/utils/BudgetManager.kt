package com.example.spendwise.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.example.spendwise.models.Budget
import com.example.spendwise.models.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class BudgetManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val transactionManager = TransactionManager(context)
    private val context = context

    init {
        checkAndResetBudgets()
    }

    private fun checkAndResetBudgets() {
        val lastResetMonth = prefs.getInt(KEY_LAST_RESET_MONTH, -1)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        
        if (lastResetMonth != currentMonth) {
            // It's a new month, clear all budgets
            saveBudgets(emptyList())
            prefs.edit().putInt(KEY_LAST_RESET_MONTH, currentMonth).apply()
            Toast.makeText(context, "Budgets have been reset for the new month", Toast.LENGTH_LONG).show()
        }
    }

    fun saveBudget(budget: Budget): Boolean {
        val budgets = getBudgets().toMutableList()
        
        // Check if a budget already exists for this category
        if (budgets.any { it.category == budget.category }) {
            Toast.makeText(
                context,
                "A budget already exists for ${budget.category}. Please edit the existing budget instead.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        
        budgets.add(budget)
        saveBudgets(budgets)
        return true
    }

    fun updateBudget(budget: Budget) {
        val budgets = getBudgets().toMutableList()
        val existingIndex = budgets.indexOfFirst { it.category == budget.category }
        if (existingIndex != -1) {
            budgets[existingIndex] = budget
            saveBudgets(budgets)
        }
    }

    fun getBudgets(): List<Budget> {
        checkAndResetBudgets() // Check if we need to reset budgets
        val json = prefs.getString(KEY_BUDGETS, "[]")
        val type = object : TypeToken<List<Budget>>() {}.type
        val budgets = gson.fromJson<List<Budget>>(json, type) ?: emptyList()
        
        // Update current spending for each budget
        return budgets.map { budget ->
            val currentSpending = calculateCurrentSpending(budget.category)
            budget.copy(currentSpending = currentSpending)
        }
    }

    fun deleteBudget(budget: Budget) {
        val budgets = getBudgets().toMutableList()
        budgets.remove(budget)
        saveBudgets(budgets)
    }

    fun getBudget(category: String): Budget? {
        return getBudgets().find { it.category == category }
    }

    private fun calculateCurrentSpending(category: String): Double {
        val transactions = transactionManager.getTransactions()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return transactions
            .filter { transaction ->
                transaction.type == TransactionType.EXPENSE &&
                transaction.category == category &&
                isInCurrentMonth(transaction.date, currentMonth, currentYear)
            }
            .sumOf { it.amount }
    }

    private fun isInCurrentMonth(date: java.util.Date, currentMonth: Int, currentYear: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.MONTH) == currentMonth && 
               calendar.get(Calendar.YEAR) == currentYear
    }

    fun clearBudgets() {
        saveBudgets(emptyList())
    }

    private fun saveBudgets(budgets: List<Budget>) {
        val json = gson.toJson(budgets)
        prefs.edit().putString(KEY_BUDGETS, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "SpendWisePrefs"
        private const val KEY_BUDGETS = "budgets"
        private const val KEY_LAST_RESET_MONTH = "last_reset_month"
    }
} 