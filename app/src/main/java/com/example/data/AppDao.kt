package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // PRODUCTS
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR productIdCode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(stockQuantity), 0) FROM products")
    fun getTotalStockCount(): Flow<Int>

    // PURCHASES
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Long): Purchase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Delete
    suspend fun deletePurchase(purchase: Purchase)

    @Query("SELECT * FROM purchases WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp ORDER BY timestamp DESC")
    fun getPurchasesInRange(fromTimestamp: Long, toTimestamp: Long): Flow<List<Purchase>>

    // SALES
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("SELECT * FROM sales WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp ORDER BY timestamp DESC")
    fun getSalesInRange(fromTimestamp: Long, toTimestamp: Long): Flow<List<Sale>>

    // STOCK TRANSACTIONS
    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC")
    fun getAllStockTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY timestamp DESC")
    fun getStockTransactionsForProduct(productId: Long): Flow<List<StockTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(item: StockTransaction): Long

    // CUSTOMERS
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR customerIdCode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // SUPPLIERS
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): Supplier?

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR supplierIdCode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSuppliers(query: String): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    // USER ACCOUNTS
    @Query("SELECT * FROM user_accounts ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUserAccount(): UserAccount?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserAccountsCount(): Int

    @Query("SELECT * FROM user_accounts WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount): Long
}
