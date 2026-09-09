package com.example.util

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.FileProvider
import com.example.data.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupFileInfo(
    val file: File,
    val fileName: String,
    val formattedDate: String,
    val formattedSize: String,
    val sizeBytes: Long,
    val timestamp: Long
)

object DatabaseBackupManager {

    private const val SQLITE_HEADER_STRING = "SQLite format 3\u0000"
    private const val BACKUPS_FOLDER_NAME = "database_backups"

    /**
     * Get the directory where local backups are stored.
     */
    fun getBackupsDirectory(context: Context): File {
        val dir = File(context.filesDir, BACKUPS_FOLDER_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Get the actual database file for Amjid Mobile Center.
     */
    fun getDatabaseFile(context: Context): File {
        return context.getDatabasePath(AppDatabase.DATABASE_NAME)
    }

    /**
     * Returns total size in bytes of the main database plus any WAL/SHM files.
     */
    fun getDatabaseSizeBytes(context: Context): Long {
        val dbFile = getDatabaseFile(context)
        if (!dbFile.exists()) return 0L
        var size = dbFile.length()
        val walFile = File(dbFile.path + "-wal")
        if (walFile.exists()) size += walFile.length()
        val shmFile = File(dbFile.path + "-shm")
        if (shmFile.exists()) size += shmFile.length()
        return size
    }

    fun getFormattedDatabaseSize(context: Context): String {
        val bytes = getDatabaseSizeBytes(context)
        return formatFileSize(bytes)
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Export database to any arbitrary output stream (e.g. Storage Access Framework Uri stream).
     */
    fun exportDatabaseToStream(context: Context, outputStream: OutputStream): Result<Long> {
        return try {
            // 1. Force WAL checkpoint to flush all data into the primary .db file
            AppDatabase.checkpointDatabase(context)

            val dbFile = getDatabaseFile(context)
            if (!dbFile.exists() || dbFile.length() == 0L) {
                return Result.failure(IllegalStateException("Database file is empty or does not exist"))
            }

            var bytesCopied = 0L
            outputStream.use { out ->
                FileInputStream(dbFile).use { input ->
                    bytesCopied = input.copyTo(out)
                }
                out.flush()
            }

            Result.success(bytesCopied)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a fast snapshot backup inside the app's local backups directory.
     */
    fun createQuickBackup(context: Context): Result<BackupFileInfo> {
        return try {
            // 1. Checkpoint
            AppDatabase.checkpointDatabase(context)

            val dbFile = getDatabaseFile(context)
            if (!dbFile.exists()) {
                return Result.failure(IllegalStateException("Database file does not exist"))
            }

            val backupsDir = getBackupsDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFileName = "amjid_backup_$timeStamp.db"
            val targetFile = File(backupsDir, backupFileName)

            FileInputStream(dbFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }

            val readableDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(targetFile.lastModified()))
            val info = BackupFileInfo(
                file = targetFile,
                fileName = targetFile.name,
                formattedDate = readableDate,
                formattedSize = formatFileSize(targetFile.length()),
                sizeBytes = targetFile.length(),
                timestamp = targetFile.lastModified()
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * List all quick local backups, sorted by newest first.
     */
    fun listQuickBackups(context: Context): List<BackupFileInfo> {
        val backupsDir = getBackupsDirectory(context)
        val files = backupsDir.listFiles { file ->
            file.isFile && (file.name.endsWith(".db") || file.name.endsWith(".sqlite"))
        } ?: emptyArray()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        return files.sortedByDescending { it.lastModified() }.map { file ->
            BackupFileInfo(
                file = file,
                fileName = file.name,
                formattedDate = dateFormat.format(Date(file.lastModified())),
                formattedSize = formatFileSize(file.length()),
                sizeBytes = file.length(),
                timestamp = file.lastModified()
            )
        }
    }

    /**
     * Delete a local backup file.
     */
    fun deleteQuickBackup(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Restore database from any InputStream (Storage Access Framework or local file).
     */
    fun restoreDatabaseFromStream(context: Context, inputStream: InputStream): Result<Unit> {
        val tempFile = File(context.cacheDir, "temp_restore_${System.currentTimeMillis()}.db")
        try {
            // 1. Copy input stream to temporary file for validation
            FileOutputStream(tempFile).use { out ->
                inputStream.copyTo(out)
                out.flush()
            }

            if (tempFile.length() < 100) {
                tempFile.delete()
                return Result.failure(IllegalArgumentException("File is too small to be a valid SQLite database"))
            }

            // 2. Validate SQLite Header
            FileInputStream(tempFile).use { fis ->
                val headerBytes = ByteArray(16)
                val read = fis.read(headerBytes)
                if (read < 16) {
                    tempFile.delete()
                    return Result.failure(IllegalArgumentException("Corrupted file header"))
                }
                val headerString = String(headerBytes, Charsets.US_ASCII)
                if (headerString != SQLITE_HEADER_STRING) {
                    tempFile.delete()
                    return Result.failure(IllegalArgumentException("Not a valid SQLite database file"))
                }
            }

            // 3. Verify SQLite integrity and table presence using read-only SQLiteDatabase
            var isValidAmjidDatabase = false
            var validationError: String? = null
            try {
                val testDb = SQLiteDatabase.openDatabase(
                    tempFile.path,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
                testDb.use { db ->
                    val cursor = db.rawQuery(
                        "SELECT name FROM sqlite_master WHERE type='table'",
                        null
                    )
                    val tables = mutableSetOf<String>()
                    cursor.use { c ->
                        while (c.moveToNext()) {
                            tables.add(c.getString(0))
                        }
                    }

                    // Check for core shop tables: products, purchases, sales
                    if (tables.contains("products") || tables.contains("sales") || tables.contains("user_accounts")) {
                        isValidAmjidDatabase = true
                    } else {
                        validationError = "Database does not contain expected Amjid Mobile Center tables"
                    }
                }
            } catch (e: Exception) {
                validationError = "SQLite validation failed: ${e.message}"
            }

            if (!isValidAmjidDatabase) {
                tempFile.delete()
                return Result.failure(IllegalArgumentException(validationError ?: "Invalid database schema"))
            }

            // 4. Safe replacement: Close active Room database instance
            AppDatabase.closeDatabase()

            val dbFile = getDatabaseFile(context)
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // Copy tempFile to the actual database file
            if (dbFile.exists()) {
                dbFile.delete()
            }
            tempFile.copyTo(dbFile, overwrite = true)
            tempFile.delete()

            // 5. Reinitialize the database
            val reloadedDb = AppDatabase.getDatabase(context)
            // Trigger a lightweight query to ensure it opens and is valid
            reloadedDb.openHelper.writableDatabase.query("SELECT 1").use { it.moveToFirst() }

            return Result.success(Unit)
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            return Result.failure(e)
        }
    }

    /**
     * Create an Intent to share the backup file via Android Sharesheet (WhatsApp, Drive, Email, etc.).
     */
    fun createShareIntent(context: Context, backupFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Amjid Mobile Center Database Backup")
            putExtra(Intent.EXTRA_TEXT, "Amjid Mobile Center SQLite Database Backup: ${backupFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
