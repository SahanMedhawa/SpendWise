package com.example.spendwise.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.databinding.ItemCategorySummaryBinding
import com.example.spendwise.utils.PreferencesManager

class CategoryAnalysisAdapter(
    private val categories: List<CategorySpending>,
    private val preferencesManager: PreferencesManager
) : RecyclerView.Adapter<CategoryAnalysisAdapter.CategoryViewHolder>() {

    class CategorySpending(
        val category: String,
        val amount: Double,
        val percentage: Int
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategorySummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(
        private val binding: ItemCategorySummaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categorySpending: CategorySpending) {
            binding.tvCategory.text = categorySpending.category
            binding.tvAmount.text = "${String.format("%.2f", categorySpending.amount)} ${preferencesManager.currency} (${categorySpending.percentage}%)"
            binding.progressBar.progress = categorySpending.percentage
        }
    }
} 