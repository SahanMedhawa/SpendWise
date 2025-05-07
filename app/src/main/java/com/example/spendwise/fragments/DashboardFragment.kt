package com.example.spendwise.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapters.CategoryAnalysisAdapter
import com.example.spendwise.adapters.TransactionAdapter
import com.example.spendwise.databinding.FragmentDashboardBinding
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import com.example.spendwise.viewmodels.FinanceViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FinanceViewModel
    private lateinit var categoryAdapter: CategoryAnalysisAdapter
    private lateinit var transactionManager: TransactionManager
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionManager = it.getParcelable(ARG_TRANSACTION_MANAGER)!!
            preferencesManager = it.getParcelable(ARG_PREFERENCES_MANAGER)!!
        }
    }

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
        viewModel = ViewModelProvider(this)[FinanceViewModel::class.java]
        setupViews()
        observeTransactions()
    }

    private fun setupViews() {
        setupPieChart()
        setupRecyclerView()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAnalysisAdapter(preferencesManager)
        binding.rvCategoryAnalysis.adapter = categoryAdapter
        binding.rvCategoryAnalysis.layoutManager = LinearLayoutManager(context)
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            transactionManager.getAllTransactions().collectLatest { transactions ->
                updateDashboard(transactions)
            }
        }
    }

    private fun updateDashboard(transactions: List<Transaction>) {
        updateTransactionSummary(transactions)
        updatePieChart(transactions)
        updateCategoryAnalysis(transactions)
    }

    private fun updateTransactionSummary(transactions: List<Transaction>) {
        var totalExpenses = 0.0
        var totalIncome = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.EXPENSE -> totalExpenses += transaction.amount
                TransactionType.INCOME -> totalIncome += transaction.amount
            }
        }

        val balance = totalIncome - totalExpenses
        binding.tvBalance.text = getString(R.string.currency_format, balance)
        binding.tvIncome.text = getString(R.string.currency_format, totalIncome)
        binding.tvExpenses.text = getString(R.string.currency_format, totalExpenses)
    }

    private fun updatePieChart(transactions: List<Transaction>) {
        val entries = transactions
            .groupBy { it.category }
            .map { (category, transactions) ->
                val total = transactions.sumOf { it.amount }
                PieEntry(total.toFloat(), category)
            }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            colors = listOf(
                Color.rgb(64, 89, 128),
                Color.rgb(149, 165, 124),
                Color.rgb(217, 184, 162),
                Color.rgb(191, 134, 134),
                Color.rgb(179, 48, 80)
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun updateCategoryAnalysis(transactions: List<Transaction>) {
        val totalExpenses = transactions.sumOf { it.amount }
        val categoryAnalysis = transactions.groupBy { it.category }
            .map { (category, transactions) ->
                val amount = transactions.sumOf { it.amount }
                val percentage = ((amount / totalExpenses) * 100).toInt()
                CategoryAnalysisAdapter.CategorySpending(category, amount, percentage)
            }
            .sortedByDescending { it.amount }

        categoryAdapter.submitList(categoryAnalysis)
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        // TODO: Implement edit dialog
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        // TODO: Implement delete confirmation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TRANSACTION_MANAGER = "transaction_manager"
        private const val ARG_PREFERENCES_MANAGER = "preferences_manager"

        fun newInstance(transactionManager: TransactionManager, preferencesManager: PreferencesManager) = DashboardFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_TRANSACTION_MANAGER, transactionManager as Parcelable)
                putParcelable(ARG_PREFERENCES_MANAGER, preferencesManager as Parcelable)
            }
        }
    }
} 