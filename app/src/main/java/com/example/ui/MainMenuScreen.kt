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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.util.CurrencyFormatter
import com.example.util.ShopStrings

@Composable
fun MainMenuScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    totalProducts: Int,
    totalStockUnits: Int,
    todaySalesAmount: Double,
    todayPurchasesAmount: Double,
    todayProfitAmount: Double,
    infoMessage: String?,
    onClearInfoMessage: () -> Unit,
    onNavigateTo: (Screen) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onClearInfoMessage()
        }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = "Amjid Mobile Center",
                subtitle = "د امجد ګرځنده مرکز",
                showBackButton = false,
                language = language,
                onToggleLanguage = onToggleLanguage,
                showLogout = true,
                onLogout = onLogout
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Title Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Amjid Mobile Center",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "د ګرځنده پلورنځي مدیریت • Mobile Shop Management",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // DASHBOARD SECTION (Real live database statistics)
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = if (language == AppLanguage.PASHTO) "📊 عمومي معلومات (ډشبورډ)" else "📊 Dashboard Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Stat 1: Total Products
            item {
                StatCard(
                    title = ShopStrings.totalProducts(language),
                    value = "$totalProducts",
                    subtitle = if (language == AppLanguage.PASHTO) "ثبت شوي توکي" else "Registered items",
                    icon = Icons.Default.Inventory,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Stat 2: Total Stock
            item {
                StatCard(
                    title = ShopStrings.totalStock(language),
                    value = "$totalStockUnits",
                    subtitle = if (language == AppLanguage.PASHTO) "موجود عددونه" else "Units in store",
                    icon = Icons.Default.Store,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Stat 3: Today's Sales
            item {
                StatCard(
                    title = ShopStrings.todaySales(language),
                    value = CurrencyFormatter.formatAfn(todaySalesAmount),
                    icon = Icons.Default.MonetizationOn,
                    containerColor = Color(0xFFE0F2FE),
                    contentColor = Color(0xFF0369A1)
                )
            }

            // Stat 4: Today's Purchases
            item {
                StatCard(
                    title = ShopStrings.todayPurchases(language),
                    value = CurrencyFormatter.formatAfn(todayPurchasesAmount),
                    icon = Icons.Default.ShoppingCart,
                    containerColor = Color(0xFFFEF3C7),
                    contentColor = Color(0xFFB45309)
                )
            }

            // Stat 5: Today's Profit (Spanning both columns)
            item(span = { GridItemSpan(2) }) {
                StatCard(
                    title = ShopStrings.todayProfit(language),
                    value = CurrencyFormatter.formatAfn(todayProfitAmount),
                    subtitle = if (language == AppLanguage.PASHTO) "خالصه ګټه = د خرڅلاو بیه - د اخیستلو بیه" else "Net Profit = Selling Price - Purchase Price",
                    icon = Icons.Default.ShowChart,
                    containerColor = Color(0xFFDCFCE7),
                    contentColor = Color(0xFF15803D)
                )
            }

            // MAIN SECTIONS HEADER
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = if (language == AppLanguage.PASHTO) "اصلي برخې • Main Menu" else "Main Menu • اصلي برخې",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
            }

            // 1. Products - توکي
            item {
                MenuButtonCard(
                    number = "1",
                    title = "Products",
                    pashtoTitle = "توکي",
                    icon = Icons.Default.Inventory,
                    color = Color(0xFF2563EB),
                    testTag = "menu_products_button",
                    onClick = { onNavigateTo(Screen.PRODUCTS) }
                )
            }

            // 2. Purchases - اخیستل
            item {
                MenuButtonCard(
                    number = "2",
                    title = "Purchases",
                    pashtoTitle = "اخیستل",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFFD97706),
                    testTag = "menu_purchases_button",
                    onClick = { onNavigateTo(Screen.PURCHASES) }
                )
            }

            // 3. Sales - خرڅول
            item {
                MenuButtonCard(
                    number = "3",
                    title = "Sales",
                    pashtoTitle = "خرڅول",
                    icon = Icons.Default.MonetizationOn,
                    color = Color(0xFF059669),
                    testTag = "menu_sales_button",
                    onClick = { onNavigateTo(Screen.SALES) }
                )
            }

            // 4. Stock - موجود توکي
            item {
                MenuButtonCard(
                    number = "4",
                    title = "Stock",
                    pashtoTitle = "موجود توکي",
                    icon = Icons.Default.Store,
                    color = Color(0xFF0284C7),
                    testTag = "menu_stock_button",
                    onClick = { onNavigateTo(Screen.STOCK) }
                )
            }

            // 5. Customers - مشتریان
            item {
                MenuButtonCard(
                    number = "5",
                    title = "Customers",
                    pashtoTitle = "مشتریان",
                    icon = Icons.Default.People,
                    color = Color(0xFF7C3AED),
                    testTag = "menu_customers_button",
                    onClick = { onNavigateTo(Screen.CUSTOMERS) }
                )
            }

            // 6. Suppliers - پلورونکي
            item {
                MenuButtonCard(
                    number = "6",
                    title = "Suppliers",
                    pashtoTitle = "پلورونکي",
                    icon = Icons.Default.LocalShipping,
                    color = Color(0xFFEA580C),
                    testTag = "menu_suppliers_button",
                    onClick = { onNavigateTo(Screen.SUPPLIERS) }
                )
            }

            // 7. Profit - ګټه
            item {
                MenuButtonCard(
                    number = "7",
                    title = "Profit",
                    pashtoTitle = "ګټه",
                    icon = Icons.Default.ShowChart,
                    color = Color(0xFF16A34A),
                    testTag = "menu_profit_button",
                    onClick = { onNavigateTo(Screen.PROFIT) }
                )
            }

            // 8. Reports - راپورونه
            item {
                MenuButtonCard(
                    number = "8",
                    title = "Reports",
                    pashtoTitle = "راپورونه",
                    icon = Icons.Default.Assessment,
                    color = Color(0xFF4F46E5),
                    testTag = "menu_reports_button",
                    onClick = { onNavigateTo(Screen.REPORTS) }
                )
            }

            // 9. Transaction History (Full Span)
            item(span = { GridItemSpan(2) }) {
                Card(
                    onClick = { onNavigateTo(Screen.TRANSACTIONS_HISTORY) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("menu_transaction_history_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Transaction History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "د معاملو تاریخچه (پیرود، پلور، زېرمه)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 10. Backup & Restore (Full Span)
            item(span = { GridItemSpan(2) }) {
                Card(
                    onClick = { onNavigateTo(Screen.BACKUP_RESTORE) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0FDF4)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .testTag("menu_backup_restore_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF059669).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.PASHTO) "د ډیټابیس بیک اپ او بیارغونه" else "Database Backup & Restore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = if (language == AppLanguage.PASHTO) "د خپلو معلوماتو د خوندیتوب لپاره د SQLite فایل صادرول" else "Export & restore SQLite database file for safety",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButtonCard(
    number: String,
    title: String,
    pashtoTitle: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column {
                Text(
                    text = pashtoTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
