package com.example.spendwise.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapters.BudgetAdapter
import com.example.spendwise.databinding.DialogAddBudgetBinding
import com.example.spendwise.databinding.FragmentBudgetBinding
import com.example.spendwise.models.Budget
import com.example.spendwise.utils.BudgetManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
class BudgetFragment : Fragment(), Parcelable {

    @IgnoredOnParcel
    private var _binding: FragmentBudgetBinding? = null
    
    @IgnoredOnParcel
    private val binding get() = _binding!!
    
    @IgnoredOnParcel
    private lateinit var budgetManager: BudgetManager
    
    @IgnoredOnParcel
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            budgetManager = it.getParcelable(ARG_BUDGET_MANAGER)!!
        }
    }

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
        setupViews()
        observeBudgets()
    }

    private fun setupViews() {
        budgetAdapter = BudgetAdapter(
            onItemClick = { budget -> showEditBudgetDialog(budget) },
            onDeleteClick = { budget -> showDeleteConfirmationDialog(budget) }
        )
        binding.recyclerViewBudgets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
        }

        binding.fabAddBudget.setOnClickListener {
            showAddBudgetDialog()
        }
    }

    private fun observeBudgets() {
        viewLifecycleOwner.lifecycleScope.launch {
            budgetManager.getAllBudgets().collectLatest { budgets ->
                budgetAdapter.submitList(budgets)
                binding.emptyStateLayout.visibility = if (budgets.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showAddBudgetDialog() {
        val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)
        
        // Setup category spinner
        val categories = resources.getStringArray(R.array.categories)
        dialogBinding.spinnerCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories))

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_budget)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
                val category = dialogBinding.spinnerCategory.text.toString()

                if (amount != null && category.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        budgetManager.addBudget(
                            Budget(
                                category = category,
                                amount = amount,
                                period = "Monthly",
                                month = java.time.LocalDate.now().monthValue,
                                year = java.time.LocalDate.now().year
                            )
                        )
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showEditBudgetDialog(budget: Budget) {
        val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)
        
        // Setup category spinner
        val categories = resources.getStringArray(R.array.categories)
        dialogBinding.spinnerCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories))

        // Set initial values
        dialogBinding.etAmount.setText(budget.amount.toString())
        dialogBinding.spinnerCategory.setText(budget.category)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_budget)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
                val category = dialogBinding.spinnerCategory.text.toString()

                if (amount != null && category.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        budgetManager.updateBudget(
                            budget.copy(
                                category = category,
                                amount = amount
                            )
                        )
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(budget: Budget) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_budget)
            .setMessage(R.string.delete_budget_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    budgetManager.deleteBudget(budget)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BUDGET_MANAGER = "budget_manager"

        fun newInstance(budgetManager: BudgetManager) = BudgetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_BUDGET_MANAGER, budgetManager as Parcelable)
            }
        }
    }
} 