package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleIdCode: String,
    val productId: Long,
    val productName: String,
    val customerId: Long? = null,
    val customerName: String = "",
    val quantity: Int,
    val sellingPrice: Double,
    val purchasePriceAtSale: Double,
    val total: Double,
    val profit: Double,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
