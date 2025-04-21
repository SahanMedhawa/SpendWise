package com.example.spendwise.utils

import android.content.Context
import android.widget.Toast
import com.example.spendwise.models.Budget
import com.example.spendwise.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BackupManager(private val context: Context) {
    private val gson = Gson()
    private val transactionManager = TransactionManager(context)
    private val budgetManager = BudgetManager(context)
    private val preferencesManager = PreferencesManager(context)

    fun getBackupData(): String {
        val backupData = BackupData(
            transactions = transactionManager.getTransactions(),
            budgets = budgetManager.getBudgets(),
            currency = preferencesManager.currency
        )
        return gson.toJson(backupData)
    }

    fun importData(json: String): Boolean {
        try {
            val backupData = gson.fromJson(json, BackupData::class.java)

            // Clear existing data
            transactionManager.clearTransactions()
            budgetManager.clearBudgets()

            // Restore data
            backupData.transactions.forEach { transactionManager.saveTransaction(it) }
            backupData.budgets.forEach { budgetManager.saveBudget(it) }
            preferencesManager.currency = backupData.currency

            return true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to restore backup: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>,
        val currency: String
    )
} 