package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ShopRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ShopRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShopRepository(database.appDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Amjid Mobile Center", appName)
    }

    @Test
    fun `test complete purchase and sale stock and profit workflow`() = runBlocking {
        // 1. Add Product: Samsung S24, Purchase: 60000, Selling: 65000, Stock: 5
        val productId = repository.addProduct(
            productIdCode = "001",
            name = "Samsung S24",
            purchasePrice = 60000.0,
            sellingPrice = 65000.0,
            stockQuantity = 5,
            customDate = "23/08/2026",
            customTime = "10:30 AM"
        )

        var product = repository.getProductById(productId)
        assertNotNull(product)
        assertEquals(5, product!!.stockQuantity)

        // 2. Record Purchase: Qty: 3 at 60000 AFN
        // Stock should increase from 5 to 8
        val purchaseResult = repository.recordPurchase(
            productId = productId,
            quantity = 3,
            purchasePrice = 60000.0,
            supplierName = "Kabul Electronics",
            customDate = "23/08/2026",
            customTime = "11:00 AM"
        )
        assertTrue(purchaseResult.isSuccess)

        product = repository.getProductById(productId)
        assertEquals(8, product!!.stockQuantity)

        // 3. Record Sale: Qty: 1 at 65000 AFN
        // Stock should decrease from 8 to 7
        // Profit = (65000 - 60000) * 1 = 5000 AFN
        val saleResult = repository.recordSale(
            productId = productId,
            quantity = 1,
            sellingPrice = 65000.0,
            customerName = "Ahmad Khan",
            customDate = "23/08/2026",
            customTime = "04:15 PM"
        )
        assertTrue(saleResult.isSuccess)

        product = repository.getProductById(productId)
        assertEquals(7, product!!.stockQuantity)

        // 4. Test Stock Protection: Cannot sell more than available stock!
        // Trying to sell 10 units when only 7 are in stock must fail
        val excessSaleResult = repository.recordSale(
            productId = productId,
            quantity = 10,
            sellingPrice = 65000.0,
            customerName = "Test"
        )
        assertTrue(excessSaleResult.isFailure)
        assertEquals(7, product.stockQuantity)
    }

    @Test
    fun `test user account creation and authentication`() = runBlocking {
        val createResult = repository.createAccount("admin", "1234")
        assertTrue(createResult.isSuccess)

        val authenticated = repository.authenticate("admin", "1234")
        assertNotNull(authenticated)
        assertEquals("admin", authenticated!!.username)

        val wrongAuth = repository.authenticate("admin", "wrongpass")
        assertEquals(null, wrongAuth)
    }

    @Test
    fun `test database quick backup and restore manager`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Ensure real DB has some data
        val realDb = AppDatabase.getDatabase(context)
        val realRepo = ShopRepository(realDb.appDao())
        val prodId = realRepo.addProduct(
            productIdCode = "BKP-001",
            name = "Backup Test Phone",
            purchasePrice = 10000.0,
            sellingPrice = 12000.0,
            stockQuantity = 4
        )
        assertNotNull(realRepo.getProductById(prodId))

        // Create quick backup
        val backupResult = com.example.util.DatabaseBackupManager.createQuickBackup(context)
        assertTrue(backupResult.isSuccess)
        val backupInfo = backupResult.getOrNull()
        assertNotNull(backupInfo)
        assertTrue(backupInfo!!.file.exists())
        assertTrue(backupInfo.sizeBytes > 0)

        // List backups
        val backupsList = com.example.util.DatabaseBackupManager.listQuickBackups(context)
        assertTrue(backupsList.isNotEmpty())
        assertTrue(backupsList.any { it.fileName == backupInfo.fileName })

        // Restore from backup file
        val restoreResult = com.example.util.DatabaseBackupManager.restoreDatabaseFromStream(
            context,
            java.io.FileInputStream(backupInfo.file)
        )
        assertTrue(restoreResult.isSuccess)

        // Verify data after restore
        val restoredDb = AppDatabase.getDatabase(context)
        realRepo.updateDao(restoredDb.appDao())
        val productAfterRestore = realRepo.getProductById(prodId)
        assertNotNull(productAfterRestore)
        assertEquals("Backup Test Phone", productAfterRestore?.name)

        // Clean up backup file
        val deleted = com.example.util.DatabaseBackupManager.deleteQuickBackup(backupInfo.file)
        assertTrue(deleted)
    }
}
