package com.example.spendwise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.spendwise.databinding.ActivityMainBinding
import com.example.spendwise.fragments.*
import com.example.spendwise.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionManager: TransactionManager
    private lateinit var budgetManager: BudgetManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var backupManager: BackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        preferencesManager = PreferencesManager(this)
        transactionManager = TransactionManager(this, preferencesManager)
        budgetManager = BudgetManager(this, transactionManager)
        backupManager = BackupManager(this, transactionManager, budgetManager, preferencesManager)

        // Set up circular dependencies
        transactionManager.setBudgetManager(budgetManager)

        setupNavigation()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment.newInstance(transactionManager, preferencesManager))
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(TransactionsFragment.newInstance(transactionManager, preferencesManager))
                    true
                }
                R.id.nav_budget -> {
                    loadFragment(BudgetFragment.newInstance(budgetManager))
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment.newInstance(preferencesManager, backupManager))
                    true
                }
                else -> false
            }
        }

        // Set default fragment
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}