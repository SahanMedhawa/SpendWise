package com.example.spendwise.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.databinding.ItemTransactionBinding
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.example.spendwise.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val preferencesManager = PreferencesManager(binding.root.context)

        init {
            binding.root.setOnClickListener {
                onItemClick(transactions[adapterPosition])
            }
            
            binding.btnEdit.setOnClickListener {
                onEditClick(transactions[adapterPosition])
            }
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(transactions[adapterPosition])
            }
        }

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = dateFormat.format(transaction.date)
            
            val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
            val currency = preferencesManager.currency
            binding.tvAmount.text = "$amountPrefix${String.format("%.2f", transaction.amount)} $currency"
            
            binding.tvAmount.setTextColor(
                binding.root.context.getColor(
                    if (transaction.type == TransactionType.INCOME) 
                        com.example.spendwise.R.color.income_green
                    else 
                        com.example.spendwise.R.color.expense_red
                )
            )
        }
    }
} 