package com.example.spendwise.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var currency: String
        get() = prefs.getString(KEY_CURRENCY, "රු") ?: "රු"
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    companion object {
        private const val PREFS_NAME = "SpendWisePrefs"
        private const val KEY_CURRENCY = "currency"
    }
} 