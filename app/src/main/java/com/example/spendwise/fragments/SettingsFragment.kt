package com.example.spendwise.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentSettingsBinding
import com.example.spendwise.utils.BackupManager
import com.example.spendwise.utils.PreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var backupManager: BackupManager

    private val createBackupFile = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportData(it) }
    }

    private val pickBackupFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importData(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferencesManager = PreferencesManager(requireContext())
        backupManager = BackupManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPreferences()
    }

    private fun setupPreferences() {
        // Setup notification preferences
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        binding.switchBudgetAlerts.isChecked = prefs.getBoolean("budget_alerts_enabled", true)
        binding.switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("budget_alerts_enabled", isChecked).apply()
        }

        binding.switchExpenseReminders.isChecked = prefs.getBoolean("expense_reminders_enabled", true)
        binding.switchExpenseReminders.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("expense_reminders_enabled", isChecked).apply()
        }

        // Setup backup preferences
        binding.btnExportData.setOnClickListener {
            createBackupFile.launch("spendwise_backup_${System.currentTimeMillis()}.json")
        }

        binding.btnImportData.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            pickBackupFile.launch(intent)
        }

        // Setup currency preference
        binding.btnChangeCurrency.setOnClickListener {
            showCurrencyDialog()
        }

        // Update currency button text
        updateCurrencyButtonText()
    }

    private fun exportData(uri: Uri) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                val backupData = backupManager.getBackupData()
                outputStream.write(backupData.toByteArray())
                showMessage(getString(R.string.backup_created_successfully))
            }
        } catch (e: Exception) {
            showMessage(getString(R.string.backup_failed, e.message))
        }
    }

    private fun importData(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val backupData = inputStream.bufferedReader().use { it.readText() }
                if (backupManager.importData(backupData)) {
                    showMessage(getString(R.string.backup_restored_successfully))
                    // Refresh all fragments
                    parentFragmentManager.fragments.forEach { fragment ->
                        when (fragment) {
                            is DashboardFragment -> fragment.onResume()
                            is TransactionsFragment -> fragment.onResume()
                            is BudgetFragment -> fragment.onResume()
                        }
                    }
                } else {
                    showMessage(getString(R.string.restore_failed))
                }
            }
        } catch (e: Exception) {
            showMessage(getString(R.string.restore_failed, e.message))
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showCurrencyDialog() {
        val currencies = resources.getStringArray(R.array.currencies)
        val currentCurrency = preferencesManager.currency
        val selectedIndex = currencies.indexOfFirst { it.startsWith(currentCurrency) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.currency)
            .setSingleChoiceItems(currencies, selectedIndex) { dialog, which ->
                val selectedCurrency = currencies[which].substringBefore(" ")
                preferencesManager.currency = selectedCurrency
                updateCurrencyButtonText()
                dialog.dismiss()
                
                // Show confirmation message
                Snackbar.make(
                    binding.root,
                    "Currency updated to ${currencies[which]}",
                    Snackbar.LENGTH_SHORT
                ).show()

                // Refresh all fragments
                parentFragmentManager.fragments.forEach { fragment ->
                    when (fragment) {
                        is DashboardFragment -> fragment.onResume()
                        is TransactionsFragment -> fragment.onResume()
                        is BudgetFragment -> fragment.onResume()
                    }
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateCurrencyButtonText() {
        val currencies = resources.getStringArray(R.array.currencies)
        val currentCurrency = preferencesManager.currency
        val currentCurrencyFull = currencies.find { it.startsWith(currentCurrency) }
        binding.btnChangeCurrency.text = getString(R.string.currency) + ": " + (currentCurrencyFull ?: currentCurrency)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 