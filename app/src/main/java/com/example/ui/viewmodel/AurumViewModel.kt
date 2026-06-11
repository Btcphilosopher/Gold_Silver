package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiAssistant
import com.example.data.database.AppDatabase
import com.example.data.database.AutoInvestPlanEntity
import com.example.data.database.HoldingEntity
import com.example.data.database.TransactionEntity
import com.example.data.database.UserAccountEntity
import com.example.data.repository.AurumRepository
import com.example.data.repository.ProductSpec
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AssistantChatUiState {
    object Idle : AssistantChatUiState
    object Loading : AssistantChatUiState
    data class Success(val response: String) : AssistantChatUiState
    data class Error(val message: String) : AssistantChatUiState
}

data class ChatMessage(
    val sender: String, // "USER" or "ASSISTANT"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AurumViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AurumRepository(db.aurumDao())

    // UI exposed rates
    val selectedCurrency: StateFlow<String> = repository.selectedCurrency
    val goldSpotPriceUSD: StateFlow<Double> = repository.goldBaseUSD
    val silverSpotPriceUSD: StateFlow<Double> = repository.silverBaseUSD
    val platinumSpotPriceUSD: StateFlow<Double> = repository.platinumBaseUSD

    // DB flows bound to lifecycles
    val userAccount: StateFlow<UserAccountEntity> = repository.observeUserAccount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserAccountEntity())

    val holdings: StateFlow<List<HoldingEntity>> = repository.observeHoldings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val autoInvestPlans: StateFlow<List<AutoInvestPlanEntity>> = repository.observePlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Assistant State flow
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ASSISTANT", "Welcome to AURUM Wealth Services. I am your premium AI portfolio strategist. Ask me anything about gold souverigns, tax benefits, savings ratios, or physical vaulting solutions.")
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory

    private val _assistantState = MutableStateFlow<AssistantChatUiState>(AssistantChatUiState.Idle)
    val assistantState: StateFlow<AssistantChatUiState> = _assistantState

    // Tab Navigation state: "HOME", "MARKET", "PORTFOLIO", "VAULTS", "AI"
    private val _currentTab = MutableStateFlow("HOME")
    val currentTab: StateFlow<String> = _currentTab

    // Product detailed view reference
    private val _selectedProduct = MutableStateFlow<ProductSpec?>(null)
    val selectedProduct: StateFlow<ProductSpec?> = _selectedProduct

    // Filter category for marketplace
    private val _marketFilter = MutableStateFlow("ALL")
    val marketFilter: StateFlow<String> = _marketFilter

    init {
        // Run auto invest plans on vm start
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            autoInvestPlans.value.forEach { plan ->
                if (plan.isActive) {
                    repository.triggerPlanExecutionManually(plan)
                }
            }
        }
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun setCurrency(currency: String) {
        repository.setCurrency(currency)
    }

    fun setMarketFilter(filter: String) {
        _marketFilter.value = filter
    }

    fun viewProduct(product: ProductSpec?) {
        _selectedProduct.value = product
    }

    fun executeBuy(product: ProductSpec, quantity: Double, destination: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val success = repository.buyProduct(product, quantity, destination)
            if (success) {
                onSuccess()
            } else {
                onError("Insufficient fiat funds. Please fund your physical gold account.")
            }
        }
    }

    fun executeSell(holding: HoldingEntity, quantity: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val success = repository.sellVaultedHolding(holding, quantity)
            if (success) {
                onSuccess()
            } else {
                onError("Unable to process settlement request.")
            }
        }
    }

    fun requestPhysicalDelivery(holding: HoldingEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val success = repository.requestDelivery(holding)
            if (success) {
                onSuccess()
            } else {
                onError("Delivery request failed.")
            }
        }
    }

    fun fundAccount(amount: Double, currency: String, isDeposit: Boolean) {
        viewModelScope.launch {
            repository.adjustFiatBalance(amount, currency, isDeposit)
        }
    }

    fun submitKycStatus(verify: Boolean) {
        viewModelScope.launch {
            repository.updateKycStatus(verify)
        }
    }

    fun changeAccountType(type: String) {
        viewModelScope.launch {
            repository.updateAccountType(type)
        }
    }

    fun editProfile(fullName: String, address: String, deliveryPref: String) {
        viewModelScope.launch {
            repository.updateUserInfo(fullName, address, deliveryPref)
        }
    }

    fun addSavingsPlan(name: String, amount: Double, currency: String, interval: String, gPct: Int, sPct: Int, pPct: Int) {
        viewModelScope.launch {
            repository.createAutoInvestPlan(name, amount, currency, interval, gPct, sPct, pPct)
        }
    }

    fun removeSavingsPlan(plan: AutoInvestPlanEntity) {
        viewModelScope.launch {
            repository.deleteAutoInvestPlan(plan)
        }
    }

    fun triggerAutoInvestPlanManual(plan: AutoInvestPlanEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.triggerPlanExecutionManually(plan)
            onComplete(success)
        }
    }

    // AI Assistant Actions
    fun sendAssistantMessage(messageText: String) {
        if (messageText.isBlank()) return
        
        // Add User message
        _chatHistory.update { it + ChatMessage("USER", messageText) }
        _assistantState.value = AssistantChatUiState.Loading

        viewModelScope.launch {
            // Generate full portfolio context for AI to make answers customized!
            val currency = selectedCurrency.value
            val holds = holdings.value.joinToString { "${it.quantity} x ${it.productName} (${it.status})" }
            val balance = when(currency) {
                "GBP" -> "${userAccount.value.balanceGBP} GBP"
                "EUR" -> "${userAccount.value.balanceEUR} EUR"
                else -> "${userAccount.value.balanceUSD} USD"
            }
            
            val customizedPrompt = """
User Portfolio Context:
- Current Fiat Balance: $balance
- Selected Viewing Currency: $currency
- Active Holdings: $holds
- Account tier: ${userAccount.value.accountType} (verified: ${userAccount.value.isKycVerified})

User query: "$messageText"
"""
            val response = GeminiAssistant.askAssistant(customizedPrompt)
            _chatHistory.update { it + ChatMessage("ASSISTANT", response) }
            _assistantState.value = AssistantChatUiState.Success(response)
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage("ASSISTANT", "Secure channel re-established. How may AURUM Private Services assist you?")
        )
        _assistantState.value = AssistantChatUiState.Idle
    }
}

class AurumViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AurumViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AurumViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
