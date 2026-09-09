package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierIdCode: String,
    val name: String,
    val phoneNumber: String,
    val address: String,
    val notes: String,
    val dateAdded: String,
    val timeAdded: String,
    val lastUpdatedDate: String,
    val lastUpdatedTime: String,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
