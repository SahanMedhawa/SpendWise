package com.example.spendwise.utils

import android.content.Context
import android.os.Parcelable
import com.example.spendwise.models.Budget
import com.example.spendwise.models.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Parcelize
class BackupManager(
    private val context: @RawValue Context,
    private val transactionManager: @RawValue TransactionManager,
    private val budgetManager: @RawValue BudgetManager,
    private val preferencesManager: @RawValue PreferencesManager
) : Parcelable {

    suspend fun createBackup(): File {
        val backupFile = File(context.filesDir, "backup.zip")
        ZipOutputStream(backupFile.outputStream()).use { zip ->
            // Backup transactions
            val transactions = transactionManager.getAllTransactions().first()
            zip.putNextEntry(ZipEntry("transactions.json"))
            zip.write(transactions.toString().toByteArray())
            zip.closeEntry()

            // Backup budgets
            val budgets = budgetManager.getAllBudgets().first()
            zip.putNextEntry(ZipEntry("budgets.json"))
            zip.write(budgets.toString().toByteArray())
            zip.closeEntry()

            // Backup preferences
            zip.putNextEntry(ZipEntry("preferences.json"))
            zip.write("""
                {
                    "currency": "${preferencesManager.getCurrency()}",
                    "theme": "${preferencesManager.getTheme()}",
                    "passcode": "${preferencesManager.getPasscode()}",
                    "passcode_enabled": ${preferencesManager.isPasscodeEnabled()}
                }
            """.trimIndent().toByteArray())
            zip.closeEntry()
        }
        return backupFile
    }

    suspend fun restoreBackup(backupFile: File) {
        ZipInputStream(backupFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "transactions.json" -> {
                        val transactions = zip.readBytes().toString(Charsets.UTF_8)
                        // Parse and restore transactions
                    }
                    "budgets.json" -> {
                        val budgets = zip.readBytes().toString(Charsets.UTF_8)
                        // Parse and restore budgets
                    }
                    "preferences.json" -> {
                        val preferences = zip.readBytes().toString(Charsets.UTF_8)
                        // Parse and restore preferences
                    }
                }
                entry = zip.nextEntry
            }
        }
    }
} 