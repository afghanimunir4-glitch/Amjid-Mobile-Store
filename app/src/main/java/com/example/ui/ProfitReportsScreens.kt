package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.Purchase
import com.example.data.Sale
import com.example.data.StockTransaction
import com.example.util.AppLanguage
import com.example.util.CurrencyFormatter
import com.example.util.DateTimeUtils
import com.example.util.ShopStrings

// ================= PROFIT SCREEN =================

@Composable
fun ProfitScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    sales: List<Sale>
) {
    val todayStart = remember { DateTimeUtils.getStartOfToday() }
    val weekStart = remember { DateTimeUtils.getStartOfWeek() }
    val monthStart = remember { DateTimeUtils.getStartOfMonth() }
    val yearStart = remember { DateTimeUtils.getStartOfYear() }

    val todayProfit = remember(sales) {
        sales.filter { it.timestamp >= todayStart }.sumOf { it.profit }
    }
    val weekProfit = remember(sales) {
        sales.filter { it.timestamp >= weekStart }.sumOf { it.profit }
    }
    val monthProfit = remember(sales) {
        sales.filter { it.timestamp >= monthStart }.sumOf { it.profit }
    }
    val yearProfit = remember(sales) {
        sales.filter { it.timestamp >= yearStart }.sumOf { it.profit }
    }
    val totalProfit = remember(sales) {
        sales.sumOf { it.profit }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "ګټه (Profit)" else "Profit (ګټه)",
                subtitle = if (language == AppLanguage.PASHTO) "د ګټې شننه او تفریقي محاسبه" else "Profit Calculation & Margins",
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            // Formula Information Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.PASHTO) "د ګټې فورمول (Profit Formula)" else "Profit Formula",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.PASHTO)
                                "ګټه = د خرڅلاو بیه - د اخیستلو بیه\nProfit = (Selling Price - Purchase Price) × Quantity Sold"
                            else
                                "Profit = (Selling Price - Purchase Price) × Quantity Sold",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            // Summary Lifetime Profit
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.PASHTO) "ټوله تاریخي خالصه ګټه" else "Total Lifetime Net Profit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatAfn(totalProfit),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Breakdown Grid (2x2 Cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = ShopStrings.dailyReport(language),
                        value = CurrencyFormatter.formatAfn(todayProfit),
                        icon = Icons.Default.ShowChart,
                        containerColor = Color(0xFFE0F2FE),
                        contentColor = Color(0xFF0369A1),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = ShopStrings.weeklyReport(language),
                        value = CurrencyFormatter.formatAfn(weekProfit),
                        icon = Icons.Default.ShowChart,
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = ShopStrings.monthlyReport(language),
                        value = CurrencyFormatter.formatAfn(monthProfit),
                        icon = Icons.Default.ShowChart,
                        containerColor = Color(0xFFDCFCE7),
                        contentColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = ShopStrings.yearlyReport(language),
                        value = CurrencyFormatter.formatAfn(yearProfit),
                        icon = Icons.Default.ShowChart,
                        containerColor = Color(0xFFF3E8FF),
                        contentColor = Color(0xFF7E22CE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sales with Profit List Header
            item {
                Text(
                    text = if (language == AppLanguage.PASHTO) "د هر پلور ګټه (Sales Profit Details)" else "Sales Profit Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (sales.isEmpty()) {
                item {
                    Text(
                        text = if (language == AppLanguage.PASHTO) "تر اوسه کوم پلور نه دی شوی" else "No sales recorded yet",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(sales.reversed(), key = { it.id }) { sale ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(sale.productName, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${sale.date} • Qty: ${sale.quantity} (Sell: ${sale.sellingPrice} AFN - Buy: ${sale.purchasePriceAtSale} AFN)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${CurrencyFormatter.formatAfn(sale.profit)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sale.profit >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                                Text(
                                    text = "Total: ${CurrencyFormatter.formatAfn(sale.total)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= REPORTS SCREEN =================

@Composable
fun ReportsScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    products: List<Product>,
    purchases: List<Purchase>,
    sales: List<Sale>,
    stockMovements: List<StockTransaction>,
    viewModel: ShopViewModel
) {
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }
    var customFromDate by remember { mutableStateOf(DateTimeUtils.currentDate()) }
    var customToDate by remember { mutableStateOf(DateTimeUtils.currentDate()) }

    val fromTimestamp = remember(customFromDate) {
        DateTimeUtils.parseDateTime(customFromDate, "00:00")
    }
    val toTimestamp = remember(customToDate) {
        DateTimeUtils.parseDateTime(customToDate, "23:59:59")
    }

    val stats = remember(selectedPeriod, customFromDate, customToDate, sales, purchases, stockMovements) {
        viewModel.calculateStatsForRange(
            period = selectedPeriod,
            customFrom = fromTimestamp,
            customTo = toTimestamp,
            allSales = sales,
            allPurchases = purchases,
            allMovements = stockMovements
        )
    }

    val currentTotalStock = remember(products) { products.sumOf { it.stockQuantity } }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "راپورونه (Reports)" else "Reports (راپورونه)",
                subtitle = if (language == AppLanguage.PASHTO) "ورځنی، اونیز، میاشتنی او کلنی راپور" else "Daily, Weekly, Monthly & Yearly Reports",
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            // Period Filter Selector Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val periods = listOf(
                        Pair(ReportPeriod.TODAY, ShopStrings.dailyReport(language)),
                        Pair(ReportPeriod.YESTERDAY, if (language == AppLanguage.PASHTO) "د پرون راپور" else "Yesterday"),
                        Pair(ReportPeriod.THIS_WEEK, ShopStrings.weeklyReport(language)),
                        Pair(ReportPeriod.THIS_MONTH, ShopStrings.monthlyReport(language)),
                        Pair(ReportPeriod.THIS_YEAR, ShopStrings.yearlyReport(language)),
                        Pair(ReportPeriod.CUSTOM, ShopStrings.customReport(language))
                    )

                    items(periods) { (period, label) ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Custom Date Range Inputs
            if (selectedPeriod == ReportPeriod.CUSTOM) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customFromDate,
                                onValueChange = { customFromDate = it },
                                label = { Text("From Date") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customToDate,
                                onValueChange = { customToDate = it },
                                label = { Text("To Date") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // REPORT KEY METRICS (As requested: Total Sales, Total Purchases, Total Profit, Stock Activity, Current Stock)
            item {
                Text(
                    text = if (language == AppLanguage.PASHTO) "د ټاکلې مودې شمېرې" else "Period Performance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Row 1: Total Sales & Total Purchases
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = ShopStrings.totalSales(language),
                        value = CurrencyFormatter.formatAfn(stats.totalSalesAmount),
                        subtitle = "${stats.totalSalesCount} ${if (language == AppLanguage.PASHTO) "پلورونه" else "sales"}",
                        icon = Icons.Default.MonetizationOn,
                        containerColor = Color(0xFFDCFCE7),
                        contentColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = ShopStrings.totalPurchases(language),
                        value = CurrencyFormatter.formatAfn(stats.totalPurchasesAmount),
                        subtitle = "${stats.totalPurchasesCount} ${if (language == AppLanguage.PASHTO) "پیرودونه" else "purchases"}",
                        icon = Icons.Default.ShoppingCart,
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Total Profit Card
            item {
                StatCard(
                    title = ShopStrings.totalProfit(language),
                    value = CurrencyFormatter.formatAfn(stats.totalProfitAmount),
                    subtitle = if (language == AppLanguage.PASHTO) "خالصه ګټه = د خرڅلاو بیه - د اخیستلو بیه" else "Net profit generated in this period",
                    icon = Icons.Default.ShowChart,
                    containerColor = Color(0xFFE0F2FE),
                    contentColor = Color(0xFF0369A1),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Row 2: Stock Activity & Current Stock
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = ShopStrings.stockActivity(language),
                        value = "${stats.stockUnitsSold} / +${stats.stockUnitsPurchased}",
                        subtitle = if (language == AppLanguage.PASHTO) "پلورل شوي / اخیستل شوي" else "Sold / Purchased",
                        icon = Icons.Default.Assessment,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = ShopStrings.currentStock(language),
                        value = "$currentTotalStock",
                        subtitle = if (language == AppLanguage.PASHTO) "ټولې موجودې دانې" else "Total units in store",
                        icon = Icons.Default.Store,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section: Sales in this period
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${ShopStrings.sales(language)} (${stats.salesList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (stats.salesList.isEmpty()) {
                item {
                    Text(
                        text = if (language == AppLanguage.PASHTO) "په دې موده کې هیڅ خرڅلاو نشته" else "No sales in this period",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(stats.salesList, key = { it.id }) { sale ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(sale.productName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${sale.date} • ${sale.time} | Qty: ${sale.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(CurrencyFormatter.formatAfn(sale.total), fontWeight = FontWeight.Bold)
                                Text(
                                    "Profit: +${CurrencyFormatter.formatAfn(sale.profit)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Section: Purchases in this period
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${ShopStrings.purchases(language)} (${stats.purchasesList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (stats.purchasesList.isEmpty()) {
                item {
                    Text(
                        text = if (language == AppLanguage.PASHTO) "په دې موده کې هیڅ اخیستل نشته" else "No purchases in this period",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(stats.purchasesList, key = { it.id }) { purchase ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(purchase.productName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${purchase.date} • ${purchase.time} | Qty: ${purchase.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    CurrencyFormatter.formatAfn(purchase.total),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= TRANSACTION HISTORY SCREEN =================

@Composable
fun TransactionHistoryScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    purchases: List<Purchase>,
    sales: List<Sale>,
    stockTransactions: List<StockTransaction>
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(
        if (language == AppLanguage.PASHTO) "ټولې معاملي" else "All Transactions",
        if (language == AppLanguage.PASHTO) "اخیستل" else "Purchases",
        if (language == AppLanguage.PASHTO) "خرڅلاو" else "Sales",
        if (language == AppLanguage.PASHTO) "د زېرمې خوځښت" else "Stock Movements"
    )

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "د معاملو تاریخچه" else "Transaction History",
                subtitle = if (language == AppLanguage.PASHTO) "د هرې معاملې بشپړ او تلپاتې ریکارډ" else "Permanent Transaction Log",
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Unified chronological list of all sales & purchases
                    val allRecords = remember(purchases, sales) {
                        val pList = purchases.map {
                            CombinedTransaction(
                                date = it.date,
                                time = it.time,
                                timestamp = it.timestamp,
                                productName = it.productName,
                                type = "PURCHASE",
                                quantity = it.quantity,
                                price = it.purchasePrice,
                                total = it.total,
                                extra = it.supplierName
                            )
                        }
                        val sList = sales.map {
                            CombinedTransaction(
                                date = it.date,
                                time = it.time,
                                timestamp = it.timestamp,
                                productName = it.productName,
                                type = "SALE",
                                quantity = it.quantity,
                                price = it.sellingPrice,
                                total = it.total,
                                extra = it.customerName
                            )
                        }
                        (pList + sList).sortedByDescending { it.timestamp }
                    }

                    if (allRecords.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.PASHTO) "هیڅ معامله نشته" else "No transactions found",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(allRecords) { record ->
                                CombinedTransactionCard(record = record, language = language)
                            }
                        }
                    }
                }
                1 -> {
                    // Purchases only
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(purchases.reversed(), key = { it.id }) { purchase ->
                            CombinedTransactionCard(
                                record = CombinedTransaction(
                                    date = purchase.date,
                                    time = purchase.time,
                                    timestamp = purchase.timestamp,
                                    productName = purchase.productName,
                                    type = "PURCHASE",
                                    quantity = purchase.quantity,
                                    price = purchase.purchasePrice,
                                    total = purchase.total,
                                    extra = purchase.supplierName
                                ),
                                language = language
                            )
                        }
                    }
                }
                2 -> {
                    // Sales only
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sales.reversed(), key = { it.id }) { sale ->
                            CombinedTransactionCard(
                                record = CombinedTransaction(
                                    date = sale.date,
                                    time = sale.time,
                                    timestamp = sale.timestamp,
                                    productName = sale.productName,
                                    type = "SALE",
                                    quantity = sale.quantity,
                                    price = sale.sellingPrice,
                                    total = sale.total,
                                    extra = sale.customerName
                                ),
                                language = language
                            )
                        }
                    }
                }
                3 -> {
                    // Stock movements log
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(stockTransactions.reversed(), key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${item.date} • ${item.time}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Type: ${item.type}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${if (item.quantityChange > 0) "+" else ""}${item.quantityChange} (Stock: ${item.previousStock} → ${item.newStock})",
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.quantityChange >= 0) Color(0xFF15803D) else Color(0xFFDC2626)
                                        )
                                    }
                                    if (item.note.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.note,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CombinedTransaction(
    val date: String,
    val time: String,
    val timestamp: Long,
    val productName: String,
    val type: String, // "PURCHASE" or "SALE"
    val quantity: Int,
    val price: Double,
    val total: Double,
    val extra: String
)

@Composable
fun CombinedTransactionCard(
    record: CombinedTransaction,
    language: AppLanguage
) {
    val isPurchase = record.type == "PURCHASE"
    val badgeBg = if (isPurchase) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
    val badgeColor = if (isPurchase) Color(0xFFB45309) else Color(0xFF15803D)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Date - Time | Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.date} - ${record.time}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isPurchase) {
                            if (language == AppLanguage.PASHTO) "اخیستل | PURCHASE" else "PURCHASE"
                        } else {
                            if (language == AppLanguage.PASHTO) "خرڅول | SALE" else "SALE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Product Name
            Text(
                text = record.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quantity | Price | Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ShopStrings.quantity(language)}: ${record.quantity} | ${ShopStrings.price(language)}: ${CurrencyFormatter.formatAfn(record.price)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${ShopStrings.total(language)}: ${CurrencyFormatter.formatAfn(record.total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            if (record.extra.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (isPurchase) ShopStrings.suppliers(language) else ShopStrings.customers(language)}: ${record.extra}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
