package com.example.spendwise.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentBackupBinding
import com.example.spendwise.utils.BackupManager
import kotlinx.coroutines.launch
import java.io.File

class BackupFragment : Fragment() {
    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!
    private lateinit var backupManager: BackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            backupManager = it.getParcelable(ARG_BACKUP_MANAGER)!!
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
        setupViews()
    }

    private fun setupViews() {
        binding.btnExport.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val backupFile = File(requireContext().filesDir, "backup.json")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BACKUP_MANAGER = "backup_manager"

        fun newInstance(backupManager: BackupManager) = BackupFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_BACKUP_MANAGER, backupManager)
            }
        }
    }
} 