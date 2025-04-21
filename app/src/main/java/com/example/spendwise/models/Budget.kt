package com.example.spendwise.models

data class Budget(
    val category: String,
    val limit: Double,
    val period: String = "Monthly",
    var currentSpending: Double = 0.0
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