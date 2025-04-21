package com.example.spendwise.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.spendwise.R
import com.example.spendwise.databinding.FragmentBudgetBinding
import com.example.spendwise.databinding.DialogAddBudgetBinding
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
import java.util.*

class BudgetFragment(
    private val transactionManager: TransactionManager,
    private val preferencesManager: PreferencesManager
) : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var budgetManager: BudgetManager

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
        budgetManager = BudgetManager(requireContext(), transactionManager)
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
        val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Budget")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val category = dialogBinding.etCategory.text.toString()
                val limit = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
                
                val calendar = Calendar.getInstance()
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                
                val budget = Budget(category, limit, month, year)
                if (budgetManager.saveBudget(budget)) {
                    loadBudgets()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.budget_categories)
        )
        dialogBinding.etCategory.setAdapter(categoryAdapter)

        dialog.show()
    }

    private fun showEditBudgetDialog(budget: Budget) {
        val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Budget")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val category = dialogBinding.etCategory.text.toString()
                val limit = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
                
                val updatedBudget = budget.copy(
                    category = category,
                    limit = limit
                )
                budgetManager.updateBudget(updatedBudget)
                loadBudgets()
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Pre-fill the form
        dialogBinding.etCategory.setText(budget.category)
        dialogBinding.etAmount.setText(budget.limit.toString())

        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.budget_categories)
        )
        dialogBinding.etCategory.setAdapter(categoryAdapter)

        dialog.show()
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