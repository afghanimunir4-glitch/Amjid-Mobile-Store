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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.util.AppLanguage
import com.example.util.CurrencyFormatter
import com.example.util.ShopStrings

@Composable
fun StockScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    products: List<Product>,
    purchases: List<Purchase>,
    sales: List<Sale>,
    onViewTransactions: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            val q = searchQuery.trim().lowercase()
            products.filter {
                it.name.lowercase().contains(q) || it.productIdCode.lowercase().contains(q)
            }
        }
    }

    // Purchases and sales maps for fast lookup
    val purchasesByProduct = remember(purchases) {
        purchases.groupBy { it.productId }
    }
    val salesByProduct = remember(sales) {
        sales.groupBy { it.productId }
    }

    val totalStoreUnits = remember(products) { products.sumOf { it.stockQuantity } }
    val totalStockValue = remember(products) { products.sumOf { it.stockQuantity * it.purchasePrice } }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "موجود توکي (Stock)" else "Stock Management (موجود توکي)",
                subtitle = if (language == AppLanguage.PASHTO) "د ګودام او زېرمې څارنه" else "Inventory & Stock Levels",
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Overall Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == AppLanguage.PASHTO) "ټولې موجودې دانې" else "Total Units in Stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$totalStoreUnits ${if (language == AppLanguage.PASHTO) "دانې" else "units"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (language == AppLanguage.PASHTO) "د زېرمې ټوله بیه" else "Total Stock Value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = CurrencyFormatter.formatAfn(totalStockValue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = if (language == AppLanguage.PASHTO)
                    "د توکي له مخې لټون..."
                else
                    "Search stock items..."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // View Transaction Log shortcut
            Button(
                onClick = onViewTransactions,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stock_view_history_btn")
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.PASHTO)
                        "د زېرمې او معاملو بشپړ تاریخ وګورئ"
                    else
                        "View Complete Stock Movement Log",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == AppLanguage.PASHTO) "هیڅ توکی ونه موندل شو" else "No products found in stock",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val productPurchases = purchasesByProduct[product.id] ?: emptyList()
                        val productSales = salesByProduct[product.id] ?: emptyList()

                        val totalPurchasedQty = productPurchases.sumOf { it.quantity }
                        val totalSoldQty = productSales.sumOf { it.quantity }

                        StockItemCard(
                            product = product,
                            totalPurchased = totalPurchasedQty,
                            totalSold = totalSoldQty,
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StockItemCard(
    product: Product,
    totalPurchased: Int,
    totalSold: Int,
    language: AppLanguage
) {
    val isLowStock = product.stockQuantity <= 1

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_item_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Product ID & Low Stock indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ID: ${product.productIdCode}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isLowStock) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Stock",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.PASHTO) "کمه زېرمه" else "Low Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Column Stats: Total Purchased, Total Sold, Remaining Stock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Total Purchased
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ShopStrings.totalPurchased(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalPurchased",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Total Sold
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ShopStrings.totalSold(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalSold",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Remaining Stock
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ShopStrings.remainingStock(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${product.stockQuantity}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${ShopStrings.purchasePrice(language)}: ${CurrencyFormatter.formatAfn(product.purchasePrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${ShopStrings.sellingPrice(language)}: ${CurrencyFormatter.formatAfn(product.sellingPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
