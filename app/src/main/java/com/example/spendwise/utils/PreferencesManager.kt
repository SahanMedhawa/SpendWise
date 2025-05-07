package com.example.spendwise.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
class PreferencesManager(
    private val context: @RawValue Context
) : Parcelable {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "light") ?: "light"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getPasscode(): String? {
        return prefs.getString(KEY_PASSCODE, null)
    }

    fun setPasscode(passcode: String) {
        prefs.edit().putString(KEY_PASSCODE, passcode).apply()
    }

    fun isPasscodeEnabled(): Boolean {
        return prefs.getBoolean(KEY_PASSCODE_ENABLED, false)
    }

    fun setPasscodeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PASSCODE_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "SpendWisePrefs"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_THEME = "theme"
        private const val KEY_PASSCODE = "passcode"
        private const val KEY_PASSCODE_ENABLED = "passcode_enabled"
    }
} 