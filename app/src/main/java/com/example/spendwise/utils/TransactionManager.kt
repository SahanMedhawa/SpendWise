package com.example.spendwise.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class TransactionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(0, transaction)
        saveTransactions(transactions)
    }

    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.remove(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it == updatedTransaction }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(transactions)
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