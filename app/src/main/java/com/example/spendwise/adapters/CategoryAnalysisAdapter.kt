package com.example.spendwise.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.databinding.ItemCategoryAnalysisBinding
import com.example.spendwise.utils.PreferencesManager
import java.text.NumberFormat
import java.util.Locale

class CategoryAnalysisAdapter(
    private val preferencesManager: PreferencesManager
) : ListAdapter<CategoryAnalysisAdapter.CategorySpending, CategoryAnalysisAdapter.ViewHolder>(CategorySpendingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryAnalysisBinding.inflate(
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
        private val binding: ItemCategoryAnalysisBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySpending) {
            binding.apply {
                tvCategory.text = item.category
                tvAmount.text = formatAmount(item.amount)
                tvPercentage.text = "${item.percentage}%"
                progressBar.progress = item.percentage
            }
        }

        private fun formatAmount(amount: Double): String {
            val currency = preferencesManager.getCurrency()
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return format.format(amount).replace(NumberFormat.getCurrencyInstance().currency.symbol, currency)
        }
    }

    data class CategorySpending(
        val category: String,
        val amount: Double,
        val percentage: Int
    )

    private class CategorySpendingDiffCallback : DiffUtil.ItemCallback<CategorySpending>() {
        override fun areItemsTheSame(oldItem: CategorySpending, newItem: CategorySpending): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategorySpending, newItem: CategorySpending): Boolean {
            return oldItem == newItem
        }
    }
} 