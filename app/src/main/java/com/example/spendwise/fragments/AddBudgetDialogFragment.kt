package com.example.spendwise.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.spendwise.R
import com.example.spendwise.databinding.DialogAddBudgetBinding
import com.example.spendwise.models.Budget

class AddBudgetDialogFragment : DialogFragment() {
    private var _binding: DialogAddBudgetBinding? = null
    private val binding get() = _binding!!
    private var onBudgetAdded: ((Budget) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        // Setup category spinner
        val categories = resources.getStringArray(R.array.categories)
        binding.spinnerCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddBudgetBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_budget)
            .setView(binding.root)
            .setPositiveButton(R.string.add) { _, _ ->
                val amount = binding.etAmount.text.toString().toDoubleOrNull()
                val category = binding.spinnerCategory.text.toString()
                if (amount != null && category.isNotEmpty()) {
                    val budget = Budget(
                        category = category,
                        amount = amount,
                        period = "Monthly",
                        month = java.time.LocalDate.now().monthValue,
                        year = java.time.LocalDate.now().year
                    )
                    onBudgetAdded?.invoke(budget)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(onBudgetAdded: (Budget) -> Unit): AddBudgetDialogFragment {
            return AddBudgetDialogFragment().apply {
                this.onBudgetAdded = onBudgetAdded
            }
        }
    }
} 