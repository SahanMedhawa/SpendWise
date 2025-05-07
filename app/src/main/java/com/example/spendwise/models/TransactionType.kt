package com.example.spendwise.models

import androidx.room.TypeConverter

enum class TransactionType {
    INCOME,
    EXPENSE;

    companion object {
        @TypeConverter
        @JvmStatic
        fun fromString(value: String): TransactionType {
            return valueOf(value)
        }

        @TypeConverter
        @JvmStatic
        fun toString(type: TransactionType): String {
            return type.name
        }
    }
} 