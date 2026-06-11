package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AutoInvestPlanEntity
import com.example.data.database.HoldingEntity
import com.example.data.database.TransactionEntity
import com.example.data.database.UserAccountEntity
import com.example.data.repository.ProductSpec
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantChatUiState
import com.example.ui.viewmodel.AurumViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: AurumViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val user by viewModel.userAccount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AurumGold, AurumDarkGold)
                                    )
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ObsidianCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AURUM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = AurumGold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "DIGITAL BULLION BANK",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = SterlingSilver,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    // Currency Selection Dropdown Trigger
                    var showCurrDialog by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SteelCardBorder)
                            .clickable { showCurrDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = AurumGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PlatinumWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Account Tier Badge
                    val badgeColor = when (user.accountType) {
                        "BUSINESS" -> Color(0xFF1E88E5)
                        "OTC" -> Color(0xFF8E24AA)
                        else -> AurumGold
                    }
                    val badgeLabel = when (user.accountType) {
                        "BUSINESS" -> "BIZ CORP"
                        "OTC" -> "OTC TIER"
                        else -> if (user.isKycVerified) "VERIFIED" else "SANDBOX"
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    if (showCurrDialog) {
                        AlertDialog(
                            onDismissRequest = { showCurrDialog = false },
                            confirmButton = {},
                            dismissButton = {
                                TextButton(onClick = { showCurrDialog = false }) {
                                    Text("Close", color = AurumGold)
                                }
                            },
                            title = { Text("Select Account Currency", color = AurumGold) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("All holdings, live spot rates, purchases, and quotes will adapt instantly.", color = SterlingSilver, fontSize = 12.sp)
                                    listOf("GBP", "USD", "EUR").forEach { curr ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (currency == curr) SteelCardBorder else Color.Transparent)
                                                .clickable {
                                                    viewModel.setCurrency(curr)
                                                    showCurrDialog = false
                                                }
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = when (curr) {
                                                    "GBP" -> "British Pound (£)"
                                                    "EUR" -> "Euro (€)"
                                                    else -> "US Dollar ($)"
                                                },
                                                color = if (currency == curr) AurumGold else PlatinumWhite,
                                                fontWeight = if (currency == curr) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (currency == curr) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AurumGold)
                                            }
                                        }
                                    }
                                }
                            },
                            containerColor = SlateGraphite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianCharcoal,
                    titleContentColor = PlatinumWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianCharcoal,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val tabs = listOf(
                    NavigationTabItem("HOME", Icons.Default.Dashboard, "Home"),
                    NavigationTabItem("MARKET", Icons.Default.Storefront, "Market"),
                    NavigationTabItem("PORTFOLIO", Icons.Default.PieChart, "Portfolio"),
                    NavigationTabItem("VAULTS", Icons.Default.VpnKey, "Vaults"),
                    NavigationTabItem("AI", Icons.Default.AutoAwesome, "Wealth AI")
                )
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab.route,
                        onClick = { viewModel.selectTab(tab.route) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(text = tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianCharcoal,
                            selectedTextColor = AurumGold,
                            indicatorColor = AurumGold,
                            unselectedIconColor = SterlingSilver.copy(alpha = 0.6f),
                            unselectedTextColor = SterlingSilver.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.route.lowercase()}")
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "HOME" -> HomeScreen(viewModel)
                "MARKET" -> MarketScreen(viewModel)
                "PORTFOLIO" -> PortfolioScreen(viewModel)
                "VAULTS" -> VaultsScreen(viewModel)
                "AI" -> AiAssistantScreen(viewModel)
            }
        }
    }
}

data class NavigationTabItem(val route: String, val icon: ImageVector, val label: String)

// ==================== HOME SCREEN ====================

@Composable
fun HomeScreen(viewModel: AurumViewModel) {
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val goldUSD by viewModel.goldSpotPriceUSD.collectAsStateWithLifecycle()
    val silverUSD by viewModel.silverSpotPriceUSD.collectAsStateWithLifecycle()
    val platinumUSD by viewModel.platinumSpotPriceUSD.collectAsStateWithLifecycle()
    
    val currentGoldPrice = viewModel.repository.convertUSDToCurrency(goldUSD, currency)
    val currentSilverPrice = viewModel.repository.convertUSDToCurrency(silverUSD, currency)
    val currentPlatinumPrice = viewModel.repository.convertUSDToCurrency(platinumUSD, currency)

    val sym = when(currency) {
        "GBP" -> "£"
        "EUR" -> "€"
        else -> "$"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Total Portfolio Banner Quick Summary
        item {
            WelcomeBanner(viewModel, sym, currentGoldPrice, currentSilverPrice)
        }

        // Live Rates Cards Ticker Grid
        item {
            Text(
                text = "Live Precious Metals Spots (1 oz)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PlatinumWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gold card
                SpotRateLongCard(
                    metalName = "Gold Bullion (999.9)",
                    symbol = "Au",
                    currentPriceStr = "$sym${String.format("%,.2f", currentGoldPrice)}",
                    percentageChange = 0.42,
                    spreadStr = "$sym${String.format("%.2f", currentGoldPrice * 0.005)}",
                    accentColor = AurumGold,
                    indicatorPoints = listOf(0.4f, 0.45f, 0.42f, 0.48f, 0.52f, 0.5f, 0.55f, 0.58f),
                    testTag = "gold_spot_card"
                )

                // Silver card
                SpotRateLongCard(
                    metalName = "Silver Bullion (999.0)",
                    symbol = "Ag",
                    currentPriceStr = "$sym${String.format("%,.2f", currentSilverPrice)}",
                    percentageChange = -0.74,
                    spreadStr = "$sym${String.format("%.2f", currentSilverPrice * 0.015)}",
                    accentColor = SterlingSilver,
                    indicatorPoints = listOf(0.6f, 0.58f, 0.59f, 0.52f, 0.48f, 0.5f, 0.44f, 0.42f),
                    testTag = "silver_spot_card"
                )

                // Platinum Card
                SpotRateLongCard(
                    metalName = "Platinum Bullion (999.5)",
                    symbol = "Pt",
                    currentPriceStr = "$sym${String.format("%,.2f", currentPlatinumPrice)}",
                    percentageChange = 0.15,
                    spreadStr = "$sym${String.format("%.2f", currentPlatinumPrice * 0.012)}",
                    accentColor = PlatinumWhite,
                    indicatorPoints = listOf(0.3f, 0.32f, 0.35f, 0.31f, 0.34f, 0.38f, 0.36f, 0.39f),
                    testTag = "platinum_spot_card"
                )
            }
        }

        // Live Market Historical Chart
        item {
            HistoricalChartCard(sym)
        }

        // Market News & Educational Indicators
        item {
            MarketIntelligenceSection(viewModel)
        }
    }
}

@Composable
fun WelcomeBanner(viewModel: AurumViewModel, sym: String, gold: Double, silver: Double) {
    val user by viewModel.userAccount.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    
    // Calculates total dynamic valuation
    val totalWealth = holdings.sumOf { holding ->
        val spotPerGram = viewModel.repository.getSpotPricePerGram(holding.metalType, viewModel.selectedCurrency.value)
        val metalBaseValue = spotPerGram * holding.weightGrams
        metalBaseValue
    }

    val availableFiat = when(viewModel.selectedCurrency.value) {
        "GBP" -> user.balanceGBP
        "EUR" -> user.balanceEUR
        else -> user.balanceUSD
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateGraphite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SteelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good day,",
                        style = MaterialTheme.typography.bodySmall,
                        color = SterlingSilver
                    )
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PlatinumWhite
                    )
                }
                
                // Quick verified indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (user.isKycVerified) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFC62828).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (user.isKycVerified) Color(0xFF4CAF50) else Color(0xFFF44336))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isKycVerified) "KYC Verified" else "KYC Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.isKycVerified) Color(0xFF81C784) else Color(0xFFE57373)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Divider(color = SteelCardBorder)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL BULLION CAPITAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = AurumGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$sym${String.format("%,.2f", totalWealth)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = PlatinumWhite
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AVAILABLE BUYING POWER",
                        style = MaterialTheme.typography.labelSmall,
                        color = SterlingSilver,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$sym${String.format("%,.2f", availableFiat)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                }
            }
        }
    }
}

@Composable
fun SpotRateLongCard(
    metalName: String,
    symbol: String,
    currentPriceStr: String,
    percentageChange: Double,
    spreadStr: String,
    accentColor: Color,
    indicatorPoints: List<Float>,
    testTag: String
) {
    var isFluctuating by remember { mutableStateOf(false) }
    // triggers micro animation effect on price updates
    LaunchedEffect(currentPriceStr) {
        isFluctuating = true
        kotlinx.coroutines.delay(350)
        isFluctuating = false
    }

    val glowAlpha by animateFloatAsState(
        targetValue = if (isFluctuating) 0.15f else 0f,
        animationSpec = tween(300),
        label = "glow"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isFluctuating) accentColor.copy(alpha = 0.5f) else SteelCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .drawBehind {
                if (glowAlpha > 0f) {
                    drawRoundRect(
                        color = accentColor,
                        alpha = glowAlpha,
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chemical Abbreviation Box with premium circle profile
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column {
                    Text(
                        text = metalName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PlatinumWhite
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Spread: $spreadStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = SterlingSilver
                        )
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SteelCardBorder))
                        Text(
                            text = "Live Vaulted Spot",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF81C784)
                        )
                    }
                }
            }

            // Price ticker & miniature line chart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mini wave chart
                Canvas(
                    modifier = Modifier
                        .width(60.dp)
                        .height(28.dp)
                ) {
                    val path = Path()
                    val widthStep = size.width / (indicatorPoints.size - 1)
                    indicatorPoints.forEachIndexed { idx, value ->
                        val cx = idx * widthStep
                        val cy = size.height - (value * size.height)
                        if (idx == 0) {
                            path.moveTo(cx, cy)
                        } else {
                            path.lineTo(cx, cy)
                        }
                    }
                    drawPath(
                        path = path,
                        color = if (percentageChange >= 0) Color(0xFF81C784) else Color(0xFFE57373),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }

                // Price & percentage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentPriceStr,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFluctuating) accentColor else PlatinumWhite
                    )
                    
                    val pctColor = if (percentageChange >= 0) Color(0xFF81C784) else Color(0xFFE57373)
                    val sign = if (percentageChange >= 0) "+" else ""

                    Text(
                        text = "$sign${percentageChange}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pctColor
                    )
                }
            }
        }
    }
}

@Composable
fun HistoricalChartCard(currencySymbol: String) {
    var selectedRange by remember { mutableStateOf("1M") }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateGraphite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SteelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMMODITY GOLD SPOT INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = AurumGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Historical Metal Performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = SterlingSilver
                    )
                }

                // Range Selector Bar
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(2.dp)
                ) {
                    listOf("1D", "1W", "1M", "1Y").forEach { range ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedRange == range) SteelCardBorder else Color.Transparent)
                                .clickable { selectedRange = range }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = range,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedRange == range) AurumGold else SterlingSilver,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful Canvas line chart
            val chartPoints = when(selectedRange) {
                "1D" -> listOf(0.2f, 0.25f, 0.22f, 0.35f, 0.3f, 0.45f, 0.41f, 0.52f, 0.58f, 0.54f, 0.62f, 0.6f)
                "1W" -> listOf(0.7f, 0.65f, 0.68f, 0.59f, 0.52f, 0.48f, 0.55f, 0.51f, 0.58f, 0.65f, 0.72f, 0.75f)
                "1M" -> listOf(0.3f, 0.35f, 0.28f, 0.34f, 0.42f, 0.45f, 0.52f, 0.59f, 0.55f, 0.61f, 0.68f, 0.76f)
                else -> listOf(0.1f, 0.15f, 0.21f, 0.26f, 0.38f, 0.44f, 0.51f, 0.41f, 0.48f, 0.62f, 0.85f, 0.92f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val widthStep = size.width / (chartPoints.size - 1)
                    val path = Path()
                    val bgPath = Path()
                    
                    chartPoints.forEachIndexed { idx, point ->
                        val cx = idx * widthStep
                        val cy = size.height - (point * (size.height - 20f)) - 10f
                        
                        if (idx == 0) {
                            path.moveTo(cx, cy)
                            bgPath.moveTo(cx, size.height)
                            bgPath.lineTo(cx, cy)
                        } else {
                            path.lineTo(cx, cy)
                            bgPath.lineTo(cx, cy)
                        }
                        
                        if (idx == chartPoints.size - 1) {
                            bgPath.lineTo(cx, size.height)
                            bgPath.close()
                        }
                    }

                    // Draw gradient background under the path
                    drawPath(
                        path = bgPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(AurumGold.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Draw primary line
                    drawPath(
                        path = path,
                        color = AurumGold,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Draw grids
                    val gridLines = 4
                    val spacing = size.height / gridLines
                    for (i in 1 until gridLines) {
                        drawLine(
                            color = SteelCardBorder.copy(alpha = 0.4f),
                            start = Offset(0f, i * spacing),
                            end = Offset(size.width, i * spacing),
                            strokeWidth = 1f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spot Base: ${currencySymbol}1,840.00 / oz",
                    style = MaterialTheme.typography.labelSmall,
                    color = SterlingSilver
                )
                Text(
                    text = "High: ${currencySymbol}1,925.50 | Low: ${currencySymbol}1,811.20",
                    style = MaterialTheme.typography.labelSmall,
                    color = SterlingSilver
                )
            }
        }
    }
}

@Composable
fun MarketIntelligenceSection(viewModel: AurumViewModel) {
    Text(
        text = "Market Intelligence Centre",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = PlatinumWhite,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SteelCardBorder),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = AurumGold)
                Text(
                    text = "Economic Overview: Inflation & Reservists",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PlatinumWhite
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Central reserve sovereign acquisitions surged 14% this quarter, demonstrating robust physical security demands. Gold and Silver coins offer complete capital preservation frameworks during hyper-inflation loops.",
                style = MaterialTheme.typography.bodySmall,
                color = SterlingSilver,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Action: redirect to wealth helper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SteelCardBorder)
                    .clickable { viewModel.selectTab("AI") }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AurumGold, modifier = Modifier.size(16.dp))
                    Text(text = "Consult AI Wealth Assistant", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AurumLightGold)
                }
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = AurumGold, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ==================== MARKETPLACE SCREEN ====================

@Composable
fun MarketScreen(viewModel: AurumViewModel) {
    val filter by viewModel.marketFilter.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()

    val filteredProducts = remember(filter) {
        if (filter == "ALL") viewModel.repository.products
        else viewModel.repository.products.filter { it.metalType.uppercase() == filter || it.category.uppercase() == filter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter tabs horizontal row
        val filters = listOf("ALL", "GOLD", "SILVER", "PLATINUM", "COINS", "BARS")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(filters) { item ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (filter == item) AurumGold else SteelCardBorder)
                        .border(1.dp, if (filter == item) Color.Transparent else SteelCardBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setMarketFilter(item) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (filter == item) ObsidianCharcoal else SterlingSilver
                    )
                }
            }
        }

        Divider(color = SteelCardBorder, modifier = Modifier.padding(bottom = 16.dp))

        // Product specification grid/list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredProducts) { product ->
                val pricing = viewModel.repository.calculateProductPricing(product, currency)
                val sym = when(currency) {
                    "GBP" -> "£"
                    "EUR" -> "€"
                    else -> "$"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, SteelCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.viewProduct(product) }
                        .testTag("product_card_${product.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Custom Adaptive Emblem
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (product.metalType) {
                                            "GOLD" -> AurumGold.copy(alpha = 0.12f)
                                            "SILVER" -> SterlingSilver.copy(alpha = 0.12f)
                                            else -> PlatinumWhite.copy(alpha = 0.12f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when (product.metalType) {
                                            "GOLD" -> AurumGold.copy(alpha = 0.4f)
                                            "SILVER" -> SterlingSilver.copy(alpha = 0.4f)
                                            else -> PlatinumWhite.copy(alpha = 0.4f)
                                        },
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (product.category == "Coins" || product.category == "Sovereigns") Icons.Default.GeneratingTokens else Icons.Default.ViewInAr,
                                    contentDescription = null,
                                    tint = when (product.metalType) {
                                        "GOLD" -> AurumGold
                                        "SILVER" -> SterlingSilver
                                        else -> PlatinumWhite
                                    }
                                )
                            }

                            Column {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PlatinumWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${product.manufacturer} • ${product.weightGrams}g • ${product.purity} Pure",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SterlingSilver
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Premium tag
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AurumGold.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "+${product.premiumPercent}% Spot Premium",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AurumGold
                                        )
                                    }
                                }
                            }
                        }

                        // Price and Action indicators
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$sym${String.format("%,.2f", pricing.totalCost)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black,
                                color = PlatinumWhite
                            )
                            Text(
                                text = "Est: ${product.deliveryEstimate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SterlingSilver,
                                fontSize = 8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AurumGold)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SECURE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianCharcoal
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog Overlay
    selectedProduct?.let { product ->
        ProductDetailDialog(viewModel = viewModel, product = product, onDismiss = { viewModel.viewProduct(null) })
    }
}

@Composable
fun ProductDetailDialog(viewModel: AurumViewModel, product: ProductSpec, onDismiss: () -> Unit) {
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val pricing = viewModel.repository.calculateProductPricing(product, currency)
    var quantity by remember { mutableStateOf(1) }
    var destination by remember { mutableStateOf("VAULT") } // "VAULT" or "DELIVERED"
    var buyError by remember { mutableStateOf<String?>(null) }
    var buySuccess by remember { mutableStateOf(false) }

    val sym = when(currency) {
        "GBP" -> "£"
        "EUR" -> "€"
        else -> "$"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGraphite),
            border = BorderStroke(1.dp, SteelCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AurumGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = AurumGold)
                        }
                        Column {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PlatinumWhite
                            )
                            Text(
                                text = "${product.manufacturer} Bullion",
                                style = MaterialTheme.typography.labelSmall,
                                color = SterlingSilver
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = SterlingSilver)
                    }
                }

                Divider(color = SteelCardBorder)

                if (buySuccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(44.dp))
                            Text("Vault Allocation Complete", fontWeight = FontWeight.Bold, color = PlatinumWhite)
                            Text("Your physical asset has been locked in segregation and signed into the ledger securely.", textAlign = TextAlign.Center, color = SterlingSilver, fontSize = 11.sp)
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = AurumGold)
                            ) {
                                Text("Done", color = ObsidianCharcoal)
                            }
                        }
                    }
                } else {
                    // Cost breakdowns
                    Text(text = "ACQUISITION COST TRANSPARENCY", style = MaterialTheme.typography.labelSmall, color = AurumGold, fontWeight = FontWeight.Bold)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, SteelCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row 1: spot rate per oz
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dynamic Ounce Spot", color = SterlingSilver, fontSize = 12.sp)
                                Text("$sym${String.format("%,.2f", pricing.spotOuncePrice)}", color = PlatinumWhite, fontSize = 12.sp)
                            }
                            // Row 2: calculated metal value
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Raw Metal Value (${product.weightGrams}g)", color = SterlingSilver, fontSize = 12.sp)
                                Text("$sym${String.format("%,.2f", pricing.metalValue)}", color = PlatinumWhite, fontSize = 12.sp)
                            }
                            // Row 3: dealer premium
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Premium (Dealer MarkUp ${product.premiumPercent}%)", color = SterlingSilver, fontSize = 12.sp)
                                Text("$sym${String.format("%,.2f", pricing.premiumValue)}", color = AurumGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Divider(color = SteelCardBorder)

                            // Row 4: Total price per item
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Item Lock-In Price", color = PlatinumWhite, fontWeight = FontWeight.Bold)
                                Text("$sym${String.format("%,.2f", pricing.totalCost)}", color = PlatinumWhite, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // Specifications
                    Text(text = "BULLION SPECIFICATIONS", style = MaterialTheme.typography.labelSmall, color = AurumGold, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpecRow("Weight (Grams)", "${product.weightGrams}g")
                        SpecRow("Purity Index", product.purity)
                        SpecRow("Refiner/Mint", product.manufacturer)
                        SpecRow("Vault storage Cost", "0.12% per annum (segregated)")
                        SpecRow("Delivery Insurance", "Fully protected by Lloyds")
                    }

                    Divider(color = SteelCardBorder)

                    // Choose destination
                    Text(text = "FULFILLMENT OPTION", style = MaterialTheme.typography.labelSmall, color = AurumGold, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (destination == "VAULT") SteelCardBorder else Color.Transparent)
                                .border(1.dp, if (destination == "VAULT") AurumGold else SteelCardBorder, RoundedCornerShape(8.dp))
                                .clickable { destination = "VAULT" }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = if (destination == "VAULT") AurumGold else SterlingSilver)
                                Text("Vault Storage", color = if (destination == "VAULT") AurumGold else PlatinumWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Insured & Segregated", color = SterlingSilver, fontSize = 9.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (destination == "DELIVERED") SteelCardBorder else Color.Transparent)
                                .border(1.dp, if (destination == "DELIVERED") AurumGold else SteelCardBorder, RoundedCornerShape(8.dp))
                                .clickable { destination = "DELIVERED" }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = if (destination == "DELIVERED") AurumGold else SterlingSilver)
                                Text("Physical Delivery", color = if (destination == "DELIVERED") AurumGold else PlatinumWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Discrete & Signed", color = SterlingSilver, fontSize = 9.sp)
                            }
                        }
                    }

                    // Quantity increment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Acquisition Units:", color = PlatinumWhite, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.background(SteelCardBorder, CircleShape)) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = null, tint = PlatinumWhite)
                            }
                            Text(text = quantity.toString(), color = PlatinumWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            IconButton(onClick = { quantity++ }, modifier = Modifier.background(SteelCardBorder, CircleShape)) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = PlatinumWhite)
                            }
                        }
                    }

                    // Total Transaction Surcharges
                    val finalTotal = pricing.totalCost * quantity
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ESTIMATED LOCK-IN CHARGE", color = SterlingSilver, fontSize = 10.sp)
                        Text("$sym${String.format("%,.2f", finalTotal)}", color = AurumGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }

                    buyError?.let { err ->
                        Text(text = err, color = Color(0xFFE57373), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Action buttons
                    Button(
                        onClick = {
                            viewModel.executeBuy(
                                product = product,
                                quantity = quantity.toDouble(),
                                destination = destination,
                                onSuccess = {
                                    buySuccess = true
                                    buyError = null
                                },
                                onError = { msg ->
                                    buyError = msg
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AurumGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("execute_buy_button")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ObsidianCharcoal)
                            Text("SECURE ACQUISITION LOCK-IN", fontWeight = FontWeight.Black, color = ObsidianCharcoal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = SterlingSilver, fontSize = 12.sp)
        Text(text = value, color = PlatinumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ==================== PORTFOLIO SCREEN ====================

@Composable
fun PortfolioScreen(viewModel: AurumViewModel) {
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    var showResellDialog by remember { mutableStateOf<HoldingEntity?>(null) }

    val sym = when(currency) {
        "GBP" -> "£"
        "EUR" -> "€"
        else -> "$"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio Asset Ratios Visuals
        item {
            PortfolioSplitCard(viewModel, holdings)
        }

        // Active holding details
        item {
            Text(
                text = "Account Vault Holdings Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PlatinumWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (holdings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Inbox, contentDescription = null, tint = SteelCardBorder, modifier = Modifier.size(48.dp))
                        Text("No active precious metals currently held.", color = SterlingSilver, fontSize = 12.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    holdings.forEach { holding ->
                        val spotPerGram = viewModel.repository.getSpotPricePerGram(holding.metalType, currency)
                        val holdingValuation = spotPerGram * holding.weightGrams

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, SteelCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (holding.metalType) {
                                                        "GOLD" -> AurumGold.copy(alpha = 0.15f)
                                                        "SILVER" -> SterlingSilver.copy(alpha = 0.15f)
                                                        else -> PlatinumWhite.copy(alpha = 0.15f)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = holding.metalType.take(2),
                                                color = when (holding.metalType) {
                                                    "GOLD" -> AurumGold
                                                    "SILVER" -> SterlingSilver
                                                    else -> PlatinumWhite
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = holding.productName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PlatinumWhite
                                            )
                                            Text(
                                                text = "Lock-In: ${holding.weightGrams}g • Status: ${holding.status}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SterlingSilver
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$sym${String.format("%,.2f", holdingValuation)}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Black,
                                            color = PlatinumWhite
                                        )
                                        Text(
                                            text = "${holding.quantity} Unit(s)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SterlingSilver
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Sell action
                                    if (holding.status == "VAULTED") {
                                        Button(
                                            onClick = { showResellDialog = holding },
                                            colors = ButtonDefaults.buttonColors(containerColor = SteelCardBorder),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Sell, contentDescription = null, tint = AurumGold, modifier = Modifier.size(12.dp))
                                                Text("Sell Instantly", color = AurumGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.requestPhysicalDelivery(
                                                    holding,
                                                    onSuccess = {},
                                                    onError = {}
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SteelCardBorder),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = SterlingSilver, modifier = Modifier.size(12.dp))
                                                Text("Deliver Home", color = PlatinumWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DarkBackground)
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Delivered & Safely Stored in Private Address", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transactions Audit Ledger
        item {
            Text(
                text = "Ledger Audit Trails",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PlatinumWhite,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGraphite),
                border = BorderStroke(1.dp, SteelCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (transactions.isEmpty()) {
                        Text("No audit logs present.", color = SterlingSilver, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            transactions.take(6).forEach { tx ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val txCol = when (tx.type) {
                                            "BUY" -> Color(0xFF81C784)
                                            "SELL" -> AurumGold
                                            "DEPOSIT" -> Color(0xFF64B5F6)
                                            else -> SterlingSilver
                                        }
                                        val txIcon = when(tx.type) {
                                            "BUY" -> Icons.Default.ArrowUpward
                                            "SELL" -> Icons.Default.ArrowDownward
                                            "DEPOSIT" -> Icons.Default.AddCircleOutline
                                            else -> Icons.Default.LocalShipping
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(txCol.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = txIcon, contentDescription = null, tint = txCol, modifier = Modifier.size(14.dp))
                                        }

                                        Column {
                                            Text(text = tx.productName, color = PlatinumWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(text = "Audit Status: ${tx.status}", color = SterlingSilver, fontSize = 9.sp)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        val prefix = if (tx.type == "SELL") "+" else if (tx.type == "BUY") "-" else ""
                                        Text(
                                            text = "$prefix$sym${String.format("%,.2f", tx.totalCost)}",
                                            color = if (tx.type == "SELL" || tx.type == "DEPOSIT") Color(0xFF81C784) else PlatinumWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(text = if (tx.type == "BUY") "${tx.quantity} unit(s)" else "Instant settle", color = SterlingSilver, fontSize = 9.sp)
                                    }
                                }
                                Divider(color = SteelCardBorder.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Sell back Quote confirmation overlay
    showResellDialog?.let { holding ->
        SellbackQuoteDialog(viewModel = viewModel, holding = holding, onDismiss = { showResellDialog = null })
    }
}

@Composable
fun PortfolioSplitCard(viewModel: AurumViewModel, holdings: List<HoldingEntity>) {
    val totalWealth = holdings.sumOf { holding ->
        val spotPerGram = viewModel.repository.getSpotPricePerGram(holding.metalType, viewModel.selectedCurrency.value)
        spotPerGram * holding.weightGrams
    }

    val goldVal = holdings.filter { it.metalType == "GOLD" }.sumOf {
        viewModel.repository.getSpotPricePerGram("GOLD", viewModel.selectedCurrency.value) * it.weightGrams
    }
    val silverVal = holdings.filter { it.metalType == "SILVER" }.sumOf {
        viewModel.repository.getSpotPricePerGram("SILVER", viewModel.selectedCurrency.value) * it.weightGrams
    }
    val platVal = holdings.filter { it.metalType == "PLATINUM" }.sumOf {
        viewModel.repository.getSpotPricePerGram("PLATINUM", viewModel.selectedCurrency.value) * it.weightGrams
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateGraphite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SteelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PORTFOLIO METAL ALLOCATION",
                style = MaterialTheme.typography.labelSmall,
                color = AurumGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Allocation Chart Canvas (stunning Canvas doughnut chart representation)
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val goldShare = if (totalWealth > 0) (goldVal / totalWealth).toFloat() else 0f
                        val silverShare = if (totalWealth > 0) (silverVal / totalWealth).toFloat() else 0f
                        val platShare = if (totalWealth > 0) (platVal / totalWealth).toFloat() else 0f
                        
                        // draw background circle
                        drawCircle(color = SteelCardBorder, style = Stroke(width = 12.dp.toPx()))

                        var currentAngle = -90f
                        if (totalWealth > 0) {
                            // Gold Arc
                            drawArc(
                                color = AurumGold,
                                startAngle = currentAngle,
                                sweepAngle = goldShare * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx())
                            )
                            currentAngle += goldShare * 360f

                            // Silver Arc
                            drawArc(
                                color = SterlingSilver,
                                startAngle = currentAngle,
                                sweepAngle = silverShare * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx())
                            )
                            currentAngle += silverShare * 360f

                            // Platinum Arc
                            drawArc(
                                color = PlatinumWhite,
                                startAngle = currentAngle,
                                sweepAngle = platShare * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx())
                            )
                        } else {
                            // default empty outline
                            drawCircle(color = SteelCardBorder.copy(alpha = 0.5f), style = Stroke(width = 12.dp.toPx()))
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalWealth > 0) "${((goldVal / totalWealth)*100).toInt()}%" else "0%",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = AurumGold
                        )
                        Text(text = "Gold Ratio", fontSize = 7.sp, color = SterlingSilver)
                    }
                }

                // Breakdown list
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    MetalRatioLabel("Physical Gold", goldVal, totalWealth, AurumGold, viewModel.selectedCurrency.value)
                    MetalRatioLabel("Physical Silver", silverVal, totalWealth, SterlingSilver, viewModel.selectedCurrency.value)
                    MetalRatioLabel("Platinum Bullion", platVal, totalWealth, PlatinumWhite, viewModel.selectedCurrency.value)
                }
            }
        }
    }
}

@Composable
fun MetalRatioLabel(label: String, valRaw: Double, total: Double, colour: Color, currSymbol: String) {
    val pct = if (total > 0) (valRaw / total) * 100 else 0.0
    val sym = when(currSymbol) {
        "GBP" -> "£"
        "EUR" -> "€"
        else -> "$"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colour))
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = label, fontWeight = FontWeight.Bold, color = PlatinumWhite, fontSize = 11.sp)
                Text(text = "${pct.toInt()}%", color = colour, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Text(text = "$sym${String.format("%,.2f", valRaw)}", color = SterlingSilver, fontSize = 9.sp)
        }
    }
}

@Composable
fun SellbackQuoteDialog(viewModel: AurumViewModel, holding: HoldingEntity, onDismiss: () -> Unit) {
    var quantitySellInput by remember { mutableStateOf(holding.quantity.toString()) }
    var sellError by remember { mutableStateOf<String?>(null) }
    var sellSuccess by remember { mutableStateOf(false) }
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    val sym = when(currency) {
        "GBP" -> "£"
        "EUR" -> "€"
        else -> "$"
    }

    val parseQty = quantitySellInput.toDoubleOrNull() ?: 0.0
    val spotPerGram = viewModel.repository.getSpotPricePerGram(holding.metalType, currency)
    val baseUnitPrice = spotPerGram * (holding.weightGrams / holding.quantity)
    val sellBackUnitPrice = baseUnitPrice * 0.975 // 2.5% spread markdown
    val totalQuoteCredit = sellBackUnitPrice * parseQty

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGraphite),
            border = BorderStroke(1.dp, SteelCardBorder),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Instant Resell Quote", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AurumGold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = SterlingSilver)
                    }
                }

                Divider(color = SteelCardBorder)

                if (sellSuccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(36.dp))
                            Text("Settle Confirmation Success", fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                            Text("The asset has been liquidated from storage and cash credited to your fiat balance instantly.", fontSize = 11.sp, color = PlatinumWhite, textAlign = TextAlign.Center)
                            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AurumGold)) {
                                Text("Acknowledge", color = ObsidianCharcoal)
                            }
                        }
                    }
                } else {
                    Text("Liquidity spread: BID price is locked at 97.5% of live spot representing secure internal merchant liquidity transfer.", color = SterlingSilver, fontSize = 10.sp)

                    Column {
                        Text("Asset Selected", color = SterlingSilver, fontSize = 11.sp)
                        Text(holding.productName, color = PlatinumWhite, fontWeight = FontWeight.Bold)
                        Text("Available to sell: ${holding.quantity} Unit(s)", color = AurumGold, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = quantitySellInput,
                        onValueChange = { quantitySellInput = it },
                        label = { Text("Resell Units") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AurumGold,
                            unfocusedBorderColor = SteelCardBorder,
                            focusedTextColor = PlatinumWhite,
                            unfocusedTextColor = PlatinumWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, SteelCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Est. Live Spot Per Unit", color = SterlingSilver, fontSize = 12.sp)
                                Text("$sym${String.format("%,.2f", baseUnitPrice)}", color = PlatinumWhite, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Liquid Bid (97.5% Spreads)", color = SterlingSilver, fontSize = 12.sp)
                                Text("$sym${String.format("%,.2f", sellBackUnitPrice)}", color = AurumGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = SteelCardBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Liquidation Proceeds", color = PlatinumWhite, fontWeight = FontWeight.Bold)
                                Text("$sym${String.format("%,.2f", totalQuoteCredit)}", color = Color(0xFF81C784), fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    sellError?.let { err ->
                        Text(text = err, color = Color(0xFFE57373), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (parseQty <= 0.0 || parseQty > holding.quantity) {
                                sellError = "Invalid quantity specified."
                            } else {
                                viewModel.executeSell(
                                    holding = holding,
                                    quantity = parseQty,
                                    onSuccess = {
                                        sellSuccess = true
                                        sellError = null
                                    },
                                    onError = { msg ->
                                        sellError = msg
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AurumGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AUTHENTICATE INSTANT SETTLEMENT", fontWeight = FontWeight.Bold, color = ObsidianCharcoal)
                    }
                }
            }
        }
    }
}

// ==================== VAULTS & SAVINGS PLANS ====================

@Composable
fun VaultsScreen(viewModel: AurumViewModel) {
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val plans by viewModel.autoInvestPlans.collectAsStateWithLifecycle()
    
    // Auto invest inputs
    var showPlanMaker by remember { mutableStateOf(false) }
    var depositDialog by remember { mutableStateOf(false) }

    val sym = when(currency) {
        "GBP" -> "£"
        "EUR" -> "€"
        else -> "$"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vault Secure Services Header Block
        item {
            VaultServicesHeaderCard { depositDialog = true }
        }

        // Vault Certificates Section
        item {
            Text(
                text = "Metal Custodian Ownership Certificates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PlatinumWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val vaultedHoldings = holdings.filter { it.status == "VAULTED" }
            if (vaultedHoldings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No segregated vaulted metal assets present.", color = SterlingSilver, fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vaultedHoldings.forEach { hold ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, SteelCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "CERTIFICATE ID: ${hold.certificateId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AurumGold,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = hold.productName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PlatinumWhite
                                        )
                                        Text(
                                            text = "Custodial Facility: ${hold.location}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SterlingSilver
                                        )
                                    }

                                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = AurumGold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                // Cert detail spec
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SteelCardBorder)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Allocation: Segregated, Insured", fontSize = 9.sp, color = SterlingSilver)
                                    Text(text = "Weight: ${hold.weightGrams}g @ ${hold.purity} Index", fontSize = 9.sp, color = PlatinumWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Auto Invest (Precious metals savings plans)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bullion Auto-Invest Plans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PlatinumWhite
                )

                Button(
                    onClick = { showPlanMaker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AurumGold),
                    modifier = Modifier.testTag("add_plan_button")
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = ObsidianCharcoal)
                        Text("Create", color = ObsidianCharcoal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (plans.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, SteelCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = SteelCardBorder, modifier = Modifier.size(36.dp))
                        Text("No repeating auto-invest allocations active.", color = SterlingSilver, fontSize = 11.sp)
                        Text("Build custom strategies (e.g. 70% Gold / 30% Silver) to acquire bullion dynamically on a weekly or monthly interval automatically.", color = SterlingSilver, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(plans) { plan ->
                var planResultLog by remember { mutableStateOf<String?>(null) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, SteelCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = plan.name, fontWeight = FontWeight.Bold, color = PlatinumWhite)
                                Text(text = "Strategy: ${plan.goldRatio}% Gold • ${plan.silverRatio}% Silver • ${plan.platinumRatio}% Platinum", style = MaterialTheme.typography.labelSmall, color = AurumGold)
                            }
                            
                            IconButton(onClick = { viewModel.removeSavingsPlan(plan) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE57373))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Plan Amount", style = MaterialTheme.typography.labelSmall, color = SterlingSilver)
                                Text(text = "$sym${String.format("%,.0f", plan.amount)} / ${plan.interval}", fontWeight = FontWeight.Black, color = PlatinumWhite, fontSize = 16.sp)
                            }

                            // Manual Trigger Button for Prototype testing simulation
                            Button(
                                onClick = {
                                    viewModel.triggerAutoInvestPlanManual(plan) { ok ->
                                        planResultLog = if (ok) "Successfully executed automatic purchase strategy." else "Insufficient capital to execute buyback plan."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SteelCardBorder)
                            ) {
                                Text("Execute Plan Now", color = AurumGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        planResultLog?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = msg, color = if (msg.contains("Success")) Color(0xFF81C784) else Color(0xFFE57373), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Interactive Plan Maker Overlay
    if (showPlanMaker) {
        PlanMakerDialog(viewModel, onDismiss = { showPlanMaker = false })
    }

    // Direct Banking Fund Overlay
    if (depositDialog) {
        DepositFundDialog(viewModel, onDismiss = { depositDialog = false })
    }
}

@Composable
fun VaultServicesHeaderCard(onFundClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateGraphite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SteelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURE CUSTODIAL VAULT",
                        style = MaterialTheme.typography.labelSmall,
                        color = AurumGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Allocated Segregated Depository",
                        style = MaterialTheme.typography.bodySmall,
                        color = SterlingSilver
                    )
                }

                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AurumGold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your assets are safely deposited with multi-jurisdictional vault support. Physical bullion coins and bars are stored segregating unique registration certificates instantly available.",
                fontSize = 11.sp,
                color = SterlingSilver,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onFundClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AurumGold),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = ObsidianCharcoal, modifier = Modifier.size(16.dp))
                        Text("Fund Local Cash", color = ObsidianCharcoal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanMakerDialog(viewModel: AurumViewModel, onDismiss: () -> Unit) {
    var pName by remember { mutableStateOf("Precious Metals Auto Reserve") }
    var pAmt by remember { mutableStateOf("150") }
    var intervalSelection by remember { mutableStateOf("MONTHLY") }
    var gRatio by remember { mutableStateOf(70) }
    var sRatio by remember { mutableStateOf(30) }
    
    val currSymbol = viewModel.selectedCurrency.value

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGraphite),
            border = BorderStroke(1.dp, SteelCardBorder),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Build Auto-Invest Strategy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AurumGold)
                
                OutlinedTextField(
                    value = pName,
                    onValueChange = { pName = it },
                    label = { Text("Plan Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AurumGold,
                        unfocusedBorderColor = SteelCardBorder,
                        focusedTextColor = PlatinumWhite,
                        unfocusedTextColor = PlatinumWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pAmt,
                    onValueChange = { pAmt = it },
                    label = { Text("Recurring Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AurumGold,
                        unfocusedBorderColor = SteelCardBorder,
                        focusedTextColor = PlatinumWhite,
                        unfocusedTextColor = PlatinumWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Interval Selection
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("WEEKLY", "MONTHLY").forEach { tm ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (intervalSelection == tm) SteelCardBorder else Color.Transparent)
                                .border(1.dp, if (intervalSelection == tm) AurumGold else SteelCardBorder, RoundedCornerShape(8.dp))
                                .clickable { intervalSelection = tm }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = tm, color = if (intervalSelection == tm) AurumGold else SterlingSilver, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Strategy mix allocation
                Text("ALLOCATION STRATEGY SPLIT", style = MaterialTheme.typography.labelSmall, color = AurumGold, fontWeight = FontWeight.Bold)
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gold Ratio Target: $gRatio%", color = PlatinumWhite, fontSize = 12.sp)
                    Slider(
                        value = gRatio.toFloat(),
                        onValueChange = { 
                            gRatio = it.toInt()
                            sRatio = 100 - gRatio
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = AurumGold, activeTrackColor = AurumGold)
                    )

                    Text("Silver Ratio Target: $sRatio%", color = PlatinumWhite, fontSize = 12.sp)
                    Slider(
                        value = sRatio.toFloat(),
                        onValueChange = {
                            sRatio = it.toInt()
                            gRatio = 100 - sRatio
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = SterlingSilver, activeTrackColor = SterlingSilver)
                    )
                }

                Button(
                    onClick = {
                        val parsedAmt = pAmt.toDoubleOrNull() ?: 100.0
                        viewModel.addSavingsPlan(
                            name = pName,
                            amount = parsedAmt,
                            currency = currSymbol,
                            interval = intervalSelection,
                            gPct = gRatio,
                            sPct = sRatio,
                            pPct = 0
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AurumGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("COMMENCE WEALTH STRATEGY", fontWeight = FontWeight.Bold, color = ObsidianCharcoal)
                }
            }
        }
    }
}

@Composable
fun DepositFundDialog(viewModel: AurumViewModel, onDismiss: () -> Unit) {
    var amt by remember { mutableStateOf("1000") }
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGraphite),
            border = BorderStroke(1.dp, SteelCardBorder),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Open Banking Wire Transfer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AurumGold)
                Text("Inject dummy liquidity capital to buy, sell, or schedule auto-purchases safely in sandbox.", color = SterlingSilver, fontSize = 11.sp)

                OutlinedTextField(
                    value = amt,
                    onValueChange = { amt = it },
                    label = { Text("Transfer Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AurumGold,
                        unfocusedBorderColor = SteelCardBorder,
                        focusedTextColor = PlatinumWhite,
                        unfocusedTextColor = PlatinumWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val parsed = amt.toDoubleOrNull() ?: 0.0
                            viewModel.fundAccount(parsed, currency, true)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AurumGold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("DEPOSIT", color = ObsidianCharcoal, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val parsed = amt.toDoubleOrNull() ?: 0.0
                            viewModel.fundAccount(parsed, currency, false)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SteelCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WITHDRAWAL", color = PlatinumWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== AI ASSISTANT CHAT SCREEN ====================

@Composable
fun AiAssistantScreen(viewModel: AurumViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val assistantState by viewModel.assistantState.collectAsStateWithLifecycle()

    var messageInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberScrollState()

    // auto-scroll on updates
    LaunchedEffect(chatHistory.size) {
        listState.animateScrollTo(listState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Floating Top Assistant Status Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SlateGraphite)
                .border(1.dp, SteelCardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AurumGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AurumGold, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("AURUM Wealth AI Advisor", fontWeight = FontWeight.Bold, color = PlatinumWhite, fontSize = 12.sp)
                    Text("Secure Cryptographic Terminal Channel", color = Color(0xFF81C784), fontSize = 9.sp)
                }
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "clear chat", tint = SterlingSilver)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat conversation flow list
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(listState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            chatHistory.forEach { msg ->
                val isUsr = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUsr) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUsr) 12.dp else 0.dp,
                                    bottomEnd = if (isUsr) 0.dp else 12.dp
                                )
                            )
                            .background(if (isUsr) AurumGold.copy(alpha = 0.15f) else DarkSurface)
                            .border(
                                1.dp,
                                if (isUsr) AurumGold.copy(alpha = 0.3f) else SteelCardBorder,
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUsr) 12.dp else 0.dp,
                                    bottomEnd = if (isUsr) 0.dp else 12.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isUsr) "Tom (Sterling Portfolio Owner)" else "AURUM Wealth Partner AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUsr) AurumGold else SterlingSilver,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PlatinumWhite,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            if (assistantState is AssistantChatUiState.Loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .padding(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AurumGold, strokeWidth = 2.dp)
                            Text("Consulting Gold Spot Indices & Ledger Security...", color = SterlingSilver, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Suggestion Chips Horizontal Bar
        val prompts = listOf(
            "What is a gold sovereign?",
            "Why does silver have higher volatility?",
            "How much gold would £500/month purchase in 10 years?",
            "What would happen if silver rises 20%?"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(prompts) { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SteelCardBorder)
                        .clickable { viewModel.sendAssistantMessage(text) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = text, color = AurumGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field text row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                placeholder = { Text("Ask Wealth Advisor...", color = SterlingSilver) },
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AurumGold,
                    unfocusedBorderColor = SteelCardBorder,
                    focusedTextColor = PlatinumWhite,
                    unfocusedTextColor = PlatinumWhite
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field")
            )

            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        viewModel.sendAssistantMessage(messageInput)
                        messageInput = ""
                    }
                },
                modifier = Modifier
                    .background(AurumGold, CircleShape)
                    .testTag("chat_send_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message", tint = ObsidianCharcoal)
            }
        }
    }
}
