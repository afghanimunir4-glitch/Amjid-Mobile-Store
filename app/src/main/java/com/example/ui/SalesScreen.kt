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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.data.Product
import com.example.data.Sale
import com.example.util.AppLanguage
import com.example.util.CurrencyFormatter
import com.example.util.DateTimeUtils
import com.example.util.ShopStrings
import kotlinx.coroutines.launch

@Composable
fun SalesScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    sales: List<Sale>,
    products: List<Product>,
    customers: List<Customer>,
    onRecordSale: (productId: Long, quantity: Int, sellingPrice: Double, customerName: String, date: String?, time: String?, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onCorrectSale: (sale: Sale, newQuantity: Int, newSellingPrice: Double, newCustomerName: String, newDate: String, newTime: String, newNotes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    infoMessage: String?,
    onClearInfoMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showNewSaleDialog by remember { mutableStateOf(false) }
    var saleToCorrect by remember { mutableStateOf<Sale?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onClearInfoMessage()
        }
    }

    val filteredSales = remember(sales, searchQuery) {
        if (searchQuery.isBlank()) {
            sales
        } else {
            val q = searchQuery.trim().lowercase()
            sales.filter {
                it.productName.lowercase().contains(q) ||
                        it.saleIdCode.lowercase().contains(q) ||
                        it.customerName.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "خرڅول (Sales)" else "Sales (خرڅول)",
                subtitle = if (language == AppLanguage.PASHTO) "د توکو پلور او ګټې محاسبه" else "Sales Registration & Stock Out",
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewSaleDialog = true },
                containerColor = Color(0xFF059669),
                contentColor = Color.White,
                modifier = Modifier.testTag("new_sale_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Sale")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "نوی خرڅلاو" else "NEW SALE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = if (language == AppLanguage.PASHTO)
                    "د توکي نوم، پلور شمېره یا پیرودونکي له مخې لټون..."
                else
                    "Search by product, sale ID, or customer..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Header
            val totalSalesRevenue = remember(sales) { sales.sumOf { it.total } }
            val totalProfit = remember(sales) { sales.sumOf { it.profit } }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (language == AppLanguage.PASHTO) "ټول خرڅلاو" else "Total Sales"}: ${sales.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${CurrencyFormatter.formatAfn(totalSalesRevenue)} | ${if (language == AppLanguage.PASHTO) "ګټه" else "Profit"}: ${CurrencyFormatter.formatAfn(totalProfit)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredSales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.PASHTO)
                                "هیڅ خرڅلاو نه دی ثبت شوی"
                            else
                                "No sales records found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showNewSaleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (language == AppLanguage.PASHTO) "نوی خرڅلاو ثبت کړئ" else "Register Sale")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp, top = 6.dp)
                ) {
                    items(filteredSales, key = { it.id }) { sale ->
                        SaleItemCard(
                            sale = sale,
                            language = language,
                            onCorrect = { saleToCorrect = sale }
                        )
                    }
                }
            }
        }
    }

    // NEW SALE DIALOG
    if (showNewSaleDialog) {
        NewSaleDialog(
            products = products,
            customers = customers,
            language = language,
            onDismiss = { showNewSaleDialog = false },
            onSave = { pId, qty, price, customerName, date, time, notes ->
                onRecordSale(pId, qty, price, customerName, date, time, notes, {
                    showNewSaleDialog = false
                }, { error ->
                    scope.launch { snackbarHostState.showSnackbar(error) }
                })
            }
        )
    }

    // CORRECT SALE DIALOG
    saleToCorrect?.let { sale ->
        CorrectSaleDialog(
            sale = sale,
            language = language,
            onDismiss = { saleToCorrect = null },
            onSave = { newQty, newPrice, newCustomer, newDate, newTime, newNotes ->
                onCorrectSale(
                    sale, newQty, newPrice, newCustomer, newDate, newTime, newNotes,
                    { saleToCorrect = null },
                    { error -> scope.launch { snackbarHostState.showSnackbar(error) } }
                )
            }
        )
    }
}

@Composable
fun SaleItemCard(
    sale: Sale,
    language: AppLanguage,
    onCorrect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_card_${sale.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ID: ${sale.saleIdCode}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }

                Text(
                    text = "${sale.date} • ${sale.time}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = sale.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (sale.customerName.isNotBlank()) {
                Text(
                    text = "${ShopStrings.customers(language)}: ${sale.customerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total and Profit calculation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${ShopStrings.quantity(language)}: ${sale.quantity} × ${CurrencyFormatter.formatAfn(sale.sellingPrice)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${ShopStrings.total(language)}: ${CurrencyFormatter.formatAfn(sale.total)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ShopStrings.profit(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatAfn(sale.profit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (sale.profit >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }

            if (sale.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${ShopStrings.notes(language)}: ${sale.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            // Safe Edit / Correct Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCorrect,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "اصلاح کول (Correct)" else "EDIT / CORRECT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleDialog(
    products: List<Product>,
    customers: List<Customer>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (productId: Long, quantity: Int, sellingPrice: Double, customerName: String, date: String, time: String, notes: String) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull { it.stockQuantity > 0 } ?: products.firstOrNull()) }
    var expandedProductDropdown by remember { mutableStateOf(false) }

    var quantityStr by remember { mutableStateOf("1") }
    var sellingPriceStr by remember {
        mutableStateOf(selectedProduct?.sellingPrice?.toString() ?: "")
    }
    var customerName by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(DateTimeUtils.currentDate()) }
    var timeStr by remember { mutableStateOf(DateTimeUtils.currentTime()) }
    var notes by remember { mutableStateOf("") }

    val quantity = quantityStr.toIntOrNull() ?: 0
    val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val total = quantity * sellingPrice

    val availableStock = selectedProduct?.stockQuantity ?: 0
    val isStockExceeded = quantity > availableStock

    val estimatedProfit = remember(selectedProduct, quantity, sellingPrice) {
        selectedProduct?.let {
            (sellingPrice - it.purchasePrice) * quantity
        } ?: 0.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.PASHTO) "د نوي خرڅلاو ثبتول" else "REGISTER SALE",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Product Selector
                ExposedDropdownMenuBox(
                    expanded = expandedProductDropdown,
                    onExpandedChange = { expandedProductDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.let { "${it.name} (Stock: ${it.stockQuantity})" }
                            ?: if (language == AppLanguage.PASHTO) "توکی وټاکئ" else "Select Product",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(ShopStrings.products(language)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductDropdown)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProductDropdown,
                        onDismissRequest = { expandedProductDropdown = false }
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(product.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "ID: ${product.productIdCode} | Price: ${product.sellingPrice} AFN",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            text = "Stock: ${product.stockQuantity}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (product.stockQuantity > 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProduct = product
                                    sellingPriceStr = product.sellingPrice.toString()
                                    expandedProductDropdown = false
                                }
                            )
                        }
                    }
                }

                // Available Stock Banner
                selectedProduct?.let { product ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (product.stockQuantity > 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.PASHTO) "موجوده زېرمه (Available Stock):" else "Available Stock:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${product.stockQuantity} units",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (product.stockQuantity > 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                    }
                }

                // Quantity
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("${ShopStrings.quantity(language)} *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isStockExceeded,
                    supportingText = {
                        if (isStockExceeded) {
                            Text(
                                text = if (language == AppLanguage.PASHTO)
                                    "تاسو نشئ کولی له شته موجودي ($availableStock) څخه ډېر وپلورئ!"
                                else
                                    "Cannot sell more than available stock ($availableStock)!",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sale_quantity_input")
                )

                // Selling Price
                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("${ShopStrings.sellingPrice(language)} (AFN) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sale_price_input")
                )

                // AUTOMATICALLY CALCULATED TOTAL & ESTIMATED PROFIT
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${ShopStrings.total(language)} (Qty × Price):",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0369A1)
                            )
                            Text(
                                text = CurrencyFormatter.formatAfn(total),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${ShopStrings.profit(language)}:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = CurrencyFormatter.formatAfn(estimatedProfit),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(ShopStrings.customers(language)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("Time") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(ShopStrings.notes(language)) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedProduct?.let { product ->
                        if (quantity > 0 && !isStockExceeded && sellingPrice >= 0) {
                            onSave(product.id, quantity, sellingPrice, customerName, dateStr, timeStr, notes)
                        }
                    }
                },
                enabled = selectedProduct != null && quantity > 0 && !isStockExceeded,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_sale_btn")
            ) {
                Text(ShopStrings.save(language), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(ShopStrings.cancel(language))
            }
        }
    )
}

@Composable
fun CorrectSaleDialog(
    sale: Sale,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (newQuantity: Int, newSellingPrice: Double, newCustomer: String, newDate: String, newTime: String, newNotes: String) -> Unit
) {
    var quantityStr by remember { mutableStateOf(sale.quantity.toString()) }
    var sellingPriceStr by remember { mutableStateOf(sale.sellingPrice.toString()) }
    var customerName by remember { mutableStateOf(sale.customerName) }
    var dateStr by remember { mutableStateOf(sale.date) }
    var timeStr by remember { mutableStateOf(sale.time) }
    var notes by remember { mutableStateOf(sale.notes) }

    val qty = quantityStr.toIntOrNull() ?: 0
    val price = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val total = qty * price
    val recalculatedProfit = (price - sale.purchasePriceAtSale) * qty

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (language == AppLanguage.PASHTO) "د خرڅلاو اصلاح کول" else "CORRECT SALE",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${sale.saleIdCode} • ${sale.productName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.PASHTO)
                        "د خرڅلاو په اصلاح سره به موجودي او ګټه په کره توګه بیا محاسبه شي."
                    else
                        "Correcting sale quantity will update stock and recalculate profit in the database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text(ShopStrings.quantity(language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("${ShopStrings.sellingPrice(language)} (AFN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("New Total:", fontWeight = FontWeight.SemiBold)
                            Text(CurrencyFormatter.formatAfn(total), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("New Profit:", fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
                            Text(CurrencyFormatter.formatAfn(recalculatedProfit), fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(ShopStrings.customers(language)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("Time") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(ShopStrings.notes(language)) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qty > 0 && price >= 0) {
                        onSave(qty, price, customerName, dateStr, timeStr, notes)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(ShopStrings.save(language), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(ShopStrings.cancel(language))
            }
        }
    )
}
