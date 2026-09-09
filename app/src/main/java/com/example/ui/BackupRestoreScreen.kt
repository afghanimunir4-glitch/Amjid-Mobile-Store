package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.util.BackupFileInfo
import com.example.util.DatabaseBackupManager
import com.example.util.DateTimeUtils
import com.example.util.ShopStrings
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    viewModel: ShopViewModel,
    localBackups: List<BackupFileInfo>,
    isLoading: Boolean,
    statusMessage: String?,
    totalProducts: Int,
    totalSalesCount: Int,
    totalPurchasesCount: Int
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var pendingRestoreFile by remember { mutableStateOf<File?>(null) }
    var pendingDeleteFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadLocalBackups()
    }

    LaunchedEffect(statusMessage) {
        if (!statusMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(statusMessage)
            viewModel.clearBackupStatusMessage()
        }
    }

    // Storage Access Framework launcher for EXPORTING SQLite DB
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportDatabaseToUri(it)
        }
    }

    // Storage Access Framework launcher for RESTORING SQLite DB
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingRestoreUri = it
        }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                title = ShopStrings.backupAndRestore(language),
                subtitle = ShopStrings.backupSubtitle(language),
                showBackButton = true,
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Current Database Status Banner
                item {
                    DatabaseStatusCard(
                        language = language,
                        databaseSize = viewModel.getDatabaseSize(),
                        totalProducts = totalProducts,
                        totalPurchases = totalPurchasesCount,
                        totalSales = totalSalesCount
                    )
                }

                // 2. Export / Backup Operations Card
                item {
                    ExportCard(
                        language = language,
                        onExportToFile = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            val defaultName = "amjid_backup_$timeStamp.db"
                            exportLauncher.launch(defaultName)
                        },
                        onQuickBackup = {
                            viewModel.createQuickBackup()
                        }
                    )
                }

                // 3. Restore Operations Card
                item {
                    RestoreCard(
                        language = language,
                        onSelectFileToRestore = {
                            restoreLauncher.launch(arrayOf("*/*", "application/octet-stream", "application/x-sqlite3"))
                        }
                    )
                }

                // 4. Local Backup Snapshots History Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ShopStrings.backupHistory(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "${localBackups.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 5. Local Backup Snapshots List
                if (localBackups.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ShopStrings.noBackupsYet(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(localBackups, key = { it.fileName }) { backup ->
                        BackupItemCard(
                            language = language,
                            backup = backup,
                            onRestore = { pendingRestoreFile = backup.file },
                            onShare = {
                                val shareIntent = DatabaseBackupManager.createShareIntent(context, backup.file)
                                context.startActivity(Intent.createChooser(shareIntent, "Share Database Backup"))
                            },
                            onDelete = { pendingDeleteFile = backup.file }
                        )
                    }
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (language == AppLanguage.PASHTO) "د ډیټابیس پروسه روانه ده..." else "Processing database...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for RESTORING via Storage Access Framework Uri
    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = ShopStrings.confirmRestoreTitle(language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = ShopStrings.confirmRestoreMessage(language),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toRestore = pendingRestoreUri
                        pendingRestoreUri = null
                        if (toRestore != null) {
                            viewModel.restoreDatabaseFromUri(toRestore)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_restore_uri_button")
                ) {
                    Text(ShopStrings.restoreDatabase(language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingRestoreUri = null }) {
                    Text(ShopStrings.cancel(language))
                }
            }
        )
    }

    // Confirmation dialog for RESTORING via Local Backup File
    pendingRestoreFile?.let { file ->
        AlertDialog(
            onDismissRequest = { pendingRestoreFile = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = ShopStrings.confirmRestoreTitle(language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = ShopStrings.confirmRestoreMessage(language),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${file.name} (${DatabaseBackupManager.formatFileSize(file.length())})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toRestore = pendingRestoreFile
                        pendingRestoreFile = null
                        if (toRestore != null) {
                            viewModel.restoreFromLocalBackup(toRestore)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_restore_file_button")
                ) {
                    Text(ShopStrings.restoreDatabase(language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingRestoreFile = null }) {
                    Text(ShopStrings.cancel(language))
                }
            }
        )
    }

    // Confirmation dialog for DELETING a backup file
    pendingDeleteFile?.let { file ->
        AlertDialog(
            onDismissRequest = { pendingDeleteFile = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = ShopStrings.confirmDeleteBackupTitle(language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${ShopStrings.confirmDeleteBackupMessage(language)}\n\n${file.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = pendingDeleteFile
                        pendingDeleteFile = null
                        if (toDelete != null) {
                            viewModel.deleteLocalBackup(toDelete)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_backup_button")
                ) {
                    Text(ShopStrings.delete(language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDeleteFile = null }) {
                    Text(ShopStrings.cancel(language))
                }
            }
        )
    }
}

@Composable
private fun DatabaseStatusCard(
    language: AppLanguage,
    databaseSize: String,
    totalProducts: Int,
    totalPurchases: Int,
    totalSales: Int
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = ShopStrings.databaseStatus(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "SQLite (amjid_mobile_center.db)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = databaseSize,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    label = ShopStrings.products(language),
                    value = "$totalProducts",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatusItem(
                    label = ShopStrings.purchases(language),
                    value = "$totalPurchases",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatusItem(
                    label = ShopStrings.sales(language),
                    value = "$totalSales",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ExportCard(
    language: AppLanguage,
    onExportToFile: () -> Unit,
    onQuickBackup: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF059669).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = ShopStrings.exportDatabase(language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ShopStrings.exportDatabaseDesc(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Primary export button (Storage Access Framework)
            Button(
                onClick = onExportToFile,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("export_database_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.PASHTO) "د موبایل حافظې ته لېږل (.db)" else "Export .db File to Device Storage",
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Snapshot Backup button
            OutlinedButton(
                onClick = onQuickBackup,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("quick_backup_button")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ShopStrings.quickBackup(language),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RestoreCard(
    language: AppLanguage,
    onSelectFileToRestore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFD97706).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = ShopStrings.restoreDatabase(language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ShopStrings.restoreDatabaseDesc(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onSelectFileToRestore,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("restore_database_button")
            ) {
                Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.PASHTO) "له موبایل څخه د بیک اپ فایل انتخاب کړئ" else "Select & Restore .db File from Device",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BackupItemCard(
    language: AppLanguage,
    backup: BackupFileInfo,
    onRestore: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = backup.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${backup.formattedDate} • ${backup.formattedSize}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Share icon
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = ShopStrings.shareBackup(language),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete icon
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = ShopStrings.delete(language),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Restore action button
            OutlinedButton(
                onClick = onRestore,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("restore_item_${backup.fileName}")
            ) {
                Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = ShopStrings.restoreDatabase(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
