package com.example.spendwise.utils

import android.content.Context
import android.os.Parcelable
import com.example.spendwise.data.AppDatabase
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flow
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Parcelize
class TransactionManager(
    private val context: @RawValue Context,
    private val preferencesManager: @RawValue PreferencesManager
) : Parcelable {
    private val database = AppDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var budgetManager: BudgetManager? = null
    private val transactions = mutableListOf<Transaction>()

    fun setBudgetManager(budgetManager: BudgetManager) {
        this.budgetManager = budgetManager
    }

    fun saveTransaction(transaction: Transaction) {
        scope.launch {
            transactionDao.insertTransaction(transaction)
            
            // Check budget alerts if this is an expense
            if (transaction.type == TransactionType.EXPENSE) {
                budgetManager?.checkBudgetAlerts()
            }
        }
    }

    fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    fun deleteTransaction(transaction: Transaction) {
        scope.launch {
            transactionDao.deleteTransaction(transaction)
            transactions.removeIf { it.id == transaction.id }
            
            // Check budget alerts if this was an expense
            if (transaction.type == TransactionType.EXPENSE) {
                budgetManager?.checkBudgetAlerts()
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        scope.launch {
            transactionDao.updateTransaction(transaction)
            val index = transactions.indexOfFirst { it.id == transaction.id }
            if (index != -1) {
                transactions[index] = transaction
            }
            
            // Check budget alerts if this is an expense
            if (transaction.type == TransactionType.EXPENSE) {
                budgetManager?.checkBudgetAlerts()
            }
        }
    }

    fun clearTransactions() {
        scope.launch {
            transactionDao.deleteAllTransactions()
            transactions.clear()
        }
    }

    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    fun getAllTransactions(): Flow<List<Transaction>> = flow {
        emit(transactions.toList())
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> = flow {
        emit(transactions.filter { it.type == type })
    }

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flow {
        emit(transactions.filter { it.category == category })
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = flow {
        emit(transactions.filter { it.date.time in startDate..endDate })
    }
} 