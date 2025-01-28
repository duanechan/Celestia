package com.coco.celestia.screens.coop.facility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.R
import com.coco.celestia.viewmodel.ReportState
import com.coco.celestia.viewmodel.ReportsViewModel
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import java.io.File

@SuppressLint("DefaultLocale")
@Composable
fun CoopReports(
    navController: NavController,
    currentEmail: String,
    modifier: Modifier = Modifier,
    reportsViewModel: ReportsViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel()
) {
    var selectedPeriod by remember { mutableStateOf("This Month") }
    var selectedReportType by remember { mutableStateOf("Sales by Customer") }
    var showTransactionsDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasInitialFetch by remember { mutableStateOf(false) }
    var transactionUpdateKey by remember { mutableIntStateOf(0) }

    val periods = listOf("This Week", "This Month", "Last 3 Months", "This Year")
    val reportTypes = listOf("Sales by Customer", "Sales by Item")
    val context = LocalContext.current

    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)
    val reportState by reportsViewModel.reportState.observeAsState()
    val reportData by reportsViewModel.reportData.observeAsState()
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)
    val transactionData by transactionViewModel.transactionData.observeAsState(hashMapOf())
    val transactionsList = transactionData.values.flatten()

    fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    true
                }

                else -> {
                    showPermissionDialog = true
                    false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    DisposableEffect(Unit) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route == Screen.CoopReports.route) {
                transactionUpdateKey++
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        when (facilityState) {
            FacilityState.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is FacilityState.ERROR -> {
                Text(
                    text = (facilityState as FacilityState.ERROR).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val userFacility = facilitiesData.find { facility ->
                    facility.emails.contains(currentEmail)
                }

                if (userFacility != null) {
                    LaunchedEffect(Unit) {
                        if (!hasInitialFetch) {
                            transactionViewModel.fetchAllTransactions(
                                filter = "Online Sale",
                                facilityName = userFacility.name
                            )
                            hasInitialFetch = true
                        }
                    }

                    LaunchedEffect(selectedPeriod, transactionsList) {
                        if (transactionsList.isNotEmpty()) {
                            reportsViewModel.generateReport(
                                reportType = selectedReportType,
                                dateRange = selectedPeriod,
                                transactions = transactionsList
                            )
                        }
                    }

                    // Permission Dialog
                    if (showPermissionDialog) {
                        AlertDialog(
                            onDismissRequest = { showPermissionDialog = false },
                            title = { Text("Permission Required") },
                            text = { Text("Storage permission is required to download reports.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showPermissionDialog = false
                                        val activity = context as? Activity
                                        activity?.requestPermissions(
                                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                            100
                                        )
                                    }
                                ) {
                                    Text("Grant Permission")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPermissionDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // Transactions Dialog
                    if (showTransactionsDialog) {
                        AlertDialog(
                            onDismissRequest = { showTransactionsDialog = false },
                            title = {
                                Text(
                                    text = "Report Summary",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                ) {
                                    when (transactionState) {
                                        TransactionState.LOADING -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        is TransactionState.ERROR -> {
                                            Text(
                                                text = (transactionState as TransactionState.ERROR).message,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        TransactionState.EMPTY -> {
                                            Text(
                                                text = "No transactions found",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        TransactionState.SUCCESS -> {
                                            if (transactionsList.isEmpty()) {
                                                Text(
                                                    text = "No transactions found",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    items(transactionsList.toList()) { transaction ->
                                                        TransactionsCard(transaction)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showTransactionsDialog = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }

                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Current Facility Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Green4
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Current Facility",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = userFacility.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = mintsansFontFamily
                                )
                            }
                        }

                        // Report Type Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Green4
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Report Type",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                reportTypes.forEach { type ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedReportType == type,
                                            onClick = { selectedReportType = type }
                                        )
                                        Text(text = type, fontFamily = mintsansFontFamily)
                                    }
                                }
                            }
                        }

                        // Report Period Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Green4
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Report Period",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                periods.forEach { period ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedPeriod == period,
                                            onClick = { selectedPeriod = period }
                                        )
                                        Text(text = period, fontFamily = mintsansFontFamily)
                                    }
                                }
                            }
                        }

                        // View Summary Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable { showTransactionsDialog = true },
                            colors = CardDefaults.cardColors(
                                containerColor = Green4
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "View Report Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "View Transactions",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !checkAndRequestPermissions()) {
                                        return@IconButton
                                    }

                                    if (reportState == null || reportState is ReportState.LOADING) {
                                        Toast.makeText(
                                            context,
                                            "Please wait for the report to generate",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@IconButton
                                    }

                                    try {
                                        val reportContent =
                                            reportData?.values?.flatten()?.firstOrNull()?.content
                                        if (reportContent == null) {
                                            Toast.makeText(
                                                context,
                                                "No report data available",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@IconButton
                                        }

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            val contentValues = ContentValues().apply {
                                                put(
                                                    MediaStore.MediaColumns.DISPLAY_NAME,
                                                    "${
                                                        selectedReportType.replace(
                                                            " ",
                                                            "_"
                                                        )
                                                    }_${System.currentTimeMillis()}.txt"
                                                )
                                                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                                                put(
                                                    MediaStore.MediaColumns.RELATIVE_PATH,
                                                    Environment.DIRECTORY_DOWNLOADS
                                                )
                                            }

                                            val resolver = context.contentResolver
                                            val uri = resolver.insert(
                                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                                contentValues
                                            )

                                            uri?.let { fileUri ->
                                                resolver.openOutputStream(fileUri)
                                                    ?.use { outputStream ->
                                                        outputStream.write(reportContent.toByteArray())
                                                    }
                                                Toast.makeText(
                                                    context,
                                                    "Report downloaded to Downloads folder",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } ?: throw Exception("Failed to create file")
                                        } else {
                                            val downloadDir =
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                )
                                            if (!downloadDir.exists()) {
                                                downloadDir.mkdirs()
                                            }

                                            val fileName = "${
                                                selectedReportType.replace(
                                                    " ",
                                                    "_"
                                                )
                                            }_${System.currentTimeMillis()}.csv"
                                            val downloadFile = File(downloadDir, fileName)
                                            downloadFile.writeText(reportContent)
                                            Toast.makeText(
                                                context,
                                                "Report downloaded to Downloads folder",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Failed to download report: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.download_2),
                                    contentDescription = "Download Report"
                                )
                            }
                        }
                    }
                }
                else {
                    NoFacilityScreen()
                }
            }
        }
    }
}