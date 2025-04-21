package com.example.spendwise.models

import com.google.gson.annotations.SerializedName
import java.util.Date
import java.util.UUID

data class Transaction(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("title")
    val title: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("category")
    val category: String,
    @SerializedName("type")
    val type: TransactionType,
    @SerializedName("date")
    val date: Date
) 