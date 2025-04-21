package com.example.spendwise.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentSettingsBinding
import com.example.spendwise.utils.BackupManager
import com.example.spendwise.utils.NotificationManager
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment(
    private val transactionManager: TransactionManager,
    private val preferencesManager: PreferencesManager
) : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var backupManager: BackupManager
    private lateinit var notificationManager: NotificationManager
    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    private val createBackupFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportData(it) }
    }

    private val pickBackupFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backupManager = BackupManager(requireContext(), transactionManager)
        notificationManager = NotificationManager(requireContext())

        // Set up notification switches
        binding.switchNotifications.isChecked = preferences.getBoolean("notifications_enabled", true)
        binding.switchBudgetAlerts.isChecked = preferences.getBoolean("budget_alerts_enabled", true)
        binding.switchExpenseReminders.isChecked = preferences.getBoolean("expense_reminders_enabled", true)

        // Set up switch listeners
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("notifications_enabled", isChecked).apply()
            if (!isChecked) {
                binding.switchBudgetAlerts.isChecked = false
                binding.switchExpenseReminders.isChecked = false
            }
            notificationManager.updateNotificationSettings()
        }

        binding.switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("budget_alerts_enabled", isChecked).apply()
            notificationManager.updateNotificationSettings()
        }

        binding.switchExpenseReminders.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("expense_reminders_enabled", isChecked).apply()
            notificationManager.updateNotificationSettings()
        }

        // Set up backup buttons
        binding.btnExportData.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "spendwise_backup_$timestamp.json"
            createBackupFile.launch(fileName)
        }

        binding.btnImportData.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            pickBackupFile.launch(intent)
        }

        // Set up currency button
        binding.btnChangeCurrency.setOnClickListener {
            showCurrencySelectionDialog()
        }
    }

    private fun exportData(uri: Uri) {
        try {
            val json = backupManager.getBackupData()
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            Snackbar.make(binding.root, "Backup exported successfully", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Failed to export backup: ${e.message}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun importData(uri: Uri) {
        try {
            val json = requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: throw Exception("Failed to read backup file")

            backupManager.importData(json)
            Snackbar.make(binding.root, "Backup imported successfully", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Failed to import backup: ${e.message}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showCurrencySelectionDialog() {
        val currencies = resources.getStringArray(R.array.currencies)
        val currentCurrency = preferencesManager.currency
        val currentIndex = currencies.indexOfFirst { it.startsWith(currentCurrency) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.currency)
            .setSingleChoiceItems(currencies, currentIndex) { dialog, which ->
                val selectedCurrency = currencies[which].substringBefore(" -")
                preferencesManager.currency = selectedCurrency
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 