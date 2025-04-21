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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapters.BackupAdapter
import com.example.spendwise.databinding.FragmentBackupBinding
import com.example.spendwise.utils.BackupManager
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import com.google.android.material.snackbar.Snackbar

class BackupFragment(
    private val transactionManager: TransactionManager,
    private val preferencesManager: PreferencesManager
) : Fragment() {
    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!
    private lateinit var backupManager: BackupManager
    private lateinit var backupAdapter: BackupAdapter

    private val createBackupFile = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { 
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
    }

    private val pickBackupFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backupManager = BackupManager(requireContext(), transactionManager)
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        backupAdapter = BackupAdapter { uri ->
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
        binding.recyclerViewBackups.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = backupAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabCreateBackup.setOnClickListener {
            createBackupFile.launch("spendwise_backup_${System.currentTimeMillis()}.json")
        }

        binding.fabRestoreBackup.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            pickBackupFile.launch(intent)
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 