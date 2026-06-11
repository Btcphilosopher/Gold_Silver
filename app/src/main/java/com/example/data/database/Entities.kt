package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val metalType: String, // "GOLD", "SILVER", "PLATINUM"
    val productName: String, // e.g. "1oz Britannia Gold Coin"
    val quantity: Double,
    val weightGrams: Double,
    val purity: String, // e.g. "999.9"
    val status: String, // "VAULTED", "DELIVERED"
    val location: String, // e.g. "London Vault (Segregated)"
    val certificateId: String, // e.g. "CERT-AU-10823"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "BUY", "SELL", "DELIVER_REQUEST"
    val metalType: String,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val premium: Double, // Premium over spot
    val totalCost: Double,
    val destination: String, // "VAULT", "HOME_DELIVERY"
    val status: String, // "COMPLETED", "PROCESSING", "SHIPPED", "DELIVERED"
    val trackingNumber: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "auto_invest_plans")
data class AutoInvestPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // "Gold Accumulation", etc.
    val amount: Double,
    val currency: String, // "GBP", "USD", "EUR"
    val interval: String, // "WEEKLY", "MONTHLY"
    val goldRatio: Int, // e.g., 70 for 70%
    val silverRatio: Int, // e.g., 30 for 30%
    val platinumRatio: Int, // e.g., 0 for 0%
    val isActive: Boolean = true,
    val lastExecutedTimestamp: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey val id: Int = 1, // Only single user configuration
    val balanceGBP: Double = 25000.0, // starts with mock capital
    val balanceUSD: Double = 30000.0,
    val balanceEUR: Double = 28000.0,
    val isKycVerified: Boolean = false,
    val accountType: String = "INDIVIDUAL", // "INDIVIDUAL", "BUSINESS", "OTC"
    val fullName: String = "Thomas Sterling",
    val businessName: String = "",
    val email: String = "tom@ahyx.org",
    val address: String = "12 Lombard St, London, EC3V 9AH",
    val deliveryPreference: String = "Insured Courier",
    val mfaEnabled: Boolean = true,
    val biometricEnabled: Boolean = false
)
