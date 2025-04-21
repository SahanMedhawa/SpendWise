package com.example.spendwise.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.spendwise.models.Budget
import com.example.spendwise.models.Transaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.Date

class BackupManager(private val context: Context) {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateSerializer())
        .create()
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
            Log.d("BackupManager", "Starting import with JSON: $json")
            
            val backupData = gson.fromJson(json, BackupData::class.java)
            Log.d("BackupManager", "Parsed backup data: ${backupData.transactions.size} transactions, ${backupData.budgets.size} budgets")

            // Clear existing data
            transactionManager.clearTransactions()
            budgetManager.clearBudgets()
            Log.d("BackupManager", "Cleared existing data")

            // Restore transactions
            backupData.transactions.forEach { transaction ->
                try {
                    val restoredTransaction = Transaction(
                        id = transaction.id,
                        title = transaction.title,
                        amount = transaction.amount,
                        category = transaction.category,
                        type = transaction.type,
                        date = transaction.date
                    )
                    transactionManager.saveTransaction(restoredTransaction)
                    Log.d("BackupManager", "Restored transaction: ${restoredTransaction.title}")
                } catch (e: Exception) {
                    Log.e("BackupManager", "Error restoring transaction: ${e.message}")
                    throw e
                }
            }

            // Restore budgets
            backupData.budgets.forEach { budget ->
                try {
                    budgetManager.saveBudget(budget)
                    Log.d("BackupManager", "Restored budget: ${budget.category}")
                } catch (e: Exception) {
                    Log.e("BackupManager", "Error restoring budget: ${e.message}")
                    throw e
                }
            }

            // Restore currency
            preferencesManager.currency = backupData.currency
            Log.d("BackupManager", "Restored currency: ${backupData.currency}")

            return true
        } catch (e: Exception) {
            Log.e("BackupManager", "Failed to restore backup: ${e.message}", e)
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