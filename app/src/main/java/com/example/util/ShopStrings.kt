package com.example.util

enum class AppLanguage {
    PASHTO,
    ENGLISH
}

object ShopStrings {
    // Current selected language
    var currentLanguage: AppLanguage = AppLanguage.PASHTO

    fun appName(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د امجد ګرځنده مرکز"
        AppLanguage.ENGLISH -> "Amjid Mobile Center"
    }

    fun appSubtitle(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ګرځنده پلورنځي مدیریت"
        AppLanguage.ENGLISH -> "Mobile Shop Management"
    }

    // Main Sections
    fun products(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "توکي"
        AppLanguage.ENGLISH -> "Products"
    }

    fun purchases(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "اخیستل"
        AppLanguage.ENGLISH -> "Purchases"
    }

    fun sales(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "خرڅول"
        AppLanguage.ENGLISH -> "Sales"
    }

    fun stock(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "موجود توکي"
        AppLanguage.ENGLISH -> "Stock"
    }

    fun customers(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "مشتریان"
        AppLanguage.ENGLISH -> "Customers"
    }

    fun suppliers(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "پلورونکي"
        AppLanguage.ENGLISH -> "Suppliers"
    }

    fun profit(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ګټه"
        AppLanguage.ENGLISH -> "Profit"
    }

    fun reports(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "راپورونه"
        AppLanguage.ENGLISH -> "Reports"
    }

    fun transactionHistory(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د معاملو تاریخچه"
        AppLanguage.ENGLISH -> "Transaction History"
    }

    // Common Actions
    fun back(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "← شا ته"
        AppLanguage.ENGLISH -> "← BACK"
    }

    fun save(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "خوندي کول"
        AppLanguage.ENGLISH -> "SAVE"
    }

    fun cancel(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "لغوه کول"
        AppLanguage.ENGLISH -> "CANCEL"
    }

    fun edit(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "سمول"
        AppLanguage.ENGLISH -> "EDIT"
    }

    fun delete(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ړنګول"
        AppLanguage.ENGLISH -> "DELETE"
    }

    fun add(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "زیاتول"
        AppLanguage.ENGLISH -> "ADD"
    }

    fun search(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "لټون..."
        AppLanguage.ENGLISH -> "Search..."
    }

    fun login(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ننوتل"
        AppLanguage.ENGLISH -> "LOGIN"
    }

    fun createAccount(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "نوې حساب جوړول"
        AppLanguage.ENGLISH -> "CREATE NEW ACCOUNT"
    }

    fun fingerprintLogin(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ګوتې نښان سره ننوتل"
        AppLanguage.ENGLISH -> "LOGIN WITH FINGERPRINT"
    }

    fun username(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "کارن نوم"
        AppLanguage.ENGLISH -> "Username"
    }

    fun password(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "پټنوم"
        AppLanguage.ENGLISH -> "Password"
    }

    fun confirmPassword(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "پټنوم تایید کړئ"
        AppLanguage.ENGLISH -> "Confirm Password"
    }

    fun backToLogin(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "بېرته ننوتلو ته"
        AppLanguage.ENGLISH -> "BACK TO LOGIN"
    }

    // Product labels
    fun productId(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د توکي شمېره (ID)"
        AppLanguage.ENGLISH -> "Product ID"
    }

    fun productName(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د توکي نوم"
        AppLanguage.ENGLISH -> "Product Name"
    }

    fun purchasePrice(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د اخیستلو بیه"
        AppLanguage.ENGLISH -> "Purchase Price"
    }

    fun sellingPrice(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د خرڅلاو بیه"
        AppLanguage.ENGLISH -> "Selling Price"
    }

    fun stockQuantity(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "موجوده شمېره"
        AppLanguage.ENGLISH -> "Stock Quantity"
    }

    fun dateAdded(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ثبت نېټه"
        AppLanguage.ENGLISH -> "Date Added"
    }

    fun timeAdded(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ثبت وخت"
        AppLanguage.ENGLISH -> "Time Added"
    }

    fun lastUpdatedDate(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "وروستۍ تازه نېټه"
        AppLanguage.ENGLISH -> "Last Updated Date"
    }

    fun lastUpdatedTime(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "وروستی تازه وخت"
        AppLanguage.ENGLISH -> "Last Updated Time"
    }

    // Dashboard Cards
    fun totalProducts(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټول توکي"
        AppLanguage.ENGLISH -> "Total Products"
    }

    fun totalStock(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټول موجود توکي"
        AppLanguage.ENGLISH -> "Total Stock"
    }

    fun todaySales(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د نن خرڅلاو"
        AppLanguage.ENGLISH -> "Today's Sales"
    }

    fun todayPurchases(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د نن اخیستل"
        AppLanguage.ENGLISH -> "Today's Purchases"
    }

    fun todayProfit(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د نن ګټه"
        AppLanguage.ENGLISH -> "Today's Profit"
    }

    // Periods
    fun today(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "نن"
        AppLanguage.ENGLISH -> "Today"
    }

    fun yesterday(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "پرون"
        AppLanguage.ENGLISH -> "Yesterday"
    }

    fun thisWeek(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "دا اونۍ"
        AppLanguage.ENGLISH -> "This Week"
    }

    fun thisMonth(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "دا میاشت"
        AppLanguage.ENGLISH -> "This Month"
    }

    fun thisYear(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "سږ کال"
        AppLanguage.ENGLISH -> "This Year"
    }

    fun customRange(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټاکلې موده"
        AppLanguage.ENGLISH -> "Custom Range"
    }

    fun total(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټولټال"
        AppLanguage.ENGLISH -> "Total"
    }

    fun quantity(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "شمېر / تعداد"
        AppLanguage.ENGLISH -> "Quantity"
    }

    fun notes(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "یادښتونه"
        AppLanguage.ENGLISH -> "Notes"
    }

    fun phone(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ټیلیفون شمېره"
        AppLanguage.ENGLISH -> "Phone Number"
    }

    fun phoneNumber(lang: AppLanguage = currentLanguage): String = phone(lang)

    fun address(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "پته"
        AppLanguage.ENGLISH -> "Address"
    }

    fun customerId(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د پیرودونکي شمېره"
        AppLanguage.ENGLISH -> "Customer ID"
    }

    fun customerName(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د پیرودونکي نوم"
        AppLanguage.ENGLISH -> "Customer Name"
    }

    fun supplierId(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د عرضه کوونکي شمېره"
        AppLanguage.ENGLISH -> "Supplier ID"
    }

    fun supplierName(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د عرضه کوونکي نوم"
        AppLanguage.ENGLISH -> "Supplier Name"
    }

    fun price(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "بیه"
        AppLanguage.ENGLISH -> "Price"
    }

    fun dailyReport(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ورځنی راپور"
        AppLanguage.ENGLISH -> "Daily Report"
    }

    fun weeklyReport(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "اونیز راپور"
        AppLanguage.ENGLISH -> "Weekly Report"
    }

    fun monthlyReport(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "میاشتنی راپور"
        AppLanguage.ENGLISH -> "Monthly Report"
    }

    fun yearlyReport(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "کلنی راپور"
        AppLanguage.ENGLISH -> "Yearly Report"
    }

    fun customReport(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټاکلې موده"
        AppLanguage.ENGLISH -> "Custom Report"
    }

    fun totalSales(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټول خرڅلاو"
        AppLanguage.ENGLISH -> "Total Sales"
    }

    fun totalPurchases(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټول اخیستل"
        AppLanguage.ENGLISH -> "Total Purchases"
    }

    fun totalProfit(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټوله ګټه"
        AppLanguage.ENGLISH -> "Total Profit"
    }

    fun stockActivity(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د زېرمې خوځښت"
        AppLanguage.ENGLISH -> "Stock Activity"
    }

    fun currentStock(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "اوسنۍ موجودي"
        AppLanguage.ENGLISH -> "Current Stock"
    }

    fun totalPurchased(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټول اخیستل شوي"
        AppLanguage.ENGLISH -> "Total Purchased"
    }

    fun totalSold(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ټول پلورل شوي"
        AppLanguage.ENGLISH -> "Total Sold"
    }

    fun remainingStock(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "پاتې موجودي"
        AppLanguage.ENGLISH -> "Remaining Stock"
    }

    fun currencySymbol(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "افغانۍ"
        AppLanguage.ENGLISH -> "AFN"
    }

    // BACKUP & RESTORE
    fun backupAndRestore(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د معلوماتو زېرمه او بیارغونه"
        AppLanguage.ENGLISH -> "Backup & Restore"
    }

    fun backupSubtitle(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د خپلو معلوماتو د خوندیتوب لپاره د SQLite فایل صادر او وارد کړئ"
        AppLanguage.ENGLISH -> "Export & import SQLite database file for data safety"
    }

    fun exportDatabase(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ډیټابیس فایل صادرول (بیک اپ)"
        AppLanguage.ENGLISH -> "Export Database to File"
    }

    fun exportDatabaseDesc(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د SQLite ډیټابیس بشپړ فایل (.db) د موبایل حافظې ته خوندي کړئ"
        AppLanguage.ENGLISH -> "Save the complete SQLite (.db) database file to device storage"
    }

    fun restoreDatabase(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "له فایل څخه ډیټابیس بیارغول"
        AppLanguage.ENGLISH -> "Restore Database from File"
    }

    fun restoreDatabaseDesc(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "له موبایل څخه یو پخوانی (.db) بیک اپ فایل غوره او فعال کړئ"
        AppLanguage.ENGLISH -> "Select a previously exported (.db) file to restore your data"
    }

    fun quickBackup(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "چټک ځايي بیک اپ"
        AppLanguage.ENGLISH -> "Quick Local Backup"
    }

    fun quickBackupDesc(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "په یو کښېکاږلو سره سمدستي د اپلیکیشن په حافظه کې بیک اپ ثبت کړئ"
        AppLanguage.ENGLISH -> "Instantly create a snapshot backup in the local app storage"
    }

    fun backupHistory(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د خوندي شویو بیک اپونو نوملړ"
        AppLanguage.ENGLISH -> "Local Backup Snapshots"
    }

    fun noBackupsYet(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "تر اوسه هیڅ ځايي بیک اپ نه دی جوړ شوی"
        AppLanguage.ENGLISH -> "No local backup snapshots found"
    }

    fun confirmRestoreTitle(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د معلوماتو بیارغونه تایید کړئ؟"
        AppLanguage.ENGLISH -> "Confirm Database Restore?"
    }

    fun confirmRestoreMessage(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "پاملرنه: د دې فایل په بیارغولو سره به اوسني معلومات له منځه لاړ شي او د دې بیک اپ معلومات به ځای پر ځای شي. ایا ډاډه یاست؟"
        AppLanguage.ENGLISH -> "Warning: Restoring will replace all current data with the contents of this backup file. Are you sure you want to proceed?"
    }

    fun confirmDeleteBackupTitle(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د بیک اپ فایل ړنګول؟"
        AppLanguage.ENGLISH -> "Delete Backup File?"
    }

    fun confirmDeleteBackupMessage(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ایا ډاډه یاست چې غواړئ دا بیک اپ فایل د تل لپاره ړنګ کړئ؟"
        AppLanguage.ENGLISH -> "Are you sure you want to permanently delete this backup snapshot?"
    }

    fun databaseStatus(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د اوسني ډیټابیس وضعیت"
        AppLanguage.ENGLISH -> "Current Database Status"
    }

    fun currentDatabaseSize(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ډیټابیس حجم:"
        AppLanguage.ENGLISH -> "Database Size:"
    }

    fun shareBackup(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "شریکول (واټساپ، ډرایو، برېښنالیک)"
        AppLanguage.ENGLISH -> "Share via Apps"
    }

    fun backupSuccess(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "بیک اپ په بریا سره خوندي شو"
        AppLanguage.ENGLISH -> "Backup created successfully"
    }

    fun restoreSuccess(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "ډیټابیس په بریا سره بیرته ورغول شو!"
        AppLanguage.ENGLISH -> "Database restored successfully!"
    }

    fun restoreFailed(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "د ډیټابیس بیارغونه ناکامه شوه: فایل ناسم دی"
        AppLanguage.ENGLISH -> "Restore failed: Invalid or corrupted backup file"
    }

    fun backupDeleted(lang: AppLanguage = currentLanguage): String = when (lang) {
        AppLanguage.PASHTO -> "بیک اپ فایل ړنګ شو"
        AppLanguage.ENGLISH -> "Backup file deleted"
    }
}
