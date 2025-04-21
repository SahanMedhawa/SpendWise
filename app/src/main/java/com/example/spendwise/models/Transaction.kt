package com.example.spendwise.models

import java.util.Date

data class Transaction(
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: Date
) 