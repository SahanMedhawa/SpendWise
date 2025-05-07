package com.example.spendwise.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentSettingsBinding
import com.example.spendwise.utils.BackupManager
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var backupManager: BackupManager
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            preferencesManager = it.getParcelable(ARG_PREFERENCES_MANAGER)!!
            backupManager = it.getParcelable(ARG_BACKUP_MANAGER)!!
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
        viewModel = ViewModelProvider(this, SettingsViewModel.Factory(preferencesManager))[SettingsViewModel::class.java]
        setupViews()
        observePreferences()
    }

    private fun setupViews() {
        binding.currencyGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbUSD -> viewModel.setCurrency("USD")
                R.id.rbEUR -> viewModel.setCurrency("EUR")
                R.id.rbGBP -> viewModel.setCurrency("GBP")
            }
        }

        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbLight -> viewModel.setTheme("light")
                R.id.rbDark -> viewModel.setTheme("dark")
                R.id.rbSystem -> viewModel.setTheme("system")
            }
        }

        binding.switchPasscode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPasscodeEnabled(isChecked)
        }

        binding.btnExport.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    backupManager.createBackup()
                    Toast.makeText(context, "Backup created successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to create backup: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnImport.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val backupFile = File(requireContext().filesDir, "backup.json")
                    backupManager.restoreBackup(backupFile)
                    Toast.makeText(context, "Backup restored successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to restore backup: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observePreferences() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getCurrency().collect { currency ->
                when (currency) {
                    "USD" -> binding.rbUSD.isChecked = true
                    "EUR" -> binding.rbEUR.isChecked = true
                    "GBP" -> binding.rbGBP.isChecked = true
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTheme().collect { theme ->
                when (theme) {
                    "light" -> binding.rbLight.isChecked = true
                    "dark" -> binding.rbDark.isChecked = true
                    "system" -> binding.rbSystem.isChecked = true
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPasscodeEnabled().collect { isEnabled ->
                binding.switchPasscode.isChecked = isEnabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PREFERENCES_MANAGER = "preferences_manager"
        private const val ARG_BACKUP_MANAGER = "backup_manager"

        fun newInstance(
            preferencesManager: PreferencesManager,
            backupManager: BackupManager
        ) = SettingsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PREFERENCES_MANAGER, preferencesManager as Parcelable)
                putParcelable(ARG_BACKUP_MANAGER, backupManager as Parcelable)
            }
        }
    }
} 