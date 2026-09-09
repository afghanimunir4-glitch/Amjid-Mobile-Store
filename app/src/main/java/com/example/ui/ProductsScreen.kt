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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.util.AppLanguage
import com.example.util.CurrencyFormatter
import com.example.util.DateTimeUtils
import com.example.util.ShopStrings
import kotlinx.coroutines.launch

@Composable
fun ProductsScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    products: List<Product>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddProduct: (code: String, name: String, purchasePrice: Double, sellingPrice: Double, stock: Int, date: String?, time: String?, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onUpdateProduct: (product: Product, name: String, purchasePrice: Double, sellingPrice: Double, stock: Int, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDeleteProduct: (product: Product, onSuccess: () -> Unit) -> Unit,
    infoMessage: String?,
    onClearInfoMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onClearInfoMessage()
        }
    }

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

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "توکي (Products)" else "Products (توکي)",
                subtitle = if (language == AppLanguage.PASHTO) "د توکو لیست او مدیریت" else "Product List & Management",
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "نوی توکی" else "ADD PRODUCT",
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

            // SEARCH BOX
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = if (language == AppLanguage.PASHTO)
                    "د توکي نوم یا شمېرې (ID) له مخې لټون..."
                else
                    "Search by Product Name or Product ID...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Total Count Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.PASHTO)
                        "موندل شوي توکي: ${filteredProducts.size}"
                    else
                        "Found Products: ${filteredProducts.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.PASHTO)
                                "هیڅ توکی ونه موندل شو"
                            else
                                "No products found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_add_product_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.PASHTO) "لومړی توکی زیات کړئ" else "Add First Product")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp, top = 6.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            language = language,
                            onEdit = { productToEdit = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // ADD PRODUCT DIALOG
    if (showAddDialog) {
        AddProductDialog(
            language = language,
            onDismiss = { showAddDialog = false },
            onSave = { code, name, pPrice, sPrice, stock, date, time ->
                onAddProduct(code, name, pPrice, sPrice, stock, date, time, {
                    showAddDialog = false
                }, { error ->
                    scope.launch { snackbarHostState.showSnackbar(error) }
                })
            }
        )
    }

    // EDIT PRODUCT DIALOG
    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            language = language,
            onDismiss = { productToEdit = null },
            onSave = { name, pPrice, sPrice, stock ->
                onUpdateProduct(product, name, pPrice, sPrice, stock, {
                    productToEdit = null
                }, { error ->
                    scope.launch { snackbarHostState.showSnackbar(error) }
                })
            }
        )
    }

    // DELETE PRODUCT CONFIRMATION
    productToDelete?.let { product ->
        ConfirmDeleteDialog(
            title = if (language == AppLanguage.PASHTO) "د توکي ړنګول" else "Delete Product",
            message = if (language == AppLanguage.PASHTO)
                "ایا تاسو باوري یاست چې '${product.name}' توکی ړنګ کړئ؟"
            else
                "Are you sure you want to delete '${product.name}'?",
            confirmText = ShopStrings.delete(language),
            cancelText = ShopStrings.cancel(language),
            onConfirm = {
                onDeleteProduct(product) {
                    productToDelete = null
                }
            },
            onDismiss = { productToDelete = null }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Product ID & Stock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ID: ${product.productIdCode}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Stock Badge
                val isLowStock = product.stockQuantity <= 1
                Box(
                    modifier = Modifier
                        .background(
                            if (isLowStock) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${ShopStrings.stock(language)}: ${product.stockQuantity}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) Color(0xFFB91C1C) else Color(0xFF15803D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price Details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = ShopStrings.purchasePrice(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatAfn(product.purchasePrice),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ShopStrings.sellingPrice(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatAfn(product.sellingPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date Added & Last Updated
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${ShopStrings.dateAdded(language)}: ${product.dateAdded} ${product.timeAdded}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (product.lastUpdatedDate != product.dateAdded || product.lastUpdatedTime != product.timeAdded) {
                Text(
                    text = "${ShopStrings.lastUpdatedDate(language)}: ${product.lastUpdatedDate} ${product.lastUpdatedTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: EDIT PRODUCT & DELETE PRODUCT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("edit_product_btn_${product.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "سمول (Edit)" else "EDIT PRODUCT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("delete_product_btn_${product.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "ړنګول (Delete)" else "DELETE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, purchasePrice: Double, sellingPrice: Double, stock: Int, date: String?, time: String?) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var purchasePriceStr by remember { mutableStateOf("") }
    var sellingPriceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("0") }
    var dateStr by remember { mutableStateOf(DateTimeUtils.currentDate()) }
    var timeStr by remember { mutableStateOf(DateTimeUtils.currentTime()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.PASHTO) "نوی توکی زیاتول" else "ADD PRODUCT",
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
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("${ShopStrings.productId(language)} (e.g. 001)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_code_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("${ShopStrings.productName(language)} * (e.g. Samsung S24)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                OutlinedTextField(
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("${ShopStrings.purchasePrice(language)} (AFN) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_purchase_price_input")
                )

                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("${ShopStrings.sellingPrice(language)} (AFN) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_selling_price_input")
                )

                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("${ShopStrings.stockQuantity(language)} *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_stock_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text(ShopStrings.dateAdded(language)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text(ShopStrings.timeAdded(language)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pPrice = purchasePriceStr.toDoubleOrNull() ?: 0.0
                    val sPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
                    val stock = stockStr.toIntOrNull() ?: 0
                    onSave(code, name, pPrice, sPrice, stock, dateStr, timeStr)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_new_product_btn")
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
fun EditProductDialog(
    product: Product,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (name: String, purchasePrice: Double, sellingPrice: Double, stock: Int) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var purchasePriceStr by remember { mutableStateOf(product.purchasePrice.toString()) }
    var sellingPriceStr by remember { mutableStateOf(product.sellingPrice.toString()) }
    var stockStr by remember { mutableStateOf(product.stockQuantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (language == AppLanguage.PASHTO) "د توکي سمول (Edit Product)" else "EDIT PRODUCT",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${product.productIdCode} | ${ShopStrings.dateAdded(language)}: ${product.dateAdded}",
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(ShopStrings.productName(language)) },
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

                OutlinedTextField(
                    value = sellingPriceStr,
                    onValueChange = { sellingPriceStr = it },
                    label = { Text("${ShopStrings.sellingPrice(language)} (AFN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text(ShopStrings.stockQuantity(language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pPrice = purchasePriceStr.toDoubleOrNull() ?: product.purchasePrice
                    val sPrice = sellingPriceStr.toDoubleOrNull() ?: product.sellingPrice
                    val stock = stockStr.toIntOrNull() ?: product.stockQuantity
                    onSave(name, pPrice, sPrice, stock)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_edit_product_btn")
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
