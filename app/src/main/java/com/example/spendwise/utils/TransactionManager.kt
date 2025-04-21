package com.example.spendwise.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class TransactionManager(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private var budgetManager: BudgetManager? = null

    fun setBudgetManager(budgetManager: BudgetManager) {
        this.budgetManager = budgetManager
    }

    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(0, transaction)
        saveTransactions(transactions)
        
        // Check budget alerts if this is an expense
        if (transaction.type == TransactionType.EXPENSE) {
            budgetManager?.checkBudgetAlerts(transaction.category)
        }
    }

    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.removeIf { it.id == transaction.id }
        saveTransactions(transactions)
        
        // Check budget alerts if this was an expense
        if (transaction.type == TransactionType.EXPENSE) {
            budgetManager?.checkBudgetAlerts(transaction.category)
        }
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(transactions)
            
            // Check budget alerts if this is an expense
            if (updatedTransaction.type == TransactionType.EXPENSE) {
                budgetManager?.checkBudgetAlerts(updatedTransaction.category)
            }
        }
    }

    fun clearTransactions() {
        saveTransactions(emptyList())
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "SpendWisePrefs"
        private const val KEY_TRANSACTIONS = "transactions"
    }
} 