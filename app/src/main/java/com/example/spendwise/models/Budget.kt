package com.example.spendwise.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "period")
    val period: String,

    @ColumnInfo(name = "month")
    val month: Int,

    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "current_spending")
    val currentSpending: Double = 0.0
) {
    val remaining: Double
        get() = amount - currentSpending

    val percentage: Int
        get() = ((currentSpending / amount) * 100).toInt()

    val isOverBudget: Boolean
        get() = currentSpending > amount

    val isNearLimit: Boolean
        get() = percentage >= 80 && !isOverBudget
} 