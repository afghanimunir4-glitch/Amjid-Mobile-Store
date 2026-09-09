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
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.data.Supplier
import com.example.util.AppLanguage
import com.example.util.ShopStrings
import kotlinx.coroutines.launch

// ================= CUSTOMERS SCREEN =================

@Composable
fun CustomersScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    customers: List<Customer>,
    onAddCustomer: (code: String, name: String, phone: String, address: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onUpdateCustomer: (customer: Customer, name: String, phone: String, address: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDeleteCustomer: (customer: Customer, onSuccess: () -> Unit) -> Unit,
    infoMessage: String?,
    onClearInfoMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onClearInfoMessage()
        }
    }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) {
            customers
        } else {
            val q = searchQuery.trim().lowercase()
            customers.filter {
                it.name.lowercase().contains(q) ||
                        it.phoneNumber.lowercase().contains(q) ||
                        it.customerIdCode.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "مشتریان (Customers)" else "Customers (مشتریان)",
                subtitle = if (language == AppLanguage.PASHTO) "د پیرودونکو نوملړ او مدیریت" else "Customer Directory",
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
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "نوی پیرودونکی" else "ADD CUSTOMER",
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
                    "د پیرودونکي نوم، ټیلیفون، یا شمېرې له مخې لټون..."
                else
                    "Search by customer name, phone, or ID..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.PASHTO) "هیڅ پیرودونکی ونه موندل شو" else "No customers found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (language == AppLanguage.PASHTO) "لومړی پیرودونکی زیات کړئ" else "Add First Customer")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp, top = 4.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerItemCard(
                            customer = customer,
                            language = language,
                            onEdit = { customerToEdit = customer },
                            onDelete = { customerToDelete = customer }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CustomerDialog(
            customer = null,
            language = language,
            onDismiss = { showAddDialog = false },
            onSave = { code, name, phone, address, notes ->
                onAddCustomer(code, name, phone, address, notes, {
                    showAddDialog = false
                }, { error -> scope.launch { snackbarHostState.showSnackbar(error) } })
            }
        )
    }

    customerToEdit?.let { customer ->
        CustomerDialog(
            customer = customer,
            language = language,
            onDismiss = { customerToEdit = null },
            onSave = { _, name, phone, address, notes ->
                onUpdateCustomer(customer, name, phone, address, notes, {
                    customerToEdit = null
                }, { error -> scope.launch { snackbarHostState.showSnackbar(error) } })
            }
        )
    }

    customerToDelete?.let { customer ->
        ConfirmDeleteDialog(
            title = if (language == AppLanguage.PASHTO) "د پیرودونکي ړنګول" else "Delete Customer",
            message = if (language == AppLanguage.PASHTO)
                "ایا تاسو باوري یاست چې '${customer.name}' ړنګ کړئ؟"
            else
                "Are you sure you want to delete '${customer.name}'?",
            confirmText = ShopStrings.delete(language),
            cancelText = ShopStrings.cancel(language),
            onConfirm = {
                onDeleteCustomer(customer) { customerToDelete = null }
            },
            onDismiss = { customerToDelete = null }
        )
    }
}

@Composable
fun CustomerItemCard(
    customer: Customer,
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
            .testTag("customer_card_${customer.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ID: ${customer.customerIdCode}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${customer.dateAdded} ${customer.timeAdded}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = customer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (customer.phoneNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = customer.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = customer.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = customer.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (customer.lastUpdatedDate != customer.dateAdded || customer.lastUpdatedTime != customer.timeAdded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${ShopStrings.lastUpdatedDate(language)}: ${customer.lastUpdatedDate} ${customer.lastUpdatedTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (language == AppLanguage.PASHTO) "سمول (Edit)" else "EDIT", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (language == AppLanguage.PASHTO) "ړنګول" else "DELETE", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CustomerDialog(
    customer: Customer?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, phone: String, address: String, notes: String) -> Unit
) {
    val isEdit = customer != null
    var code by remember { mutableStateOf(customer?.customerIdCode ?: "") }
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) {
                    if (language == AppLanguage.PASHTO) "د پیرودونکي سمول" else "EDIT CUSTOMER"
                } else {
                    if (language == AppLanguage.PASHTO) "نوی پیرودونکی زیاتول" else "ADD CUSTOMER"
                },
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
                if (!isEdit) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("${ShopStrings.customerId(language)} (e.g. C-01)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("${ShopStrings.customerName(language)} *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(ShopStrings.phoneNumber(language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(ShopStrings.address(language)) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

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
                    if (name.isNotBlank()) {
                        onSave(code, name, phone, address, notes)
                    }
                },
                enabled = name.isNotBlank(),
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

// ================= SUPPLIERS SCREEN =================

@Composable
fun SuppliersScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    suppliers: List<Supplier>,
    onAddSupplier: (code: String, name: String, phone: String, address: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onUpdateSupplier: (supplier: Supplier, name: String, phone: String, address: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDeleteSupplier: (supplier: Supplier, onSuccess: () -> Unit) -> Unit,
    infoMessage: String?,
    onClearInfoMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }
    var supplierToDelete by remember { mutableStateOf<Supplier?>(null) }

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onClearInfoMessage()
        }
    }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        if (searchQuery.isBlank()) {
            suppliers
        } else {
            val q = searchQuery.trim().lowercase()
            suppliers.filter {
                it.name.lowercase().contains(q) ||
                        it.phoneNumber.lowercase().contains(q) ||
                        it.supplierIdCode.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = if (language == AppLanguage.PASHTO) "پلورونکي (Suppliers)" else "Suppliers (پلورونکي)",
                subtitle = if (language == AppLanguage.PASHTO) "د عرضه کوونکو مدیریت" else "Supplier Directory",
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFEA580C),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_supplier_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.PASHTO) "نوی عرضه کوونکی" else "ADD SUPPLIER",
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
                    "د عرضه کوونکي نوم، ټیلیفون، یا شمېرې له مخې لټون..."
                else
                    "Search by supplier name, phone, or ID..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredSuppliers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.PASHTO) "هیڅ عرضه کوونکی ونه موندل شو" else "No suppliers found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (language == AppLanguage.PASHTO) "لومړی عرضه کوونکی زیات کړئ" else "Add First Supplier")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp, top = 4.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        SupplierItemCard(
                            supplier = supplier,
                            language = language,
                            onEdit = { supplierToEdit = supplier },
                            onDelete = { supplierToDelete = supplier }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SupplierDialog(
            supplier = null,
            language = language,
            onDismiss = { showAddDialog = false },
            onSave = { code, name, phone, address, notes ->
                onAddSupplier(code, name, phone, address, notes, {
                    showAddDialog = false
                }, { error -> scope.launch { snackbarHostState.showSnackbar(error) } })
            }
        )
    }

    supplierToEdit?.let { supplier ->
        SupplierDialog(
            supplier = supplier,
            language = language,
            onDismiss = { supplierToEdit = null },
            onSave = { _, name, phone, address, notes ->
                onUpdateSupplier(supplier, name, phone, address, notes, {
                    supplierToEdit = null
                }, { error -> scope.launch { snackbarHostState.showSnackbar(error) } })
            }
        )
    }

    supplierToDelete?.let { supplier ->
        ConfirmDeleteDialog(
            title = if (language == AppLanguage.PASHTO) "د عرضه کوونکي ړنګول" else "Delete Supplier",
            message = if (language == AppLanguage.PASHTO)
                "ایا تاسو باوري یاست چې '${supplier.name}' ړنګ کړئ؟"
            else
                "Are you sure you want to delete '${supplier.name}'?",
            confirmText = ShopStrings.delete(language),
            cancelText = ShopStrings.cancel(language),
            onConfirm = {
                onDeleteSupplier(supplier) { supplierToDelete = null }
            },
            onDismiss = { supplierToDelete = null }
        )
    }
}

@Composable
fun SupplierItemCard(
    supplier: Supplier,
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
            .testTag("supplier_card_${supplier.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFEA580C).copy(alpha = 0.12f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ID: ${supplier.supplierIdCode}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )
                }

                Text(
                    text = "${supplier.dateAdded} ${supplier.timeAdded}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = supplier.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (supplier.phoneNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFEA580C)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = supplier.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (supplier.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = supplier.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (supplier.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = supplier.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (supplier.lastUpdatedDate != supplier.dateAdded || supplier.lastUpdatedTime != supplier.timeAdded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${ShopStrings.lastUpdatedDate(language)}: ${supplier.lastUpdatedDate} ${supplier.lastUpdatedTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (language == AppLanguage.PASHTO) "سمول (Edit)" else "EDIT", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (language == AppLanguage.PASHTO) "ړنګول" else "DELETE", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SupplierDialog(
    supplier: Supplier?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, phone: String, address: String, notes: String) -> Unit
) {
    val isEdit = supplier != null
    var code by remember { mutableStateOf(supplier?.supplierIdCode ?: "") }
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var phone by remember { mutableStateOf(supplier?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }
    var notes by remember { mutableStateOf(supplier?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) {
                    if (language == AppLanguage.PASHTO) "د عرضه کوونکي سمول" else "EDIT SUPPLIER"
                } else {
                    if (language == AppLanguage.PASHTO) "نوی عرضه کوونکی زیاتول" else "ADD SUPPLIER"
                },
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
                if (!isEdit) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("${ShopStrings.supplierId(language)} (e.g. S-01)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("${ShopStrings.supplierName(language)} *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(ShopStrings.phoneNumber(language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(ShopStrings.address(language)) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

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
                    if (name.isNotBlank()) {
                        onSave(code, name, phone, address, notes)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
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
