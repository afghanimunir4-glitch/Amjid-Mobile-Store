package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseIdCode: String,
    val productId: Long,
    val productName: String,
    val supplierId: Long? = null,
    val supplierName: String = "",
    val quantity: Int,
    val purchasePrice: Double,
    val total: Double,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
