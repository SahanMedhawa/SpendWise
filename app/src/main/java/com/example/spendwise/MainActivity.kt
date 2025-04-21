package com.example.spendwise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.spendwise.databinding.ActivityMainBinding
import com.example.spendwise.fragments.BudgetFragment
import com.example.spendwise.fragments.DashboardFragment
import com.example.spendwise.fragments.SettingsFragment
import com.example.spendwise.fragments.TransactionsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> loadFragment(DashboardFragment())
                R.id.navigation_transactions -> loadFragment(TransactionsFragment())
                R.id.navigation_budget -> loadFragment(BudgetFragment())
                R.id.navigation_settings -> loadFragment(SettingsFragment())
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