package com.example.spendwise.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapters.TransactionAdapter
import com.example.spendwise.databinding.DialogAddTransactionBinding
import com.example.spendwise.databinding.FragmentTransactionsBinding
import com.example.spendwise.models.Transaction
import com.example.spendwise.models.TransactionType
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import java.util.Date

class TransactionsFragment(
    private val transactionManager: TransactionManager,
    private val preferencesManager: PreferencesManager
) : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val transactions = mutableListOf<Transaction>()

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
        setupRecyclerView()
        setupFab()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = TransactionAdapter(
            transactions,
            onItemClick = { transaction ->
                // Show transaction details
                Toast.makeText(context, "Transaction clicked: ${transaction.title}", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { transaction ->
                // Show edit dialog
                showEditTransactionDialog(transaction)
            },
            onDeleteClick = { transaction ->
                // Show delete confirmation
                showDeleteConfirmation(transaction)
            }
        )
    }

    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog() {
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_transaction)
            .setView(dialogBinding.root)
            .create()

        // Setup category spinner with expense categories by default
        setupCategoryDropdown(dialogBinding, R.array.categories)

        // Handle radio button changes
        dialogBinding.typeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbIncome -> setupCategoryDropdown(dialogBinding, R.array.income_categories)
                R.id.rbExpense -> setupCategoryDropdown(dialogBinding, R.array.categories)
            }
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString()
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
            val category = dialogBinding.spinnerCategory.text.toString()
            val type = if (dialogBinding.rbIncome.isChecked) TransactionType.INCOME else TransactionType.EXPENSE

            if (validateInput(title, amount, category)) {
                val transaction = Transaction(
                    title = title,
                    amount = amount!!,
                    category = category,
                    type = type,
                    date = Date()
                )
                addTransaction(transaction)
                dialog.dismiss()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateTransaction(transaction: Transaction) {
        transactionManager.updateTransaction(transaction)
        loadTransactions()
    }

    private fun deleteTransaction(transaction: Transaction) {
        transactionManager.deleteTransaction(transaction)
        loadTransactions()
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.edit_transaction)
            .setView(dialogBinding.root)
            .create()

        // Pre-fill the form
        dialogBinding.etTitle.setText(transaction.title)
        dialogBinding.etAmount.setText(transaction.amount.toString())
        dialogBinding.rbIncome.isChecked = transaction.type == TransactionType.INCOME
        dialogBinding.rbExpense.isChecked = transaction.type == TransactionType.EXPENSE

        // Setup initial category dropdown based on transaction type
        val categoryArrayId = if (transaction.type == TransactionType.INCOME) {
            R.array.income_categories
        } else {
            R.array.categories
        }
        setupCategoryDropdown(dialogBinding, categoryArrayId)
        dialogBinding.spinnerCategory.setText(transaction.category)

        // Handle radio button changes
        dialogBinding.typeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbIncome -> setupCategoryDropdown(dialogBinding, R.array.income_categories)
                R.id.rbExpense -> setupCategoryDropdown(dialogBinding, R.array.categories)
            }
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString()
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
            val category = dialogBinding.spinnerCategory.text.toString()
            val type = if (dialogBinding.rbIncome.isChecked) TransactionType.INCOME else TransactionType.EXPENSE

            if (validateInput(title, amount, category)) {
                val updatedTransaction = transaction.copy(
                    title = title,
                    amount = amount!!,
                    category = category,
                    type = type,
                    date = transaction.date // Preserve the original date
                )
                updateTransaction(updatedTransaction)
                dialog.dismiss()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setPositiveButton(R.string.delete) { dialog: DialogInterface, _: Int ->
                deleteTransaction(transaction)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    private fun validateInput(title: String, amount: Double?, category: String): Boolean {
        if (title.isBlank()) {
            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
            return false
        }
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return false
        }
        if (category.isBlank()) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun addTransaction(transaction: Transaction) {
        transactionManager.saveTransaction(transaction)
        transactions.add(0, transaction)
        binding.recyclerView.adapter?.notifyItemInserted(0)
    }

    private fun loadTransactions() {
        transactions.clear()
        transactions.addAll(transactionManager.getTransactions())
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun setupCategoryDropdown(dialogBinding: DialogAddTransactionBinding, categoryArrayId: Int) {
        val categories = resources.getStringArray(categoryArrayId)
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        dialogBinding.spinnerCategory.setText("") // Clear current selection
        dialogBinding.spinnerCategory.setAdapter(categoryAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 