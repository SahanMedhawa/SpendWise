package com.example.spendwise.models

import com.google.gson.annotations.SerializedName

data class Budget(
    @SerializedName("category")
    val category: String,
    @SerializedName("limit")
    val limit: Double,
    @SerializedName("month")
    val month: Int,
    @SerializedName("year")
    val year: Int,
    @SerializedName("currentSpending")
    val currentSpending: Double = 0.0
) {
    val remaining: Double
        get() = limit - currentSpending

    val percentage: Int
        get() = ((currentSpending / limit) * 100).toInt()

    val isOverBudget: Boolean
        get() = currentSpending > limit

    val isNearLimit: Boolean
        get() = percentage >= 80 && !isOverBudget
} 