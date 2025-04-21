package com.example.spendwise.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.databinding.ItemBackupBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupAdapter(
    private val onRestoreClick: (Uri) -> Unit
) : ListAdapter<Uri, BackupAdapter.BackupViewHolder>(BackupDiffCallback()) {

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

    inner class BackupViewHolder(
        private val binding: ItemBackupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonRestore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRestoreClick(getItem(position))
                }
            }
        }

        fun bind(uri: Uri) {
            binding.textFileName.text = uri.lastPathSegment ?: "Unknown file"
            binding.textFileSize.text = "Size: Unknown" // We can't easily get file size from URI
            binding.textDate.text = "Last modified: Unknown" // We can't easily get last modified from URI
        }
    }

    private class BackupDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
} 