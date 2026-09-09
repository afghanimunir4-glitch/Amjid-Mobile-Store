package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val isBiometricEnabled: Boolean = true,
    val createdDate: String,
    val createdTime: String,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
