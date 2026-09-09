package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.UserAccount
import com.example.ui.BackupRestoreScreen
import com.example.ui.CreateAccountScreen
import com.example.ui.CustomersScreen
import com.example.ui.LoginScreen
import com.example.ui.MainMenuScreen
import com.example.ui.ProductsScreen
import com.example.ui.ProfitScreen
import com.example.ui.PurchasesScreen
import com.example.ui.ReportsScreen
import com.example.ui.SalesScreen
import com.example.ui.Screen
import com.example.ui.ShopViewModel
import com.example.ui.StockScreen
import com.example.ui.SuppliersScreen
import com.example.ui.TransactionHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.DateTimeUtils

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShopApp(
                        onTriggerBiometric = { account, onAuthenticated ->
                            showBiometricPrompt(account, onAuthenticated)
                        }
                    )
                }
            }
        }
    }

    private fun showBiometricPrompt(
        account: UserAccount,
        onSuccess: (UserAccount) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        this@MainActivity,
                        "ښه راغلاست / Welcome ${account.username}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    onSuccess(account)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If biometric hardware is absent in emulator or not enrolled, allow entering credentials
                    Toast.makeText(
                        this@MainActivity,
                        "$errString (Use username & password)",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@MainActivity,
                        "د ګوتې نښه ونه پېژندل شوه / Fingerprint not recognized",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Amjid Mobile Center")
            .setSubtitle("د ګوتې نښې تصدیق • Fingerprint Verification")
            .setDescription("حساب ته د ننوتلو لپاره خپله ګوته کیږدئ / Place your finger to log in")
            .setNegativeButtonText("لغوه کول / Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun ShopApp(
    viewModel: ShopViewModel = viewModel(),
    onTriggerBiometric: (UserAccount, (UserAccount) -> Unit) -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val language by viewModel.language.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()

    val products by viewModel.products.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val stockTransactions by viewModel.stockTransactions.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    val totalProducts by viewModel.totalProductCount.collectAsState()
    val totalStockUnits by viewModel.totalStockUnits.collectAsState()

    val productSearchQuery by viewModel.productSearchQuery.collectAsState()

    val localBackups by viewModel.localBackups.collectAsState()
    val isBackupLoading by viewModel.isBackupLoading.collectAsState()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsState()

    // Calculate today's dashboard stats
    val todayStart = remember { DateTimeUtils.getStartOfToday() }
    val todaySalesAmount = remember(sales) {
        sales.filter { it.timestamp >= todayStart }.sumOf { it.total }
    }
    val todayPurchasesAmount = remember(purchases) {
        purchases.filter { it.timestamp >= todayStart }.sumOf { it.total }
    }
    val todayProfitAmount = remember(sales) {
        sales.filter { it.timestamp >= todayStart }.sumOf { it.profit }
    }

    // Handle system back button
    BackHandler(enabled = currentScreen != Screen.LOGIN && currentScreen != Screen.MAIN_MENU) {
        viewModel.navigateBack()
    }

    when (currentScreen) {
        Screen.LOGIN -> {
            LoginScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                errorMessage = authError,
                onLogin = { u, p -> viewModel.login(u, p) },
                onCreateAccountClick = {
                    viewModel.clearAuthError()
                    viewModel.navigateTo(Screen.CREATE_ACCOUNT)
                },
                onFingerprintClick = {
                    viewModel.loginWithFingerprint { account ->
                        onTriggerBiometric(account) { authenticatedUser ->
                            viewModel.onBiometricAuthenticationSuccess(authenticatedUser)
                        }
                    }
                }
            )
        }

        Screen.CREATE_ACCOUNT -> {
            CreateAccountScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                errorMessage = authError,
                onCreateAccount = { u, p, c -> viewModel.createAccount(u, p, c) },
                onBackToLogin = {
                    viewModel.clearAuthError()
                    viewModel.navigateTo(Screen.LOGIN)
                }
            )
        }

        Screen.MAIN_MENU -> {
            MainMenuScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onLogout = { viewModel.logout() },
                totalProducts = totalProducts,
                totalStockUnits = totalStockUnits,
                todaySalesAmount = todaySalesAmount,
                todayPurchasesAmount = todayPurchasesAmount,
                todayProfitAmount = todayProfitAmount,
                infoMessage = infoMessage,
                onClearInfoMessage = { viewModel.clearInfoMessage() },
                onNavigateTo = { screen -> viewModel.navigateTo(screen) }
            )
        }

        Screen.PRODUCTS -> {
            ProductsScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                products = products,
                searchQuery = productSearchQuery,
                onSearchQueryChange = { viewModel.productSearchQuery.value = it },
                onAddProduct = { code, name, pPrice, sPrice, stock, date, time, onSuccess, onError ->
                    viewModel.addProduct(code, name, pPrice, sPrice, stock, date, time, onSuccess, onError)
                },
                onUpdateProduct = { product, name, pPrice, sPrice, stock, onSuccess, onError ->
                    viewModel.updateProduct(product, name, pPrice, sPrice, stock, onSuccess, onError)
                },
                onDeleteProduct = { product, onSuccess ->
                    viewModel.deleteProduct(product, onSuccess)
                },
                infoMessage = infoMessage,
                onClearInfoMessage = { viewModel.clearInfoMessage() }
            )
        }

        Screen.PURCHASES -> {
            PurchasesScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                purchases = purchases,
                products = products,
                suppliers = suppliers,
                onRecordPurchase = { pId, qty, price, sName, date, time, notes, onSuccess, onError ->
                    viewModel.recordPurchase(pId, qty, price, sName, date, time, notes, onSuccess, onError)
                },
                onCorrectPurchase = { purchase, qty, price, sName, date, time, notes, onSuccess, onError ->
                    viewModel.correctPurchase(purchase, qty, price, sName, date, time, notes, onSuccess, onError)
                },
                infoMessage = infoMessage,
                onClearInfoMessage = { viewModel.clearInfoMessage() }
            )
        }

        Screen.SALES -> {
            SalesScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                sales = sales,
                products = products,
                customers = customers,
                onRecordSale = { pId, qty, price, cName, date, time, notes, onSuccess, onError ->
                    viewModel.recordSale(pId, qty, price, cName, date, time, notes, onSuccess, onError)
                },
                onCorrectSale = { sale, qty, price, cName, date, time, notes, onSuccess, onError ->
                    viewModel.correctSale(sale, qty, price, cName, date, time, notes, onSuccess, onError)
                },
                infoMessage = infoMessage,
                onClearInfoMessage = { viewModel.clearInfoMessage() }
            )
        }

        Screen.STOCK -> {
            StockScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                products = products,
                purchases = purchases,
                sales = sales,
                onViewTransactions = { viewModel.navigateTo(Screen.TRANSACTIONS_HISTORY) }
            )
        }

        Screen.CUSTOMERS -> {
            CustomersScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                customers = customers,
                onAddCustomer = { code, name, phone, address, notes, onSuccess, onError ->
                    viewModel.addCustomer(code, name, phone, address, notes, onSuccess, onError)
                },
                onUpdateCustomer = { customer, name, phone, address, notes, onSuccess, onError ->
                    viewModel.updateCustomer(customer, name, phone, address, notes, onSuccess, onError)
                },
                onDeleteCustomer = { customer, onSuccess ->
                    viewModel.deleteCustomer(customer, onSuccess)
                },
                infoMessage = infoMessage,
                onClearInfoMessage = { viewModel.clearInfoMessage() }
            )
        }

        Screen.SUPPLIERS -> {
            SuppliersScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                suppliers = suppliers,
                onAddSupplier = { code, name, phone, address, notes, onSuccess, onError ->
                    viewModel.addSupplier(code, name, phone, address, notes, onSuccess, onError)
                },
                onUpdateSupplier = { supplier, name, phone, address, notes, onSuccess, onError ->
                    viewModel.updateSupplier(supplier, name, phone, address, notes, onSuccess, onError)
                },
                onDeleteSupplier = { supplier, onSuccess ->
                    viewModel.deleteSupplier(supplier, onSuccess)
                },
                infoMessage = infoMessage,
                onClearInfoMessage = { viewModel.clearInfoMessage() }
            )
        }

        Screen.PROFIT -> {
            ProfitScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                sales = sales
            )
        }

        Screen.REPORTS -> {
            ReportsScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                products = products,
                purchases = purchases,
                sales = sales,
                stockMovements = stockTransactions,
                viewModel = viewModel
            )
        }

        Screen.TRANSACTIONS_HISTORY -> {
            TransactionHistoryScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                purchases = purchases,
                sales = sales,
                stockTransactions = stockTransactions
            )
        }

        Screen.BACKUP_RESTORE -> {
            BackupRestoreScreen(
                language = language,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateBack() },
                viewModel = viewModel,
                localBackups = localBackups,
                isLoading = isBackupLoading,
                statusMessage = backupStatusMessage,
                totalProducts = totalProducts,
                totalSalesCount = sales.size,
                totalPurchasesCount = purchases.size
            )
        }
    }
}
