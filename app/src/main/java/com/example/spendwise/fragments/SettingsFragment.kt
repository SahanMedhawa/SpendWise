package com.example.spendwise.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentSettingsBinding
import com.example.spendwise.utils.PreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferencesManager = PreferencesManager(requireContext())
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
            // TODO: Implement data export
        }

        binding.btnImportData.setOnClickListener {
            // TODO: Implement data import
        }

        // Setup currency preference
        binding.btnChangeCurrency.setOnClickListener {
            showCurrencyDialog()
        }

        // Update currency button text
        updateCurrencyButtonText()
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