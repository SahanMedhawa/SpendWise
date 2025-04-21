package com.example.spendwise.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapters.BackupAdapter
import com.example.spendwise.databinding.FragmentBackupBinding
import com.example.spendwise.utils.BackupManager
import com.google.android.material.snackbar.Snackbar
import java.io.File

class BackupFragment : Fragment() {
    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!
    private lateinit var backupManager: BackupManager
    private lateinit var backupAdapter: BackupAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createBackup()
        } else {
            showMessage(getString(R.string.storage_permission_required))
        }
    }

    private val pickBackupFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                restoreBackup(uri)
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
        backupManager = BackupManager(requireContext())
        setupRecyclerView()
        setupClickListeners()
        loadBackups()
    }

    private fun setupRecyclerView() {
        backupAdapter = BackupAdapter { file ->
            restoreBackup(Uri.fromFile(file))
        }
        binding.recyclerViewBackups.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = backupAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabCreateBackup.setOnClickListener {
            checkStoragePermission()
        }

        binding.fabRestoreBackup.setOnClickListener {
            openFilePicker()
        }
    }

    private fun checkStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                createBackup()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            //new re
        }
    }

    private fun createBackup() {
        if (backupManager.exportData()) {
            loadBackups()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickBackupFile.launch(intent)
    }

    private fun restoreBackup(uri: Uri) {
        val file = File(uri.path ?: return)
        if (backupManager.importData(file)) {
            loadBackups()
        }
    }

    private fun loadBackups() {
        val backupFiles = backupManager.getBackupFiles()
        backupAdapter.submitList(backupFiles)
        binding.emptyState.visibility = if (backupFiles.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 