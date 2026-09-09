package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // "PURCHASE", "SALE", "EDIT_PURCHASE", "EDIT_SALE", "MANUAL_EDIT"
    val referenceId: Long? = null,
    val quantityChange: Int, // e.g. +3, -1
    val previousStock: Int,
    val newStock: Int,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
