package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productIdCode: String,
    val name: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val dateAdded: String,
    val timeAdded: String,
    val lastUpdatedDate: String,
    val lastUpdatedTime: String,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val updatedAtTimestamp: Long = System.currentTimeMillis()
)
