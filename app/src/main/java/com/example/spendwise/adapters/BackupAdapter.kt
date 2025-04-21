package com.example.spendwise.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.databinding.ItemBackupBinding
import java.io.File

class BackupAdapter(
    private val onRestoreClick: (File) -> Unit
) : ListAdapter<File, BackupAdapter.BackupViewHolder>(BackupDiffCallback()) {

    inner class BackupViewHolder(private val binding: ItemBackupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            binding.textFileName.text = file.name
            binding.textFileSize.text = formatFileSize(file.length())
            binding.textDate.text = formatDate(file.lastModified())
            binding.buttonRestore.setOnClickListener {
                onRestoreClick(file)
            }
        }

        private fun formatFileSize(size: Long): String {
            val kb = size / 1024.0
            return String.format("%.2f KB", kb)
        }

        private fun formatDate(timestamp: Long): String {
            return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val binding = ItemBackupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BackupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class BackupDiffCallback : DiffUtil.ItemCallback<File>() {
    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.absolutePath == newItem.absolutePath
    }

    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.lastModified() == newItem.lastModified() &&
                oldItem.length() == newItem.length()
    }
} 