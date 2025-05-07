package com.example.spendwise.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapters.TransactionAdapter
import com.example.spendwise.databinding.DialogAddTransactionBinding
import com.example.spendwise.databinding.FragmentTransactionsBinding
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionManager: TransactionManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            transactionManager = args.getParcelable(ARG_TRANSACTION_MANAGER)!!
            preferencesManager = args.getParcelable(ARG_PREFERENCES_MANAGER)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeTransactions()
    }

    private fun setupViews() {
        transactionAdapter = TransactionAdapter(
            onEditClick = { transaction -> showEditTransactionDialog(transaction) },
            onDeleteClick = { transaction -> showDeleteConfirmationDialog(transaction) },
            preferencesManager = preferencesManager
        )
        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }

        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            transactionManager.getAllTransactions().collectLatest { transactions ->
                transactionAdapter.submitList(transactions)
            }
        }
    }

    private fun showAddTransactionDialog() {
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)

        val expenseCategories = resources.getStringArray(R.array.expense_categories)
        val incomeCategories = resources.getStringArray(R.array.income_categories)
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, expenseCategories)
        dialogBinding.spinnerCategory.setAdapter(categoryAdapter)

        // Listen for radio button changes
        dialogBinding.rbExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, expenseCategories)
                dialogBinding.spinnerCategory.setAdapter(adapter)
            }
        }
        dialogBinding.rbIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, incomeCategories)
                dialogBinding.spinnerCategory.setAdapter(adapter)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_transaction)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add, null) // Set to null initially
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
                val title = dialogBinding.etTitle.text.toString()
                val category = dialogBinding.spinnerCategory.text.toString()
                val type = if (dialogBinding.rbExpense.isChecked) {
                    TransactionType.EXPENSE
                } else {
                    TransactionType.INCOME
                }

                if (amount != null && title.isNotEmpty() && category.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        transactionManager.saveTransaction(
                            Transaction(
                                title = title,
                                amount = amount,
                                category = category,
                                type = type,
                                date = Date()
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

    private fun showEditTransactionDialog(transaction: Transaction) {
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_transaction)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
                val title = dialogBinding.etTitle.text.toString()
                val category = dialogBinding.spinnerCategory.text.toString()
                val type = if (dialogBinding.rbExpense.isChecked) {
                    TransactionType.EXPENSE
                } else {
                    TransactionType.INCOME
                }

                if (amount != null && title.isNotEmpty() && category.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        transactionManager.updateTransaction(
                            transaction.copy(
                                title = title,
                                amount = amount,
                                category = category,
                                type = type
                            )
                        )
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()

        // Setup category spinner
        val categories = resources.getStringArray(R.array.categories)
        dialogBinding.spinnerCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories))

        // Set initial values
        dialogBinding.etAmount.setText(transaction.amount.toString())
        dialogBinding.etTitle.setText(transaction.title)
        dialogBinding.rbExpense.isChecked = transaction.type == TransactionType.EXPENSE
        dialogBinding.rbIncome.isChecked = transaction.type == TransactionType.INCOME
        dialogBinding.spinnerCategory.setText(transaction.category)
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    transactionManager.deleteTransaction(transaction)
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
        private const val ARG_TRANSACTION_MANAGER = "transaction_manager"
        private const val ARG_PREFERENCES_MANAGER = "preferences_manager"

        fun newInstance(transactionManager: TransactionManager, preferencesManager: PreferencesManager) = TransactionsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_TRANSACTION_MANAGER, transactionManager as Parcelable)
                putParcelable(ARG_PREFERENCES_MANAGER, preferencesManager as Parcelable)
            }
        }
    }
} 