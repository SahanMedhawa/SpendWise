package com.example.spendwise.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.spendwise.models.Budget
import com.example.spendwise.models.Transaction
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context) {
    private val gson = Gson()
    private val transactionManager = TransactionManager(context)
    private val budgetManager = BudgetManager(context)
    private val preferencesManager = PreferencesManager(context)

    fun exportData(): Boolean {
        try {
            // Create backup directory if it doesn't exist
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Create backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "spendwise_backup_$timestamp.json")

            // Prepare data for backup
            val backupData = BackupData(
                transactions = transactionManager.getTransactions(),
                budgets = budgetManager.getBudgets(),
                currency = preferencesManager.currency
            )

            // Write data to file
            FileWriter(backupFile).use { writer ->
                writer.write(gson.toJson(backupData))
            }

            Toast.makeText(context, "Backup created successfully", Toast.LENGTH_SHORT).show()
            return true
        } catch (e: IOException) {
            Toast.makeText(context, "Failed to create backup: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun importData(backupFile: File): Boolean {
        try {
            // Read backup file
            val json = backupFile.readText()
            val backupData = gson.fromJson(json, BackupData::class.java)

            // Restore data
            backupData.transactions.forEach { transactionManager.saveTransaction(it) }
            backupData.budgets.forEach { budgetManager.saveBudget(it) }
            preferencesManager.currency = backupData.currency

            Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
            return true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to restore backup: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun getBackupFiles(): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        return if (backupDir.exists()) {
            backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>,
        val currency: String
    )
} 