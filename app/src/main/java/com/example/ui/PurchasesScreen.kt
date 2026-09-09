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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.example.data.Product
import com.example.data.Purchase
import com.example.data.Supplier
import com.example.util.AppLanguage
import com.example.util.CurrencyFormatter
import com.example.util.DateTimeUtils
import com.example.util.ShopStrings
import kotlinx.coroutines.launch

@Composable
fun PurchasesScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    purchases: List<Purchase>,
    products: List<Product>,
    suppliers: List<Supplier>,
    onRecordPurchase: (productId: Long, quantity: Int, purchasePrice: Double, supplierName: String, date: String?, time: String?, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onCorrectPurchase: (purchase: Purchase, newQuantity: Int, newPurchasePrice: Double, newSupplierName: String, newDate: String, newTime: String, newNotes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    infoMessage: String?,
    onClearInfoMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showNewPurchaseDialog by remember { mutableStateOf(false) }
    var purchaseToCorrect by remember { mutableStateOf<Purchase?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onClearInfoMessage()
        }
    }

    val filteredPurchases = remember(purchases, searchQuery) {
        if (searchQuery.isBlank()) {
            purchases
        } else {
            val q = searchQuery.trim().lowercase()
            purchases.filter {
                it.productName.lowercase().contains(q) ||
                        it.purchaseIdCode.lowercase().contains(q) ||
                        it.supplierName.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "اخیستل (Purchases)" else "Purchases (اخیستل)",
                subtitle = if (language == AppLanguage.PASHTO) "د توکو پیرود او د زېرمې زیاتوالی" else "Purchase Registration & Stock In",
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewPurchaseDialog = true },
                containerColor = Color(0xFFD97706),
                contentColor = Color.White,
                modifier = Modifier.testTag("new_purchase_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = "New Purchase")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "نوی اخیستل" else "NEW PURCHASE",
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
                    "د توکي نوم یا د اخیستلو شمېرې له مخې لټون..."
                else
                    "Search by product or purchase ID..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Header
            val totalPurchasedValue = remember(purchases) { purchases.sumOf { it.total } }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (language == AppLanguage.PASHTO) "ټول ثبت شوي اخیستل" else "Total Purchases"}: ${purchases.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = CurrencyFormatter.formatAfn(totalPurchasedValue),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredPurchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.PASHTO)
                                "هیڅ اخیستل نه دي ثبت شوي"
                            else
                                "No purchase records found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showNewPurchaseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (language == AppLanguage.PASHTO) "نوی اخیستل ثبت کړئ" else "Register Purchase")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp, top = 6.dp)
                ) {
                    items(filteredPurchases, key = { it.id }) { purchase ->
                        PurchaseItemCard(
                            purchase = purchase,
                            language = language,
                            onCorrect = { purchaseToCorrect = purchase }
                        )
                    }
                }
            }
        }
    }

    // NEW PURCHASE DIALOG
    if (showNewPurchaseDialog) {
        NewPurchaseDialog(
            products = products,
            suppliers = suppliers,
            language = language,
            onDismiss = { showNewPurchaseDialog = false },
            onSave = { pId, qty, price, supplierName, date, time, notes ->
                onRecordPurchase(pId, qty, price, supplierName, date, time, notes, {
                    showNewPurchaseDialog = false
                }, { error ->
                    scope.launch { snackbarHostState.showSnackbar(error) }
                })
            }
        )
    }

    // CORRECT PURCHASE DIALOG
    purchaseToCorrect?.let { purchase ->
        CorrectPurchaseDialog(
            purchase = purchase,
            language = language,
            onDismiss = { purchaseToCorrect = null },
            onSave = { newQty, newPrice, newSupplier, newDate, newTime, newNotes ->
                onCorrectPurchase(
                    purchase, newQty, newPrice, newSupplier, newDate, newTime, newNotes,
                    { purchaseToCorrect = null },
                    { error -> scope.launch { snackbarHostState.showSnackbar(error) } }
                )
            }
        )
    }
}

@Composable
fun PurchaseItemCard(
    purchase: Purchase,
    language: AppLanguage,
    onCorrect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("purchase_card_${purchase.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Purchase ID & Date/Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ID: ${purchase.purchaseIdCode}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }

                Text(
                    text = "${purchase.date} • ${purchase.time}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product Name
            Text(
                text = purchase.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (purchase.supplierName.isNotBlank()) {
                Text(
                    text = "${ShopStrings.suppliers(language)}: ${purchase.supplierName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calculation Box: Quantity x Price = Total
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
                        text = "${ShopStrings.quantity(language)}: ${purchase.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${ShopStrings.purchasePrice(language)}: ${CurrencyFormatter.formatAfn(purchase.purchasePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ShopStrings.total(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatAfn(purchase.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }

            if (purchase.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${ShopStrings.notes(language)}: ${purchase.notes}",
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
fun NewPurchaseDialog(
    products: List<Product>,
    suppliers: List<Supplier>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (productId: Long, quantity: Int, purchasePrice: Double, supplierName: String, date: String, time: String, notes: String) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var expandedProductDropdown by remember { mutableStateOf(false) }

    var quantityStr by remember { mutableStateOf("1") }
    var purchasePriceStr by remember {
        mutableStateOf(selectedProduct?.purchasePrice?.toString() ?: "")
    }
    var supplierName by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(DateTimeUtils.currentDate()) }
    var timeStr by remember { mutableStateOf(DateTimeUtils.currentTime()) }
    var notes by remember { mutableStateOf("") }

    // Auto calculate Total = Quantity x Purchase Price
    val quantity = quantityStr.toIntOrNull() ?: 0
    val purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0
    val total = quantity * purchasePrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.PASHTO) "د نوي اخیستلو ثبتول" else "REGISTER PURCHASE",
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
                // Product Selector Dropdown
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
                                    Column {
                                        Text(product.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "ID: ${product.productIdCode} | Current Stock: ${product.stockQuantity} | Price: ${product.purchasePrice} AFN",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProduct = product
                                    purchasePriceStr = product.purchasePrice.toString()
                                    expandedProductDropdown = false
                                }
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
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("purchase_quantity_input")
                )

                // Purchase Price
                OutlinedTextField(
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("${ShopStrings.purchasePrice(language)} (AFN) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("purchase_price_input")
                )

                // AUTOMATICALLY CALCULATED TOTAL BANNER
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ShopStrings.total(language)} (Qty × Price):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )
                        Text(
                            text = CurrencyFormatter.formatAfn(total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                // Supplier
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text(ShopStrings.suppliers(language)) },
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
                        if (quantity > 0 && purchasePrice >= 0) {
                            onSave(product.id, quantity, purchasePrice, supplierName, dateStr, timeStr, notes)
                        }
                    }
                },
                enabled = selectedProduct != null && quantity > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_purchase_btn")
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
fun CorrectPurchaseDialog(
    purchase: Purchase,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (newQuantity: Int, newPurchasePrice: Double, newSupplier: String, newDate: String, newTime: String, newNotes: String) -> Unit
) {
    var quantityStr by remember { mutableStateOf(purchase.quantity.toString()) }
    var purchasePriceStr by remember { mutableStateOf(purchase.purchasePrice.toString()) }
    var supplierName by remember { mutableStateOf(purchase.supplierName) }
    var dateStr by remember { mutableStateOf(purchase.date) }
    var timeStr by remember { mutableStateOf(purchase.time) }
    var notes by remember { mutableStateOf(purchase.notes) }

    val qty = quantityStr.toIntOrNull() ?: 0
    val price = purchasePriceStr.toDoubleOrNull() ?: 0.0
    val total = qty * price

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (language == AppLanguage.PASHTO) "د اخیستلو اصلاح کول" else "CORRECT PURCHASE",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${purchase.purchaseIdCode} • ${purchase.productName}",
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
                        "د اخیستلو په اصلاح سره به موجودي (Stock) په اتومات ډول بیا سمه شي."
                    else
                        "Correcting quantity will automatically adjust product stock in the database.",
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
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("${ShopStrings.purchasePrice(language)} (AFN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recalculated Total:", fontWeight = FontWeight.SemiBold, color = Color(0xFFB45309))
                        Text(CurrencyFormatter.formatAfn(total), fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    }
                }

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text(ShopStrings.suppliers(language)) },
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
                        onSave(qty, price, supplierName, dateStr, timeStr, notes)
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
