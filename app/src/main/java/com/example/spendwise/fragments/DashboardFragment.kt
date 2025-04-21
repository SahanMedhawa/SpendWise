package com.example.spendwise.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentDashboardBinding
import com.example.spendwise.models.TransactionType
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionManager: TransactionManager
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        transactionManager = TransactionManager(requireContext())
        preferencesManager = PreferencesManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val transactions = transactionManager.getTransactions()
        val currency = preferencesManager.currency

        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val totalBalance = totalIncome - totalExpenses
        val savings = totalBalance

        binding.tvTotalBalance.text = formatAmount(totalBalance, currency)
        binding.tvIncome.text = formatAmount(totalIncome, currency)
        binding.tvExpenses.text = formatAmount(totalExpenses, currency)
        binding.tvSavings.text = formatAmount(savings, currency)
    }

    private fun formatAmount(amount: Double, currency: String): String {
        return "${String.format("%.2f", amount)} $currency"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 