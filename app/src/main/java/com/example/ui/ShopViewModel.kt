package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Customer
import com.example.data.Product
import com.example.data.Purchase
import com.example.data.Sale
import com.example.data.ShopRepository
import com.example.data.StockTransaction
import com.example.data.Supplier
import com.example.data.UserAccount
import com.example.util.AppLanguage
import com.example.util.BackupFileInfo
import com.example.util.DatabaseBackupManager
import com.example.util.DateTimeUtils
import com.example.util.ShopStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

enum class Screen {
    LOGIN,
    CREATE_ACCOUNT,
    MAIN_MENU,
    PRODUCTS,
    PURCHASES,
    SALES,
    STOCK,
    CUSTOMERS,
    SUPPLIERS,
    PROFIT,
    REPORTS,
    TRANSACTIONS_HISTORY,
    BACKUP_RESTORE
}

enum class ReportPeriod {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    CUSTOM
}

data class FilteredReportStats(
    val totalSalesAmount: Double = 0.0,
    val totalSalesCount: Int = 0,
    val totalPurchasesAmount: Double = 0.0,
    val totalPurchasesCount: Int = 0,
    val totalProfitAmount: Double = 0.0,
    val stockUnitsSold: Int = 0,
    val stockUnitsPurchased: Int = 0,
    val salesList: List<Sale> = emptyList(),
    val purchasesList: List<Purchase> = emptyList(),
    val stockMovementsList: List<StockTransaction> = emptyList()
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ShopRepository(database.appDao())
        checkRegisteredAccount()
    }

    // Navigation
    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<Screen>()

    // Language
    private val _language = MutableStateFlow(AppLanguage.PASHTO)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.PASHTO) AppLanguage.ENGLISH else AppLanguage.PASHTO
        ShopStrings.currentLanguage = _language.value
    }

    // User & Auth
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _hasRegisteredAccount = MutableStateFlow(false)
    val hasRegisteredAccount: StateFlow<Boolean> = _hasRegisteredAccount.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun navigateTo(screen: Screen) {
        _screenHistory.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (_screenHistory.isNotEmpty()) {
            _currentScreen.value = _screenHistory.removeAt(_screenHistory.size - 1)
        } else {
            if (_currentScreen.value != Screen.MAIN_MENU && _currentUser.value != null) {
                _currentScreen.value = Screen.MAIN_MENU
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _screenHistory.clear()
        _currentScreen.value = Screen.LOGIN
    }

    private fun checkRegisteredAccount() {
        viewModelScope.launch {
            val hasAccount = repository.hasAnyAccount()
            _hasRegisteredAccount.value = hasAccount
        }
    }

    fun login(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            _authError.value = if (_language.value == AppLanguage.PASHTO)
                "مهرباني وکړئ کارن نوم او پټنوم ولیکئ"
            else
                "Please enter username and password"
            return false
        }

        var success = false
        viewModelScope.launch {
            val user = repository.authenticate(username, password)
            if (user != null) {
                _currentUser.value = user
                _authError.value = null
                _screenHistory.clear()
                _currentScreen.value = Screen.MAIN_MENU
                success = true
            } else {
                _authError.value = if (_language.value == AppLanguage.PASHTO)
                    "کارن نوم یا پټنوم ناسم دی"
                else
                    "Invalid username or password"
            }
        }
        return success
    }

    fun loginWithFingerprint(onReadyForBiometric: (UserAccount) -> Unit) {
        viewModelScope.launch {
            val account = repository.getPrimaryAccount()
            if (account == null) {
                _authError.value = if (_language.value == AppLanguage.PASHTO)
                    "هیڅ حساب شتون نلري، مهرباني وکړئ لومړی حساب جوړ کړئ"
                else
                    "No account found. Please create an account first."
            } else {
                _authError.value = null
                onReadyForBiometric(account)
            }
        }
    }

    fun onBiometricAuthenticationSuccess(account: UserAccount) {
        _currentUser.value = account
        _authError.value = null
        _screenHistory.clear()
        _currentScreen.value = Screen.MAIN_MENU
    }

    fun createAccount(username: String, password: String, confirmPass: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            _authError.value = if (_language.value == AppLanguage.PASHTO)
                "مهرباني وکړئ ټول معلومات بشپړ کړئ"
            else
                "Please fill in all fields"
            return false
        }
        if (password != confirmPass) {
            _authError.value = if (_language.value == AppLanguage.PASHTO)
                "پټنوم او د هغې تایید سره سمون نه خوري"
            else
                "Passwords do not match"
            return false
        }
        if (password.length < 4) {
            _authError.value = if (_language.value == AppLanguage.PASHTO)
                "پټنوم باید لږترلږه ۴ توري وي"
            else
                "Password must be at least 4 characters"
            return false
        }

        viewModelScope.launch {
            val result = repository.createAccount(username, password)
            if (result.isSuccess) {
                _hasRegisteredAccount.value = true
                val createdUser = repository.authenticate(username, password)
                _currentUser.value = createdUser
                _authError.value = null
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                    "حساب په بریالیتوب سره جوړ شو"
                else
                    "Account created successfully"
                _screenHistory.clear()
                _currentScreen.value = Screen.MAIN_MENU
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Error creating account"
            }
        }
        return true
    }

    // Database reload trigger for instant reactive updates upon DB restore
    private val _databaseReloadTrigger = MutableStateFlow(0)

    // Data streams
    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = _databaseReloadTrigger
        .flatMapLatest { repository.allProducts }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val purchases: StateFlow<List<Purchase>> = _databaseReloadTrigger
        .flatMapLatest { repository.allPurchases }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val sales: StateFlow<List<Sale>> = _databaseReloadTrigger
        .flatMapLatest { repository.allSales }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val stockTransactions: StateFlow<List<StockTransaction>> = _databaseReloadTrigger
        .flatMapLatest { repository.allStockTransactions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = _databaseReloadTrigger
        .flatMapLatest { repository.allCustomers }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val suppliers: StateFlow<List<Supplier>> = _databaseReloadTrigger
        .flatMapLatest { repository.allSuppliers }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalProductCount: StateFlow<Int> = _databaseReloadTrigger
        .flatMapLatest { repository.productCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalStockUnits: StateFlow<Int> = _databaseReloadTrigger
        .flatMapLatest { repository.totalStockCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Search filters
    val productSearchQuery = MutableStateFlow("")
    val customerSearchQuery = MutableStateFlow("")
    val supplierSearchQuery = MutableStateFlow("")

    // PRODUCT ACTIONS
    fun addProduct(
        code: String,
        name: String,
        purchasePrice: Double,
        sellingPrice: Double,
        stock: Int,
        date: String? = null,
        time: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError(if (_language.value == AppLanguage.PASHTO) "د توکي نوم حتمي دی" else "Product name is required")
            return
        }
        if (purchasePrice < 0 || sellingPrice < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "بیه نشي کولی منفي وي" else "Price cannot be negative")
            return
        }
        if (stock < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "شمېر نشي کولی منفي وي" else "Stock cannot be negative")
            return
        }

        viewModelScope.launch {
            try {
                repository.addProduct(code, name, purchasePrice, sellingPrice, stock, date, time)
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                    "توکی ثبت شو"
                else
                    "Product added successfully"
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add product")
            }
        }
    }

    fun updateProduct(
        product: Product,
        name: String,
        purchasePrice: Double,
        sellingPrice: Double,
        stock: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError(if (_language.value == AppLanguage.PASHTO) "د توکي نوم حتمي دی" else "Product name is required")
            return
        }
        if (purchasePrice < 0 || sellingPrice < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "بیه نشي کولی منفي وي" else "Price cannot be negative")
            return
        }
        if (stock < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "شمېر نشي کولی منفي وي" else "Stock cannot be negative")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateProduct(product, name, purchasePrice, sellingPrice, stock)
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                    "توکی سم شو"
                else
                    "Product updated successfully"
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update product")
            }
        }
    }

    fun deleteProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                "توکی ړنګ شو"
            else
                "Product deleted"
            onSuccess()
        }
    }

    // PURCHASES ACTIONS
    fun recordPurchase(
        productId: Long,
        quantity: Int,
        purchasePrice: Double,
        supplierName: String,
        date: String? = null,
        time: String? = null,
        notes: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (quantity <= 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "شمېر باید له صفر څخه ډېر وي" else "Quantity must be greater than 0")
            return
        }
        if (purchasePrice < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "بیه نشي کولی منفي وي" else "Price cannot be negative")
            return
        }

        viewModelScope.launch {
            val result = repository.recordPurchase(
                productId = productId,
                quantity = quantity,
                purchasePrice = purchasePrice,
                supplierName = supplierName,
                customDate = date,
                customTime = time,
                notes = notes
            )
            if (result.isSuccess) {
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                    "اخیستل په بریالیتوب سره ثبت شول"
                else
                    "Purchase saved successfully"
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to save purchase")
            }
        }
    }

    fun correctPurchase(
        purchase: Purchase,
        newQuantity: Int,
        newPurchasePrice: Double,
        newSupplierName: String,
        newDate: String,
        newTime: String,
        newNotes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (newQuantity <= 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "شمېر باید له صفر څخه ډېر وي" else "Quantity must be greater than 0")
            return
        }
        if (newPurchasePrice < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "بیه نشي کولی منفي وي" else "Price cannot be negative")
            return
        }

        viewModelScope.launch {
            val result = repository.correctPurchase(
                oldPurchase = purchase,
                newQuantity = newQuantity,
                newPurchasePrice = newPurchasePrice,
                newSupplierName = newSupplierName,
                newDate = newDate,
                newTime = newTime,
                newNotes = newNotes
            )
            if (result.isSuccess) {
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                    "د اخیستلو ریکارډ اصلاح شو او موجودي تازه شوه"
                else
                    "Purchase corrected and stock updated successfully"
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to correct purchase")
            }
        }
    }

    // SALES ACTIONS
    fun recordSale(
        productId: Long,
        quantity: Int,
        sellingPrice: Double,
        customerName: String,
        date: String? = null,
        time: String? = null,
        notes: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (quantity <= 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "شمېر باید له صفر څخه ډېر وي" else "Quantity must be greater than 0")
            return
        }
        if (sellingPrice < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "بیه نشي کولی منفي وي" else "Price cannot be negative")
            return
        }

        viewModelScope.launch {
            val result = repository.recordSale(
                productId = productId,
                quantity = quantity,
                sellingPrice = sellingPrice,
                customerName = customerName,
                customDate = date,
                customTime = time,
                notes = notes
            )
            if (result.isSuccess) {
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                    "خرڅلاو ثبت شو او موجودي کمه شوه"
                else
                    "Sale recorded and stock updated"
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to record sale")
            }
        }
    }

    fun correctSale(
        sale: Sale,
        newQuantity: Int,
        newSellingPrice: Double,
        newCustomerName: String,
        newDate: String,
        newTime: String,
        newNotes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (newQuantity <= 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "شمېر باید له صفر څخه ډېر وي" else "Quantity must be greater than 0")
            return
        }
        if (newSellingPrice < 0) {
            onError(if (_language.value == AppLanguage.PASHTO) "بیه نشي کولی منفي وي" else "Price cannot be negative")
            return
        }

        viewModelScope.launch {
            val result = repository.correctSale(
                oldSale = sale,
                newQuantity = newQuantity,
                newSellingPrice = newSellingPrice,
                newCustomerName = newCustomerName,
                newDate = newDate,
                newTime = newTime,
                newNotes = newNotes
            )
            if (result.isSuccess) {
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO)
                    "د خرڅلاو ریکارډ اصلاح شو او ګټه او موجودي بیا محاسبه شول"
                else
                    "Sale corrected, stock and profit recalculated"
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to correct sale")
            }
        }
    }

    // CUSTOMERS ACTIONS
    fun addCustomer(
        code: String,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError(if (_language.value == AppLanguage.PASHTO) "د پیرودونکي نوم حتمي دی" else "Customer name is required")
            return
        }

        viewModelScope.launch {
            try {
                repository.addCustomer(code, name, phone, address, notes)
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO) "پیرودونکی ثبت شو" else "Customer added"
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add customer")
            }
        }
    }

    fun updateCustomer(
        customer: Customer,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError(if (_language.value == AppLanguage.PASHTO) "د پیرودونکي نوم حتمي دی" else "Customer name is required")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCustomer(customer, name, phone, address, notes)
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO) "پیرودونکی سم شو" else "Customer updated"
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update customer")
            }
        }
    }

    fun deleteCustomer(customer: Customer, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _infoMessage.value = if (_language.value == AppLanguage.PASHTO) "پیرودونکی ړنګ شو" else "Customer deleted"
            onSuccess()
        }
    }

    // SUPPLIERS ACTIONS
    fun addSupplier(
        code: String,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError(if (_language.value == AppLanguage.PASHTO) "د عرضه کوونکي نوم حتمي دی" else "Supplier name is required")
            return
        }

        viewModelScope.launch {
            try {
                repository.addSupplier(code, name, phone, address, notes)
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO) "عرضه کوونکی ثبت شو" else "Supplier added"
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add supplier")
            }
        }
    }

    fun updateSupplier(
        supplier: Supplier,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError(if (_language.value == AppLanguage.PASHTO) "د عرضه کوونکي نوم حتمي دی" else "Supplier name is required")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateSupplier(supplier, name, phone, address, notes)
                _infoMessage.value = if (_language.value == AppLanguage.PASHTO) "عرضه کوونکی سم شو" else "Supplier updated"
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update supplier")
            }
        }
    }

    fun deleteSupplier(supplier: Supplier, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            _infoMessage.value = if (_language.value == AppLanguage.PASHTO) "عرضه کوونکی ړنګ شو" else "Supplier deleted"
            onSuccess()
        }
    }

    // REPORT STATS CALCULATION
    fun calculateStatsForRange(
        period: ReportPeriod,
        customFrom: Long = 0L,
        customTo: Long = Long.MAX_VALUE,
        allSales: List<Sale>,
        allPurchases: List<Purchase>,
        allMovements: List<StockTransaction>
    ): FilteredReportStats {
        val (start, end) = when (period) {
            ReportPeriod.TODAY -> Pair(DateTimeUtils.getStartOfToday(), Long.MAX_VALUE)
            ReportPeriod.YESTERDAY -> Pair(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getEndOfYesterday())
            ReportPeriod.THIS_WEEK -> Pair(DateTimeUtils.getStartOfWeek(), Long.MAX_VALUE)
            ReportPeriod.THIS_MONTH -> Pair(DateTimeUtils.getStartOfMonth(), Long.MAX_VALUE)
            ReportPeriod.THIS_YEAR -> Pair(DateTimeUtils.getStartOfYear(), Long.MAX_VALUE)
            ReportPeriod.CUSTOM -> Pair(customFrom, customTo)
        }

        val filteredSales = allSales.filter { it.timestamp in start..end }
        val filteredPurchases = allPurchases.filter { it.timestamp in start..end }
        val filteredMovements = allMovements.filter { it.timestamp in start..end }

        val totalSalesAmount = filteredSales.sumOf { it.total }
        val totalPurchasesAmount = filteredPurchases.sumOf { it.total }
        val totalProfit = filteredSales.sumOf { it.profit }
        val stockSold = filteredSales.sumOf { it.quantity }
        val stockPurchased = filteredPurchases.sumOf { it.quantity }

        return FilteredReportStats(
            totalSalesAmount = totalSalesAmount,
            totalSalesCount = filteredSales.size,
            totalPurchasesAmount = totalPurchasesAmount,
            totalPurchasesCount = filteredPurchases.size,
            totalProfitAmount = totalProfit,
            stockUnitsSold = stockSold,
            stockUnitsPurchased = stockPurchased,
            salesList = filteredSales,
            purchasesList = filteredPurchases,
            stockMovementsList = filteredMovements
        )
    }

    // ==========================================
    // BACKUP & RESTORE
    // ==========================================
    private val _localBackups = MutableStateFlow<List<BackupFileInfo>>(emptyList())
    val localBackups: StateFlow<List<BackupFileInfo>> = _localBackups.asStateFlow()

    private val _isBackupLoading = MutableStateFlow(false)
    val isBackupLoading: StateFlow<Boolean> = _isBackupLoading.asStateFlow()

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    fun clearBackupStatusMessage() {
        _backupStatusMessage.value = null
    }

    fun loadLocalBackups() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = DatabaseBackupManager.listQuickBackups(getApplication())
            _localBackups.value = list
        }
    }

    fun createQuickBackup(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val result = withContext(Dispatchers.IO) {
                DatabaseBackupManager.createQuickBackup(getApplication())
            }
            _isBackupLoading.value = false
            if (result.isSuccess) {
                loadLocalBackups()
                val fileName = result.getOrNull()?.fileName ?: "backup.db"
                val msg = if (_language.value == AppLanguage.PASHTO)
                    "چټک بیک اپ جوړ شو: $fileName"
                else
                    "Quick backup created: $fileName"
                _infoMessage.value = msg
                _backupStatusMessage.value = msg
                onComplete(true)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Backup failed"
                _backupStatusMessage.value = err
                onComplete(false)
            }
        }
    }

    fun exportDatabaseToUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val result = withContext(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        DatabaseBackupManager.exportDatabaseToStream(context, outputStream)
                    } ?: Result.failure(IllegalStateException("Cannot open output stream for URI"))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            _isBackupLoading.value = false
            if (result.isSuccess) {
                val msg = if (_language.value == AppLanguage.PASHTO)
                    "ډیټابیس په بریا سره فایل ته صادر شو"
                else
                    "Database exported successfully to file"
                _infoMessage.value = msg
                _backupStatusMessage.value = msg
                onComplete(true)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Export failed"
                _backupStatusMessage.value = err
                onComplete(false)
            }
        }
    }

    fun restoreDatabaseFromUri(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val result = withContext(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        DatabaseBackupManager.restoreDatabaseFromStream(context, inputStream)
                    } ?: Result.failure(IllegalStateException("Cannot open input stream for URI"))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            _isBackupLoading.value = false
            if (result.isSuccess) {
                onDatabaseRestored()
                val msg = if (_language.value == AppLanguage.PASHTO)
                    "ډیټابیس په بریا سره بیرته ورغول شو!"
                else
                    "Database restored successfully!"
                _infoMessage.value = msg
                _backupStatusMessage.value = msg
                onComplete(true)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Restore failed"
                val errorMsg = if (_language.value == AppLanguage.PASHTO)
                    "د بیارغونې تېروتنه: $err"
                else
                    "Restore error: $err"
                _backupStatusMessage.value = errorMsg
                onComplete(false)
            }
        }
    }

    fun restoreFromLocalBackup(file: File, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val result = withContext(Dispatchers.IO) {
                try {
                    FileInputStream(file).use { inputStream ->
                        DatabaseBackupManager.restoreDatabaseFromStream(getApplication(), inputStream)
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            _isBackupLoading.value = false
            if (result.isSuccess) {
                onDatabaseRestored()
                val msg = if (_language.value == AppLanguage.PASHTO)
                    "ډیټابیس له ځايي بیک اپ څخه بیرته ورغول شو!"
                else
                    "Database restored from local backup successfully!"
                _infoMessage.value = msg
                _backupStatusMessage.value = msg
                onComplete(true)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Restore failed"
                _backupStatusMessage.value = err
                onComplete(false)
            }
        }
    }

    fun deleteLocalBackup(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseBackupManager.deleteQuickBackup(file)
            loadLocalBackups()
            val msg = if (_language.value == AppLanguage.PASHTO) "بیک اپ فایل ړنګ شو" else "Backup file deleted"
            _backupStatusMessage.value = msg
        }
    }

    private suspend fun onDatabaseRestored() {
        withContext(Dispatchers.IO) {
            val newDb = AppDatabase.getDatabase(getApplication())
            repository.updateDao(newDb.appDao())
        }
        _databaseReloadTrigger.value++
        checkRegisteredAccount()
        loadLocalBackups()
    }

    fun getDatabaseSize(): String {
        return DatabaseBackupManager.getFormattedDatabaseSize(getApplication())
    }
}
