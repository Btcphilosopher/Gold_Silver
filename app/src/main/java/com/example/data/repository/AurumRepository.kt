package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Marketplace Product Specifications ---

data class ProductSpec(
    val id: String,
    val name: String,
    val metalType: String, // "GOLD", "SILVER", "PLATINUM"
    val category: String, // "Bars", "Coins", "Sovereigns", "Fractional", "Tubes", "Monster boxes"
    val weightGrams: Double,
    val purity: String,
    val manufacturer: String,
    val premiumPercent: Double, // premium over spot rate (e.g. 3.5 for 3.5%)
    val imageRes: String,
    val deliveryEstimate: String = "2-4 Business Days"
)

class AurumRepository(private val aurumDao: AurumDao) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Current global currency selector state (default: GBP)
    private val _selectedCurrency = MutableStateFlow("GBP")
    val selectedCurrency: StateFlow<String> = _selectedCurrency

    // Dynamic base prices in USD per troy ounce
    private val _goldBaseUSD = MutableStateFlow(2385.50)
    val goldBaseUSD: StateFlow<Double> = _goldBaseUSD

    private val _silverBaseUSD = MutableStateFlow(29.65)
    val silverBaseUSD: StateFlow<Double> = _silverBaseUSD

    private val _platinumBaseUSD = MutableStateFlow(985.20)
    val platinumBaseUSD: StateFlow<Double> = _platinumBaseUSD

    // Exchange rates from USD to currency
    private val usdToGbpRate = 1.0 / 1.28   // 1 GBP = 1.28 USD
    private val usdToEurRate = 1.0 / 1.08   // 1 EUR = 1.08 USD

    // Live fluctuations loop
    init {
        repositoryScope.launch {
            // Check & initialize default user account if empty
            aurumDao.getUserAccount().firstOrNull()?.let {
                // Account already exists
            } ?: run {
                aurumDao.insertUserAccount(UserAccountEntity())
                // Pre-populate some initial premium holdings to look gorgeous
                aurumDao.insertHolding(
                    HoldingEntity(
                        metalType = "GOLD",
                        productName = "1oz Gold Sovereign Coin",
                        quantity = 2.0,
                        weightGrams = 31.1035 * 2,
                        purity = "916.7",
                        status = "VAULTED",
                        location = "London Vault (Segregated - Safe A3)",
                        certificateId = "CERT-AU-5012A"
                    )
                )
                aurumDao.insertHolding(
                    HoldingEntity(
                        metalType = "SILVER",
                        productName = "1kg Metalor Silver Bar",
                        quantity = 1.0,
                        weightGrams = 1000.0,
                        purity = "999.0",
                        status = "VAULTED",
                        location = "Zurich Vault (Allocated - Drawer C8)",
                        certificateId = "CERT-AG-8094B"
                    )
                )
                aurumDao.insertHolding(
                    HoldingEntity(
                        metalType = "GOLD",
                        productName = "100g Valcambi Gold Bar",
                        quantity = 1.0,
                        weightGrams = 100.0,
                        purity = "999.9",
                        status = "DELIVERED",
                        location = "Home Delivered (Insured Courier)",
                        certificateId = "DEL-AU-9901Z"
                    )
                )

                // Insert one initial active Auto-Invest plan for demo
                aurumDao.insertAutoInvestPlan(
                    AutoInvestPlanEntity(
                        name = "Gold & Silver Wealth Reserve",
                        amount = 250.0,
                        currency = "GBP",
                        interval = "MONTHLY",
                        goldRatio = 70,
                        silverRatio = 30,
                        platinumRatio = 0,
                        isActive = true,
                        lastExecutedTimestamp = System.currentTimeMillis() - 15 * 86400 * 1000L // 15 days ago
                    )
                )

                // Log default purchase transactions matching the pre-populated holdings
                aurumDao.insertTransaction(
                    TransactionEntity(
                        type = "BUY",
                        metalType = "GOLD",
                        productName = "1oz Gold Sovereign Coin",
                        quantity = 2.0,
                        unitPrice = 1860.0,
                        premium = 85.0,
                        totalCost = 3805.0,
                        destination = "VAULT",
                        status = "COMPLETED"
                    )
                )
                aurumDao.insertTransaction(
                    TransactionEntity(
                        type = "BUY",
                        metalType = "SILVER",
                        productName = "1kg Metalor Silver Bar",
                        quantity = 1.0,
                        unitPrice = 920.0,
                        premium = 110.0,
                        totalCost = 1030.0,
                        destination = "VAULT",
                        status = "COMPLETED"
                    )
                )
            }

            // Fluctuations loop to make prices live
            while (true) {
                kotlinx.coroutines.delay(4000)
                // small random walk -0.04% to +0.05%
                val goldWalk = 1.0 + (Random.nextDouble(-0.0004, 0.0005))
                val silverWalk = 1.0 + (Random.nextDouble(-0.0008, 0.0010))
                val platinumWalk = 1.0 + (Random.nextDouble(-0.0006, 0.0007))

                _goldBaseUSD.update { it * goldWalk }
                _silverBaseUSD.update { it * silverWalk }
                _platinumBaseUSD.update { it * platinumWalk }
            }
        }
    }

    fun setCurrency(currency: String) {
        if (currency in listOf("GBP", "USD", "EUR")) {
            _selectedCurrency.value = currency
        }
    }

    // Converters to selected or specified currency
    fun getRateToUSD(currency: String): Double {
        return when (currency) {
            "GBP" -> usdToGbpRate
            "EUR" -> usdToEurRate
            else -> 1.0
        }
    }

    fun convertUSDToCurrency(usdAmount: Double, targetCurrency: String): Double {
        return usdAmount * getRateToUSD(targetCurrency)
    }

    // Spot Price flows for Gold, Silver, Platinum
    fun getSpotPricePerOunce(metal: String, targetCurrency: String): Double {
        val usdPrice = when (metal.uppercase()) {
            "GOLD" -> _goldBaseUSD.value
            "SILVER" -> _silverBaseUSD.value
            "PLATINUM" -> _platinumBaseUSD.value
            else -> _goldBaseUSD.value
        }
        return convertUSDToCurrency(usdPrice, targetCurrency)
    }

    fun getSpotPricePerGram(metal: String, targetCurrency: String): Double {
        return getSpotPricePerOunce(metal, targetCurrency) / 31.1035
    }

    // DB flows
    fun observeUserAccount(): Flow<UserAccountEntity> {
        return aurumDao.getUserAccount().map { it ?: UserAccountEntity() }
    }

    fun observeHoldings(): Flow<List<HoldingEntity>> = aurumDao.getAllHoldings()

    fun observeTransactions(): Flow<List<TransactionEntity>> = aurumDao.getAllTransactions()

    fun observePlans(): Flow<List<AutoInvestPlanEntity>> = aurumDao.getAutoInvestPlans()

    // Base Products catalog specifications
    val products = listOf(
        // Gold Bars & Coins
        ProductSpec("au_bar_100", "100g Valcambi Gold Bar", "GOLD", "Bars", 100.0, "999.9", "Valcambi", 1.8, "gold_bar_100"),
        ProductSpec("au_coin_sovereign", "1oz Gold Sovereign", "GOLD", "Sovereigns", 31.1035, "916.7", "Royal Mint", 4.2, "gold_sov"),
        ProductSpec("au_coin_britannia", "1oz Gold Britannia", "GOLD", "Coins", 31.1035, "999.9", "Royal Mint", 3.1, "gold_brit"),
        ProductSpec("au_bar_10", "10g PAMP Gold Card Bar", "GOLD", "Fractional", 10.0, "999.9", "PAMP", 5.9, "gold_pamp"),
        
        // Silver Bars & Coins
        ProductSpec("ag_bar_1kg", "1kg Metalor Silver Bar", "SILVER", "Bars", 1000.0, "999.0", "Metalor", 10.5, "silver_bar_1k"),
        ProductSpec("ag_coin_brit", "1oz Silver Britannia Coin", "SILVER", "Coins", 31.1035, "999.0", "Royal Mint", 15.0, "silver_brit"),
        ProductSpec("ag_tube_25", "Silver Britannia Tube (25 Coins)", "SILVER", "Tubes", 31.1035 * 25, "999.0", "Royal Mint", 12.8, "silver_tube"),
        ProductSpec("ag_monster_500", "500-Coin Silver Monster Box", "SILVER", "Monster boxes", 31.1035 * 500, "999.0", "Royal Mint", 9.5, "silver_monster"),
        
        // Platinum
        ProductSpec("pt_coin_maple", "1oz Platinum Maple Leaf", "PLATINUM", "Coins", 31.1035, "999.5", "Royal Canadian Mint", 4.8, "plat_maple"),
        ProductSpec("pt_bar_50", "50g Platinum Bar", "PLATINUM", "Bars", 50.0, "999.5", "Argor-Heraeus", 3.2, "plat_bar_50")
    )

    // Calculate details for a specific product
    fun calculateProductPricing(product: ProductSpec, currency: String): PricingDetails {
        val spotPerGram = getSpotPricePerGram(product.metalType, currency)
        val metalValue = spotPerGram * product.weightGrams
        val premiumCost = metalValue * (product.premiumPercent / 100.0)
        val totalCost = metalValue + premiumCost
        
        val spotPricePerOunce = getSpotPricePerOunce(product.metalType, currency)

        return PricingDetails(
            metalValue = metalValue,
            premiumValue = premiumCost,
            totalCost = totalCost,
            spotOuncePrice = spotPricePerOunce,
            premiumPercentCheck = product.premiumPercent
        )
    }

    // Primary Purchase mechanism
    suspend fun buyProduct(product: ProductSpec, quantity: Double, destination: String): Boolean {
        val currency = _selectedCurrency.value
        val pricing = calculateProductPricing(product, currency)
        val totalDebit = pricing.totalCost * quantity

        val user = aurumDao.getUserAccount().firstOrNull() ?: UserAccountEntity()
        val hasSufficientBalance = when (currency) {
            "GBP" -> user.balanceGBP >= totalDebit
            "EUR" -> user.balanceEUR >= totalDebit
            else -> user.balanceUSD >= totalDebit
        }

        if (!hasSufficientBalance) return false

        // Deduct money
        val updatedUser = when (currency) {
            "GBP" -> user.copy(balanceGBP = user.balanceGBP - totalDebit)
            "EUR" -> user.copy(balanceEUR = user.balanceEUR - totalDebit)
            else -> user.copy(balanceUSD = user.balanceUSD - totalDebit)
        }
        aurumDao.insertUserAccount(updatedUser)

        // Insert / Add Vaulted holding
        val location = if (destination == "VAULT") {
            "UK London Vault Facility"
        } else {
            "Home Secure Delivery Pending"
        }
        val status = if (destination == "VAULT") "VAULTED" else "DELIVERED"
        val certId = if (destination == "VAULT") "CERT-VAULT-${Random.nextInt(10000, 99999)}" else "DELIVER-${Random.nextInt(50000, 99999)}"

        aurumDao.insertHolding(
            HoldingEntity(
                metalType = product.metalType,
                productName = product.name,
                quantity = quantity,
                weightGrams = product.weightGrams * quantity,
                purity = product.purity,
                status = status,
                location = location,
                certificateId = certId
            )
        )

        // Record Transaction
        aurumDao.insertTransaction(
            TransactionEntity(
                type = "BUY",
                metalType = product.metalType,
                productName = product.name,
                quantity = quantity,
                unitPrice = pricing.totalCost,
                premium = pricing.premiumValue,
                totalCost = totalDebit,
                destination = destination,
                status = if (destination == "VAULT") "COMPLETED" else "PROCESSING"
            )
        )

        return true
    }

    // Sell mechanism
    suspend fun sellVaultedHolding(holding: HoldingEntity, sellQuantity: Double): Boolean {
        if (holding.status != "VAULTED") return false
        if (holding.quantity < sellQuantity) return false

        val currency = _selectedCurrency.value
        // Determine the metal's live spot rate, let's offer a standard dealer sell-back price (e.g. 97.5% of spot)
        val spotPerGram = getSpotPricePerGram(holding.metalType, currency)
        val metalValuePerGram = spotPerGram
        val baseUnitPrice = metalValuePerGram * (holding.weightGrams / holding.quantity)
        val sellBackUnitPrice = baseUnitPrice * 0.975 // 2.5% sell-back spread
        val totalCredit = sellBackUnitPrice * sellQuantity

        val user = aurumDao.getUserAccount().firstOrNull() ?: UserAccountEntity()

        // Update holding quantity
        if (holding.quantity == sellQuantity) {
            aurumDao.deleteHolding(holding)
        } else {
            aurumDao.updateHolding(
                holding.copy(
                    quantity = holding.quantity - sellQuantity,
                    weightGrams = holding.weightGrams - (holding.weightGrams / holding.quantity * sellQuantity)
                )
            )
        }

        // Credit fiat balance
        val updatedUser = when (currency) {
            "GBP" -> user.copy(balanceGBP = user.balanceGBP + totalCredit)
            "EUR" -> user.copy(balanceEUR = user.balanceEUR + totalCredit)
            else -> user.copy(balanceUSD = user.balanceUSD + totalCredit)
        }
        aurumDao.insertUserAccount(updatedUser)

        // Record sell action
        aurumDao.insertTransaction(
            TransactionEntity(
                type = "SELL",
                metalType = holding.metalType,
                productName = holding.productName,
                quantity = sellQuantity,
                unitPrice = sellBackUnitPrice,
                premium = -(baseUnitPrice * 0.025), // negative premium representation
                totalCost = totalCredit,
                destination = "PLATFORM_RESELL",
                status = "COMPLETED"
            )
        )

        return true
    }

    // Direct Delivery Request of vaulted holdings
    suspend fun requestDelivery(holding: HoldingEntity): Boolean {
        if (holding.status != "VAULTED") return false

        // Update status of holding
        aurumDao.updateHolding(
            holding.copy(
                status = "DELIVERED",
                location = "Home Delivered (Secure Courier Service)",
                certificateId = "DEL-SHIPPED-${Random.nextInt(10000, 99999)}"
            )
        )

        // Record Delivery details
        aurumDao.insertTransaction(
            TransactionEntity(
                type = "DELIVER_REQUEST",
                metalType = holding.metalType,
                productName = holding.productName,
                quantity = holding.quantity,
                unitPrice = 0.0, // shipping service
                premium = 15.0, // basic surcharge/shipping
                totalCost = 15.0,
                destination = "HOME_DELIVERY",
                status = "SHIPPED",
                trackingNumber = "TRACK-AU-${Random.nextInt(100000, 999999)}"
            )
        )

        return true
    }

    // Create custom Auto-Invest Plan
    suspend fun createAutoInvestPlan(
        name: String,
        amount: Double,
        currency: String,
        interval: String,
        goldPct: Int,
        silverPct: Int,
        platPct: Int
    ) {
        aurumDao.insertAutoInvestPlan(
            AutoInvestPlanEntity(
                name = name,
                amount = amount,
                currency = currency,
                interval = interval,
                goldRatio = goldPct,
                silverRatio = silverPct,
                platinumRatio = platPct,
                isActive = true,
                lastExecutedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteAutoInvestPlan(plan: AutoInvestPlanEntity) {
        aurumDao.deleteAutoInvestPlan(plan)
    }

    // Run active Auto-Invest plans
    suspend fun triggerPlanExecutionManually(plan: AutoInvestPlanEntity): Boolean {
        val user = aurumDao.getUserAccount().firstOrNull() ?: UserAccountEntity()
        val currency = plan.currency
        val totalAmount = plan.amount

        val hasBalance = when (currency) {
            "GBP" -> user.balanceGBP >= totalAmount
            "EUR" -> user.balanceEUR >= totalAmount
            else -> user.balanceUSD >= totalAmount
        }

        if (!hasBalance) return false

        // Deduct money
        val updatedUser = when (currency) {
            "GBP" -> user.copy(balanceGBP = user.balanceGBP - totalAmount)
            "EUR" -> user.copy(balanceEUR = user.balanceEUR - totalAmount)
            else -> user.copy(balanceUSD = user.balanceUSD - totalAmount)
        }
        aurumDao.insertUserAccount(updatedUser)

        // Buy proportionate metal quantities based on target allocation strategy
        // Gold purchase
        if (plan.goldRatio > 0) {
            val allocatedCash = totalAmount * (plan.goldRatio / 100.0)
            val spotPerGram = getSpotPricePerGram("GOLD", currency)
            val goldAcquisitionPremium = 1.03 // 3% premium for fractional savings engine
            val goldPriceForPlan = spotPerGram * goldAcquisitionPremium
            val weightAcquired = allocatedCash / goldPriceForPlan
            aurumDao.insertHolding(
                HoldingEntity(
                    metalType = "GOLD",
                    productName = "Savings Portfolio Gold Fractions",
                    quantity = weightAcquired / 31.1035, // fraction of oz
                    weightGrams = weightAcquired,
                    purity = "999.9",
                    status = "VAULTED",
                    location = "Allocated Savings Pool",
                    certificateId = "CERT-AUV-${Random.nextInt(10000, 99999)}"
                )
            )
        }

        // Silver purchase
        if (plan.silverRatio > 0) {
            val allocatedCash = totalAmount * (plan.silverRatio / 100.0)
            val spotPerGram = getSpotPricePerGram("SILVER", currency)
            val silverAcquisitionPremium = 1.10 // 10% premium for physical silver pool
            val silverPriceForPlan = spotPerGram * silverAcquisitionPremium
            val weightAcquired = allocatedCash / silverPriceForPlan
            aurumDao.insertHolding(
                HoldingEntity(
                    metalType = "SILVER",
                    productName = "Savings Portfolio Silver Fractions",
                    quantity = weightAcquired / 31.1035,
                    weightGrams = weightAcquired,
                    purity = "999.0",
                    status = "VAULTED",
                    location = "Allocated Savings Pool",
                    certificateId = "CERT-AGV-${Random.nextInt(10000, 99999)}"
                )
            )
        }

        // Platinum purchase
        if (plan.platinumRatio > 0) {
            val allocatedCash = totalAmount * (plan.platinumRatio / 100.0)
            val spotPerGram = getSpotPricePerGram("PLATINUM", currency)
            val platAcquisitionPremium = 1.05 // 5% premium
            val platPriceForPlan = spotPerGram * platAcquisitionPremium
            val weightAcquired = allocatedCash / platPriceForPlan
            aurumDao.insertHolding(
                HoldingEntity(
                    metalType = "PLATINUM",
                    productName = "Savings Portfolio Platinum Fractions",
                    quantity = weightAcquired / 31.1035,
                    weightGrams = weightAcquired,
                    purity = "999.5",
                    status = "VAULTED",
                    location = "Allocated Savings Pool",
                    certificateId = "CERT-PTV-${Random.nextInt(10000, 99999)}"
                )
            )
        }

        // Record a structured savings transaction
        aurumDao.insertTransaction(
            TransactionEntity(
                type = "BUY",
                metalType = "MIXED",
                productName = "Auto-Invest Plan: ${plan.name}",
                quantity = 1.0,
                unitPrice = totalAmount,
                premium = totalAmount * 0.05, // estimated pooled premium
                totalCost = totalAmount,
                destination = "VAULT",
                status = "COMPLETED"
            )
        )

        // Update execution timestamp
        aurumDao.insertAutoInvestPlan(plan.copy(lastExecutedTimestamp = System.currentTimeMillis()))
        return true
    }

    // Direct mock fiat deposits/withdrawals for portfolio buying power
    suspend fun adjustFiatBalance(amount: Double, currency: String, isDeposit: Boolean): Boolean {
        val user = aurumDao.getUserAccount().firstOrNull() ?: UserAccountEntity()
        val modifier = if (isDeposit) 1.0 else -1.0
        val change = amount * modifier

        val updatedUser = when (currency) {
            "GBP" -> {
                if (!isDeposit && user.balanceGBP < amount) return false
                user.copy(balanceGBP = user.balanceGBP + change)
            }
            "EUR" -> {
                if (!isDeposit && user.balanceEUR < amount) return false
                user.copy(balanceEUR = user.balanceEUR + change)
            }
            else -> {
                if (!isDeposit && user.balanceUSD < amount) return false
                user.copy(balanceUSD = user.balanceUSD + change)
            }
        }
        aurumDao.insertUserAccount(updatedUser)

        aurumDao.insertTransaction(
            TransactionEntity(
                type = if (isDeposit) "DEPOSIT" else "WITHDRAWAL",
                metalType = "FIAT",
                productName = if (isDeposit) "Bank Deposit (Open Banking)" else "Bank Wire Withdrawal",
                quantity = 1.0,
                unitPrice = amount,
                premium = 0.0,
                totalCost = amount,
                destination = "FIAT_ACCOUNT",
                status = "COMPLETED"
            )
        )
        return true
    }

    // Update User KYC and metadata
    suspend fun updateKycStatus(isVerified: Boolean) {
        val user = aurumDao.getUserAccount().firstOrNull() ?: UserAccountEntity()
        aurumDao.insertUserAccount(user.copy(isKycVerified = isVerified))
    }

    suspend fun updateAccountType(type: String) {
        val user = aurumDao.getUserAccount().firstOrNull() ?: UserAccountEntity()
        aurumDao.insertUserAccount(user.copy(accountType = type))
    }

    suspend fun updateUserInfo(fullName: String, address: String, deliveryPreference: String) {
        val user = aurumDao.getUserAccount().firstOrNull() ?: UserAccountEntity()
        aurumDao.insertUserAccount(user.copy(
            fullName = fullName,
            address = address,
            deliveryPreference = deliveryPreference
        ))
    }
}

data class PricingDetails(
    val metalValue: Double,
    val premiumValue: Double,
    val totalCost: Double,
    val spotOuncePrice: Double,
    val premiumPercentCheck: Double
)
