package com.example.data

import com.example.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ShopRepository(private var dao: AppDao) {

    fun updateDao(newDao: AppDao) {
        this.dao = newDao
    }

    val allProducts: Flow<List<Product>> get() = dao.getAllProducts()
    val allPurchases: Flow<List<Purchase>> get() = dao.getAllPurchases()
    val allSales: Flow<List<Sale>> get() = dao.getAllSales()
    val allStockTransactions: Flow<List<StockTransaction>> get() = dao.getAllStockTransactions()
    val allCustomers: Flow<List<Customer>> get() = dao.getAllCustomers()
    val allSuppliers: Flow<List<Supplier>> get() = dao.getAllSuppliers()
    val productCount: Flow<Int> get() = dao.getProductCount()
    val totalStockCount: Flow<Int> get() = dao.getTotalStockCount()

    fun searchProducts(query: String): Flow<List<Product>> = dao.searchProducts(query)
    fun searchCustomers(query: String): Flow<List<Customer>> = dao.searchCustomers(query)
    fun searchSuppliers(query: String): Flow<List<Supplier>> = dao.searchSuppliers(query)

    suspend fun getProductById(id: Long): Product? = withContext(Dispatchers.IO) {
        dao.getProductById(id)
    }

    suspend fun addProduct(
        productIdCode: String,
        name: String,
        purchasePrice: Double,
        sellingPrice: Double,
        stockQuantity: Int,
        customDate: String? = null,
        customTime: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val date = customDate?.ifBlank { null } ?: DateTimeUtils.currentDate()
        val time = customTime?.ifBlank { null } ?: DateTimeUtils.currentTime()
        val timestamp = DateTimeUtils.parseDateTime(date, time)

        val product = Product(
            productIdCode = productIdCode.trim().ifEmpty { "P-${System.currentTimeMillis() % 10000}" },
            name = name.trim(),
            purchasePrice = purchasePrice,
            sellingPrice = sellingPrice,
            stockQuantity = stockQuantity,
            dateAdded = date,
            timeAdded = time,
            lastUpdatedDate = date,
            lastUpdatedTime = time,
            createdAtTimestamp = timestamp,
            updatedAtTimestamp = timestamp
        )
        val id = dao.insertProduct(product)

        if (stockQuantity > 0) {
            dao.insertStockTransaction(
                StockTransaction(
                    productId = id,
                    productName = product.name,
                    type = "INITIAL_STOCK",
                    referenceId = null,
                    quantityChange = stockQuantity,
                    previousStock = 0,
                    newStock = stockQuantity,
                    date = date,
                    time = time,
                    timestamp = timestamp,
                    note = "Initial stock recorded"
                )
            )
        }
        id
    }

    suspend fun updateProduct(
        product: Product,
        newName: String,
        newPurchasePrice: Double,
        newSellingPrice: Double,
        newStockQuantity: Int
    ) = withContext(Dispatchers.IO) {
        val currentDate = DateTimeUtils.currentDate()
        val currentTime = DateTimeUtils.currentTime()
        val previousStock = product.stockQuantity

        val updated = product.copy(
            name = newName.trim(),
            purchasePrice = newPurchasePrice,
            sellingPrice = newSellingPrice,
            stockQuantity = newStockQuantity,
            lastUpdatedDate = currentDate,
            lastUpdatedTime = currentTime,
            updatedAtTimestamp = DateTimeUtils.currentTimestamp()
        )
        dao.updateProduct(updated)

        if (previousStock != newStockQuantity) {
            val delta = newStockQuantity - previousStock
            dao.insertStockTransaction(
                StockTransaction(
                    productId = product.id,
                    productName = updated.name,
                    type = "MANUAL_EDIT",
                    referenceId = null,
                    quantityChange = delta,
                    previousStock = previousStock,
                    newStock = newStockQuantity,
                    date = currentDate,
                    time = currentTime,
                    timestamp = DateTimeUtils.currentTimestamp(),
                    note = "Stock manually updated from $previousStock to $newStockQuantity"
                )
            )
        }
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        dao.deleteProduct(product)
    }

    // PURCHASES
    suspend fun recordPurchase(
        productId: Long,
        quantity: Int,
        purchasePrice: Double,
        supplierId: Long? = null,
        supplierName: String = "",
        customDate: String? = null,
        customTime: String? = null,
        notes: String = ""
    ): Result<Long> = withContext(Dispatchers.IO) {
        val product = dao.getProductById(productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Product not found"))

        val date = customDate?.ifBlank { null } ?: DateTimeUtils.currentDate()
        val time = customTime?.ifBlank { null } ?: DateTimeUtils.currentTime()
        val timestamp = DateTimeUtils.parseDateTime(date, time)

        val total = quantity * purchasePrice
        val purchaseIdCode = "PUR-${System.currentTimeMillis() % 100000}"

        val purchase = Purchase(
            purchaseIdCode = purchaseIdCode,
            productId = product.id,
            productName = product.name,
            supplierId = supplierId,
            supplierName = supplierName.trim(),
            quantity = quantity,
            purchasePrice = purchasePrice,
            total = total,
            date = date,
            time = time,
            timestamp = timestamp,
            notes = notes.trim()
        )
        val purchaseId = dao.insertPurchase(purchase)

        // Increase product stock automatically
        val previousStock = product.stockQuantity
        val newStock = previousStock + quantity
        val updatedProduct = product.copy(
            stockQuantity = newStock,
            purchasePrice = purchasePrice, // update current purchase price
            lastUpdatedDate = date,
            lastUpdatedTime = time,
            updatedAtTimestamp = timestamp
        )
        dao.updateProduct(updatedProduct)

        // Keep permanent stock transaction history
        dao.insertStockTransaction(
            StockTransaction(
                productId = product.id,
                productName = product.name,
                type = "PURCHASE",
                referenceId = purchaseId,
                quantityChange = quantity,
                previousStock = previousStock,
                newStock = newStock,
                date = date,
                time = time,
                timestamp = timestamp,
                note = "Purchase ID: $purchaseIdCode (${quantity}x at $purchasePrice AFN)"
            )
        )

        Result.success(purchaseId)
    }

    suspend fun correctPurchase(
        oldPurchase: Purchase,
        newQuantity: Int,
        newPurchasePrice: Double,
        newSupplierName: String,
        newDate: String,
        newTime: String,
        newNotes: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val product = dao.getProductById(oldPurchase.productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Associated product not found"))

        val deltaStock = newQuantity - oldPurchase.quantity
        val previousStock = product.stockQuantity
        val newStock = previousStock + deltaStock

        if (newStock < 0) {
            return@withContext Result.failure(IllegalStateException("Correction would cause negative stock ($newStock)"))
        }

        val timestamp = DateTimeUtils.parseDateTime(newDate, newTime)
        val updatedPurchase = oldPurchase.copy(
            quantity = newQuantity,
            purchasePrice = newPurchasePrice,
            total = newQuantity * newPurchasePrice,
            supplierName = newSupplierName.trim(),
            date = newDate,
            time = newTime,
            timestamp = timestamp,
            notes = newNotes.trim()
        )
        dao.updatePurchase(updatedPurchase)

        // Update product stock
        val updatedProduct = product.copy(
            stockQuantity = newStock,
            lastUpdatedDate = newDate,
            lastUpdatedTime = newTime,
            updatedAtTimestamp = timestamp
        )
        dao.updateProduct(updatedProduct)

        // Log correction in stock history
        if (deltaStock != 0) {
            dao.insertStockTransaction(
                StockTransaction(
                    productId = product.id,
                    productName = product.name,
                    type = "CORRECTION_PURCHASE",
                    referenceId = oldPurchase.id,
                    quantityChange = deltaStock,
                    previousStock = previousStock,
                    newStock = newStock,
                    date = newDate,
                    time = newTime,
                    timestamp = timestamp,
                    note = "Purchase ${oldPurchase.purchaseIdCode} corrected from ${oldPurchase.quantity} to $newQuantity"
                )
            )
        }

        Result.success(Unit)
    }

    // SALES
    suspend fun recordSale(
        productId: Long,
        quantity: Int,
        sellingPrice: Double,
        customerId: Long? = null,
        customerName: String = "",
        customDate: String? = null,
        customTime: String? = null,
        notes: String = ""
    ): Result<Long> = withContext(Dispatchers.IO) {
        val product = dao.getProductById(productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Product not found"))

        if (quantity <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        }

        if (product.stockQuantity < quantity) {
            return@withContext Result.failure(
                IllegalStateException("Cannot sell more than available stock! (Available: ${product.stockQuantity})")
            )
        }

        val date = customDate?.ifBlank { null } ?: DateTimeUtils.currentDate()
        val time = customTime?.ifBlank { null } ?: DateTimeUtils.currentTime()
        val timestamp = DateTimeUtils.parseDateTime(date, time)

        val total = quantity * sellingPrice
        val purchasePriceAtSale = product.purchasePrice
        val profit = (sellingPrice - purchasePriceAtSale) * quantity
        val saleIdCode = "SAL-${System.currentTimeMillis() % 100000}"

        val sale = Sale(
            saleIdCode = saleIdCode,
            productId = product.id,
            productName = product.name,
            customerId = customerId,
            customerName = customerName.trim(),
            quantity = quantity,
            sellingPrice = sellingPrice,
            purchasePriceAtSale = purchasePriceAtSale,
            total = total,
            profit = profit,
            date = date,
            time = time,
            timestamp = timestamp,
            notes = notes.trim()
        )
        val saleId = dao.insertSale(sale)

        // Decrease product stock automatically
        val previousStock = product.stockQuantity
        val newStock = previousStock - quantity
        val updatedProduct = product.copy(
            stockQuantity = newStock,
            sellingPrice = sellingPrice, // update recent selling price
            lastUpdatedDate = date,
            lastUpdatedTime = time,
            updatedAtTimestamp = timestamp
        )
        dao.updateProduct(updatedProduct)

        // Keep permanent stock transaction history
        dao.insertStockTransaction(
            StockTransaction(
                productId = product.id,
                productName = product.name,
                type = "SALE",
                referenceId = saleId,
                quantityChange = -quantity,
                previousStock = previousStock,
                newStock = newStock,
                date = date,
                time = time,
                timestamp = timestamp,
                note = "Sale ID: $saleIdCode (${quantity}x at $sellingPrice AFN, Profit: $profit AFN)"
            )
        )

        Result.success(saleId)
    }

    suspend fun correctSale(
        oldSale: Sale,
        newQuantity: Int,
        newSellingPrice: Double,
        newCustomerName: String,
        newDate: String,
        newTime: String,
        newNotes: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val product = dao.getProductById(oldSale.productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Associated product not found"))

        // deltaQty > 0 means selling more items -> requires additional stock
        val deltaQty = newQuantity - oldSale.quantity
        val previousStock = product.stockQuantity
        val newStock = previousStock - deltaQty

        if (newStock < 0) {
            return@withContext Result.failure(
                IllegalStateException("Cannot increase sale quantity by $deltaQty, only $previousStock items in stock!")
            )
        }

        val timestamp = DateTimeUtils.parseDateTime(newDate, newTime)
        val newTotal = newQuantity * newSellingPrice
        val newProfit = (newSellingPrice - oldSale.purchasePriceAtSale) * newQuantity

        val updatedSale = oldSale.copy(
            quantity = newQuantity,
            sellingPrice = newSellingPrice,
            total = newTotal,
            profit = newProfit,
            customerName = newCustomerName.trim(),
            date = newDate,
            time = newTime,
            timestamp = timestamp,
            notes = newNotes.trim()
        )
        dao.updateSale(updatedSale)

        // Update product stock
        val updatedProduct = product.copy(
            stockQuantity = newStock,
            lastUpdatedDate = newDate,
            lastUpdatedTime = newTime,
            updatedAtTimestamp = timestamp
        )
        dao.updateProduct(updatedProduct)

        // Log correction in stock history
        if (deltaQty != 0) {
            dao.insertStockTransaction(
                StockTransaction(
                    productId = product.id,
                    productName = product.name,
                    type = "CORRECTION_SALE",
                    referenceId = oldSale.id,
                    quantityChange = -deltaQty,
                    previousStock = previousStock,
                    newStock = newStock,
                    date = newDate,
                    time = newTime,
                    timestamp = timestamp,
                    note = "Sale ${oldSale.saleIdCode} corrected from ${oldSale.quantity} to $newQuantity"
                )
            )
        }

        Result.success(Unit)
    }

    // CUSTOMERS
    suspend fun addCustomer(
        customerIdCode: String,
        name: String,
        phoneNumber: String,
        address: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val date = DateTimeUtils.currentDate()
        val time = DateTimeUtils.currentTime()
        val customer = Customer(
            customerIdCode = customerIdCode.trim().ifEmpty { "C-${System.currentTimeMillis() % 10000}" },
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            address = address.trim(),
            notes = notes.trim(),
            dateAdded = date,
            timeAdded = time,
            lastUpdatedDate = date,
            lastUpdatedTime = time
        )
        dao.insertCustomer(customer)
    }

    suspend fun updateCustomer(
        customer: Customer,
        newName: String,
        newPhoneNumber: String,
        newAddress: String,
        newNotes: String
    ) = withContext(Dispatchers.IO) {
        val updated = customer.copy(
            name = newName.trim(),
            phoneNumber = newPhoneNumber.trim(),
            address = newAddress.trim(),
            notes = newNotes.trim(),
            lastUpdatedDate = DateTimeUtils.currentDate(),
            lastUpdatedTime = DateTimeUtils.currentTime()
        )
        dao.updateCustomer(updated)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        dao.deleteCustomer(customer)
    }

    // SUPPLIERS
    suspend fun addSupplier(
        supplierIdCode: String,
        name: String,
        phoneNumber: String,
        address: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val date = DateTimeUtils.currentDate()
        val time = DateTimeUtils.currentTime()
        val supplier = Supplier(
            supplierIdCode = supplierIdCode.trim().ifEmpty { "S-${System.currentTimeMillis() % 10000}" },
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            address = address.trim(),
            notes = notes.trim(),
            dateAdded = date,
            timeAdded = time,
            lastUpdatedDate = date,
            lastUpdatedTime = time
        )
        dao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(
        supplier: Supplier,
        newName: String,
        newPhoneNumber: String,
        newAddress: String,
        newNotes: String
    ) = withContext(Dispatchers.IO) {
        val updated = supplier.copy(
            name = newName.trim(),
            phoneNumber = newPhoneNumber.trim(),
            address = newAddress.trim(),
            notes = newNotes.trim(),
            lastUpdatedDate = DateTimeUtils.currentDate(),
            lastUpdatedTime = DateTimeUtils.currentTime()
        )
        dao.updateSupplier(updated)
    }

    suspend fun deleteSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        dao.deleteSupplier(supplier)
    }

    // USER ACCOUNTS
    suspend fun hasAnyAccount(): Boolean = withContext(Dispatchers.IO) {
        dao.getUserAccountsCount() > 0
    }

    suspend fun getPrimaryAccount(): UserAccount? = withContext(Dispatchers.IO) {
        dao.getFirstUserAccount()
    }

    suspend fun createAccount(username: String, password: String): Result<Long> = withContext(Dispatchers.IO) {
        val existing = dao.getUserByUsername(username.trim())
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Username already exists"))
        }

        val account = UserAccount(
            username = username.trim(),
            passwordHash = password, // Local offline storage
            isBiometricEnabled = true,
            createdDate = DateTimeUtils.currentDate(),
            createdTime = DateTimeUtils.currentTime()
        )
        val id = dao.insertUserAccount(account)
        Result.success(id)
    }

    suspend fun authenticate(username: String, password: String): UserAccount? = withContext(Dispatchers.IO) {
        val account = dao.getUserByUsername(username.trim())
        if (account != null && account.passwordHash == password) {
            account
        } else {
            null
        }
    }
}
