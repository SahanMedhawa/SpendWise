package com.example.spendwise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.spendwise.databinding.ActivityMainBinding
import com.example.spendwise.fragments.BudgetFragment
import com.example.spendwise.fragments.DashboardFragment
import com.example.spendwise.fragments.SettingsFragment
import com.example.spendwise.fragments.TransactionsFragment
import com.example.spendwise.utils.BudgetManager
import com.example.spendwise.utils.PreferencesManager
import com.example.spendwise.utils.TransactionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionManager: TransactionManager
    private lateinit var budgetManager: BudgetManager
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // Initialize managers in the correct order
        transactionManager = TransactionManager(this)
        budgetManager = BudgetManager(this, transactionManager)
        transactionManager.setBudgetManager(budgetManager)
        preferencesManager = PreferencesManager(this)

        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment(transactionManager, preferencesManager))
                .commit()
        }

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> loadFragment(DashboardFragment(transactionManager, preferencesManager))
                R.id.navigation_transactions -> loadFragment(TransactionsFragment(transactionManager, preferencesManager))
                R.id.navigation_budget -> loadFragment(BudgetFragment(transactionManager, preferencesManager))
                R.id.navigation_settings -> loadFragment(SettingsFragment(transactionManager, preferencesManager))
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}