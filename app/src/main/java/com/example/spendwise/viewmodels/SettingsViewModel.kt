package com.example.spendwise.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwise.utils.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    fun getCurrency(): Flow<String> = flow {
        emit(preferencesManager.getCurrency())
    }

    fun getTheme(): Flow<String> = flow {
        emit(preferencesManager.getTheme())
    }

    fun isPasscodeEnabled(): Flow<Boolean> = flow {
        emit(preferencesManager.isPasscodeEnabled())
    }

    fun setCurrency(currency: String) {
        preferencesManager.setCurrency(currency)
    }

    fun setTheme(theme: String) {
        preferencesManager.setTheme(theme)
    }

    fun setPasscodeEnabled(enabled: Boolean) {
        preferencesManager.setPasscodeEnabled(enabled)
    }

    class Factory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(preferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 