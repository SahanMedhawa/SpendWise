package com.example.spendwise.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapters.CategoryAnalysisAdapter
import com.example.spendwise.databinding.FragmentDashboardBinding
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class DashboardFragment(
    private val transactionManager: TransactionManager,
    private val preferencesManager: PreferencesManager
) : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryAnalysis()
        setupPieChart()
        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun setupCategoryAnalysis() {
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(context)
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            transparentCircleRadius = 0f
            setDrawEntryLabels(false)
            legend.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            setEntryLabelTextSize(14f)
            setEntryLabelColor(Color.BLACK)
            setDrawCenterText(false)
        }
    }

    private fun updateDashboard() {
        val transactions = transactionManager.getTransactions()
        val currency = preferencesManager.currency

        val totalIncome = transactions
            .filter { transaction -> transaction.type == TransactionType.INCOME }
            .sumOf { transaction -> transaction.amount }

        val totalExpenses = transactions
            .filter { transaction -> transaction.type == TransactionType.EXPENSE }
            .sumOf { transaction -> transaction.amount }

        val totalBalance = totalIncome - totalExpenses
        val savings = totalBalance

        binding.tvTotalBalance.text = formatAmount(totalBalance, currency)
        binding.tvIncome.text = formatAmount(totalIncome, currency)
        binding.tvExpenses.text = formatAmount(totalExpenses, currency)
        binding.tvSavings.text = formatAmount(savings, currency)

        updateCategoryAnalysis(transactions)
        updatePieChart(totalExpenses, totalIncome - totalExpenses)
    }

    private fun updatePieChart(expenses: Double, remaining: Double) {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(expenses.toFloat(), " Spent"))
            add(PieEntry(remaining.toFloat(), " Remaining"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.expense_red),
                ContextCompat.getColor(requireContext(), R.color.income_green)
            )
            valueTextSize = 16f
            valueTextColor = Color.BLACK
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = entries.indexOfFirst { it.value == value }
                    return "${formatAmount(value.toDouble())}\n${entries[index].label}"
                }
            }
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun updateCategoryAnalysis(transactions: List<Transaction>) {
        val expensesByCategory = transactions
            .filter { transaction -> transaction.type == TransactionType.EXPENSE }
            .groupBy { transaction -> transaction.category }
            .mapValues { (_, categoryTransactions) -> categoryTransactions.sumOf { transaction -> transaction.amount } }

        val totalExpenses = expensesByCategory.values.sum()
        
        val categorySpendings = expensesByCategory.map { (category, amount) ->
            val percentage = if (totalExpenses > 0) {
                ((amount / totalExpenses) * 100).toInt()
            } else {
                0
            }
            CategoryAnalysisAdapter.CategorySpending(category, amount, percentage)
        }.sortedByDescending { categorySpending -> categorySpending.amount }

        binding.recyclerViewCategories.adapter = CategoryAnalysisAdapter(
            categorySpendings,
            preferencesManager
        )
    }

    private fun formatAmount(amount: Double, currency: String): String {
        return "${String.format("%.2f", amount)} $currency"
    }

    private fun formatAmount(amount: Double): String {
        return String.format("%.2f", amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 