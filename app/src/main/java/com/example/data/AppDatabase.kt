package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Product::class,
        Customer::class,
        Supplier::class,
        Purchase::class,
        Sale::class,
        StockTransaction::class,
        UserAccount::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        const val DATABASE_NAME = "amjid_mobile_center.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                try {
                    if (INSTANCE?.isOpen == true) {
                        INSTANCE?.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    INSTANCE = null
                }
            }
        }

        fun checkpointDatabase(context: Context) {
            try {
                val db = getDatabase(context)
                if (db.isOpen) {
                    db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                        cursor.moveToFirst()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
