package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerIdCode: String,
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
