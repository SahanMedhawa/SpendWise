package com.example.spendwise.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentBudgetBinding
import com.example.spendwise.models.Budget
import com.example.spendwise.utils.BudgetManager
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.progressindicator.LinearProgressIndicator
import androidx.appcompat.app.AlertDialog

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var budgetManager: BudgetManager
    private lateinit var transactionManager: TransactionManager
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetManager = BudgetManager(requireContext())
        transactionManager = TransactionManager(requireContext())
        preferencesManager = PreferencesManager(requireContext())
        setupUI()
        loadBudgets()
    }

    override fun onResume() {
        super.onResume()
        loadBudgets()
    }

    private fun setupUI() {
        binding.fabAddBudget.setOnClickListener {
            showAddBudgetDialog()
        }
    }

    private fun loadBudgets() {
        val budgets = budgetManager.getBudgets()
        binding.linearLayout.removeAllViews()

        budgets.forEach { budget ->
            val cardView = createBudgetCard(budget)
            binding.linearLayout.addView(cardView)
        }
    }

    private fun createBudgetCard(budget: Budget): MaterialCardView {
        val cardView = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            cardElevation = 2f
            radius = 8f
        }

        val inflater = LayoutInflater.from(requireContext())
        val cardContent = inflater.inflate(R.layout.item_budget, cardView, false)

        cardContent.findViewById<TextView>(R.id.tvCategory).text = budget.category
        cardContent.findViewById<TextView>(R.id.tvAmount).text = 
            "${String.format("%.2f", budget.limit)} ${preferencesManager.currency}"
        cardContent.findViewById<TextView>(R.id.tvPeriod).text = "Monthly" // Always show Monthly

        // Setup progress bar
        val progressBar = cardContent.findViewById<LinearProgressIndicator>(R.id.progressBar)
        val progressText = cardContent.findViewById<TextView>(R.id.tvProgress)
        val percentage = budget.percentage
        progressBar.progress = percentage
        progressText.text = "${budget.currentSpending} ${preferencesManager.currency} / ${budget.limit} ${preferencesManager.currency} ($percentage%)"

        // Setup edit button
        cardContent.findViewById<View>(R.id.btnEdit).setOnClickListener {
            showEditBudgetDialog(budget)
        }

        // Setup delete button
        cardContent.findViewById<View>(R.id.btnDelete).setOnClickListener {
            showDeleteConfirmation(budget)
        }

        cardView.addView(cardContent)
        return cardView
    }

    private fun showAddBudgetDialog() {
        showBudgetDialog(null)
    }

    private fun showEditBudgetDialog(budget: Budget) {
        showBudgetDialog(budget)
    }

    private fun showBudgetDialog(existingBudget: Budget?) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existingBudget == null) R.string.add_budget else R.string.edit_budget)
            .setView(R.layout.dialog_add_budget)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val category = (dialog as AlertDialog).findViewById<AutoCompleteTextView>(R.id.etCategory)?.text.toString()
                val amount = dialog.findViewById<TextInputEditText>(R.id.etAmount)?.text.toString().toDoubleOrNull() ?: 0.0

                if (category.isNotEmpty() && amount > 0) {
                    val budget = if (existingBudget == null) {
                        Budget(category = category, limit = amount)
                    } else {
                        existingBudget.copy(category = category, limit = amount)
                    }
                    
                    if (existingBudget == null) {
                        if (budgetManager.saveBudget(budget)) {
                            loadBudgets()
                        }
                    } else {
                        budgetManager.updateBudget(budget)
                        loadBudgets()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Pre-fill the form if editing
        if (existingBudget != null) {
            dialog.findViewById<AutoCompleteTextView>(R.id.etCategory)?.setText(existingBudget.category)
            dialog.findViewById<TextInputEditText>(R.id.etAmount)?.setText(existingBudget.limit.toString())
        }

        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.budget_categories)
        )
        dialog.findViewById<AutoCompleteTextView>(R.id.etCategory)?.setAdapter(categoryAdapter)
    }

    private fun showDeleteConfirmation(budget: Budget) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_budget)
            .setMessage(R.string.delete_budget_confirmation)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                budgetManager.deleteBudget(budget)
                loadBudgets()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 