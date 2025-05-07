package com.example.spendwise.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.spendwise.R
import com.example.spendwise.databinding.DialogEditBudgetBinding
import com.example.spendwise.models.Budget

class EditBudgetDialogFragment(
    private val budget: Budget,
    private val onBudgetEdited: (Double) -> Unit
) : DialogFragment() {

    private var _binding: DialogEditBudgetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditBudgetBinding.inflate(LayoutInflater.from(context))

        binding.etAmount.setText(budget.amount.toString())
        binding.tvCategory.text = budget.category

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.edit_budget)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    onBudgetEdited(amount)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 