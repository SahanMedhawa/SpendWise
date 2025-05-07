package com.example.spendwise.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.databinding.ItemTransactionBinding
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.example.spendwise.utils.PreferencesManager
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(
    private val preferencesManager: PreferencesManager,
    private val onDeleteClick: (Transaction) -> Unit,
    private val onEditClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                tvTitle.text = transaction.title
                tvCategory.text = transaction.category
                tvAmount.text = formatAmount(transaction.amount)
                tvDate.text = transaction.date.toString()
                btnDelete.setOnClickListener { onDeleteClick(transaction) }
                btnEdit.setOnClickListener { onEditClick(transaction) }
            }
        }

        private fun formatAmount(amount: Double): String {
            val currency = preferencesManager.getCurrency()
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return format.format(amount).replace(NumberFormat.getCurrencyInstance().currency.symbol, currency)
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 