package com.coco.celestia.screens.coop.facility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.DatePicker
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
import com.coco.celestia.viewmodel.ReportsViewModel
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
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
    val selectedReportType = "Sales by Item"
    var selectedProduct by remember { mutableStateOf<String?>(null) }
    var showTransactionsDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasInitialFetch by remember { mutableStateOf(false) }
    var transactionUpdateKey by remember { mutableIntStateOf(0) }
    var startDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current

    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState(UserData())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val isAdmin = remember(userData) { userData.role == "Admin" }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userViewModel.fetchUser(currentUser.uid)
        }
    }

    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)
    val reportState by reportsViewModel.reportState.observeAsState()
    val reportData by reportsViewModel.reportData.observeAsState()
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)
    val transactionData by transactionViewModel.transactionData.observeAsState(hashMapOf())
    val transactionsList = transactionData.values.flatten()

    val filteredTransactions = remember(transactionsList, startDate, endDate, selectedProduct) {
        transactionsList.filter { transaction ->
            if (transaction.type != "Online Sale") return@filter false

            if (selectedProduct != null && transaction.productName != selectedProduct) {
                return@filter false
            }

            try {
                val formatters = listOf(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("dd MMM yyyy"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("dd MMMM yyyy")
                )

                val transactionDate = formatters.firstNotNullOfOrNull { formatter ->
                    try {
                        LocalDate.parse(transaction.date, formatter)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (transactionDate != null) {
                    transactionDate.isEqual(startDate) ||
                            transactionDate.isEqual(endDate) ||
                            (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate))
                } else {
                    true
                }
            } catch (e: Exception) {
                true
            }
        }
    }

    val productNames = remember(transactionsList) {
        transactionsList
            .filter { it.type == "Online Sale" }
            .map { it.productName }
            .distinct()
            .sorted()
    }

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

                if (userFacility != null || isAdmin) {
                    LaunchedEffect(Unit) {
                        if (!hasInitialFetch) {
                            if (isAdmin) {
                                transactionViewModel.fetchAllTransactions(
                                    filter = "Online Sale"
                                )
                            } else {
                                transactionViewModel.fetchAllTransactions(
                                    filter = "Online Sale",
                                    facilityName = userFacility?.name ?: ""
                                )
                            }
                            hasInitialFetch = true
                        }
                    }

                    LaunchedEffect(startDate, endDate, selectedReportType, selectedProduct, transactionsList) {
                        if (transactionsList.isNotEmpty()) {
                            reportsViewModel.generateReportWithDateRange(
                                reportType = selectedReportType,
                                startDate = startDate,
                                endDate = endDate,
                                transactions = filteredTransactions,
                                productFilter = selectedProduct
                            )
                        }
                    }

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

                    if (showTransactionsDialog) {
                        AlertDialog(
                            onDismissRequest = { showTransactionsDialog = false },
                            title = {
                                Text(
                                    text = "Sales by Item Report Summary",
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
                                            if (filteredTransactions.isEmpty()) {
                                                Text(
                                                    text = "No transactions found for the selected criteria",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            } else {
                                                Column(
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        text = "Product Summary",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                                                    Text(
                                                        text = "Period: ${startDate.format(dateFormatter)} to ${endDate.format(dateFormatter)}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )

                                                    if (selectedProduct != null) {
                                                        Text(
                                                            text = "Product: $selectedProduct",
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "Product: All Products",
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }

                                                    Text(
                                                        text = "Total Sales: ${filteredTransactions.size}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )

                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Green4)
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "Product",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1.3f)
                                                        )
                                                        Text(
                                                            text = "Date",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1.5f)
                                                        )
                                                        Text(
                                                            text = "Status",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }

                                                    LazyColumn(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        items(filteredTransactions) { transaction ->
                                                            val displayDate = transaction.date

                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(
                                                                    text = transaction.productName,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    modifier = Modifier.weight(1.3f)
                                                                )
                                                                Text(
                                                                    text = displayDate,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    modifier = Modifier.weight(1.5f)
                                                                )
                                                                Text(
                                                                    text = transaction.status,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                            }
                                                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                                                        }
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
                                    text = if (isAdmin) "Admin Report" else "Current Facility",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isAdmin) "Special Requests" else userFacility?.name ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = mintsansFontFamily
                                )
                            }
                        }

                        // Report Type
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
                                    text = "Sales by Item Report",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (productNames.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Select Product",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = mintsansFontFamily
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    var expanded by remember { mutableStateOf(false) }

                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextField(
                                            value = selectedProduct ?: "All Products",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                            },
                                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("All Products") },
                                                onClick = {
                                                    selectedProduct = null
                                                    expanded = false
                                                }
                                            )

                                            productNames.forEach { product ->
                                                DropdownMenuItem(
                                                    text = { Text(product) },
                                                    onClick = {
                                                        selectedProduct = product
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Report Period
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
                                Spacer(modifier = Modifier.height(16.dp))

                                // Start Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Start Date",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = mintsansFontFamily
                                    )

                                    OutlinedTextField(
                                        value = startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        onValueChange = { },
                                        singleLine = true,
                                        readOnly = true,
                                        modifier = Modifier.width(200.dp),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Select Date",
                                                modifier = Modifier.clickable {
                                                    val calendar = Calendar.getInstance()
                                                    calendar.set(startDate.year, startDate.monthValue - 1, startDate.dayOfMonth)

                                                    android.app.DatePickerDialog(
                                                        context,
                                                        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                                            startDate = LocalDate.of(
                                                                year,
                                                                month + 1,
                                                                dayOfMonth
                                                            )
                                                        },
                                                        calendar.get(Calendar.YEAR),
                                                        calendar.get(Calendar.MONTH),
                                                        calendar.get(Calendar.DAY_OF_MONTH)
                                                    ).show()
                                                }
                                            )
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // End Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "End Date",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = mintsansFontFamily
                                    )

                                    OutlinedTextField(
                                        value = endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        onValueChange = { },
                                        singleLine = true,
                                        readOnly = true,
                                        modifier = Modifier.width(200.dp),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Select Date",
                                                modifier = Modifier.clickable {
                                                    val calendar = Calendar.getInstance()
                                                    calendar.set(endDate.year, endDate.monthValue - 1, endDate.dayOfMonth)

                                                    android.app.DatePickerDialog(
                                                        context,
                                                        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                                            endDate = LocalDate.of(
                                                                year,
                                                                month + 1,
                                                                dayOfMonth
                                                            )
                                                        },
                                                        calendar.get(Calendar.YEAR),
                                                        calendar.get(Calendar.MONTH),
                                                        calendar.get(Calendar.DAY_OF_MONTH)
                                                    ).show()
                                                }
                                            )
                                        }
                                    )
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

                                    if (filteredTransactions.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "No transactions found for the selected criteria",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@IconButton
                                    }

                                    try {
                                        val reportContent = buildString {
                                            appendLine("Sales by Item Report")
                                            appendLine("Generated on: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
                                            appendLine()
                                            appendLine()

                                            appendLine("----------------------------------------")
                                            appendLine("Product Analysis")
                                            appendLine("----------------------------------------")
                                            appendLine()

                                            val productGroups = filteredTransactions
                                                .groupBy { it.productName }
                                                .toSortedMap()

                                            if (productGroups.isNotEmpty()) {
                                                productGroups.forEach { (product, sales) ->
                                                    val dates = sales.map { it.date }
                                                    appendLine("-----------------------------------")
                                                    appendLine("Product Name: $product")
                                                    appendLine("Product ID: ${sales.firstOrNull()?.productId ?: ""}")
                                                    appendLine("Number of Sales: ${sales.size}")
                                                    appendLine("First Sale Date: ${dates.minOrNull() ?: ""}")
                                                    appendLine("Last Sale Date: ${dates.maxOrNull() ?: ""}")
                                                    appendLine("-----------------------------------")
                                                    appendLine()
                                                }
                                            }
                                            appendLine()

                                            appendLine("----------------------------------------")
                                            appendLine("Daily Sales Breakdown")
                                            appendLine("----------------------------------------")
                                            appendLine()

                                            if (filteredTransactions.isNotEmpty()) {
                                                filteredTransactions
                                                    .groupBy { it.date }
                                                    .toSortedMap()
                                                    .forEach { (date, salesOnDate) ->
                                                        appendLine("-----------------------------------")
                                                        appendLine("Date: $date")
                                                        appendLine("Number of Sales: ${salesOnDate.size}")
                                                        appendLine("Products Sold: ${salesOnDate.map { it.productName }.distinct().joinToString(", ")}")
                                                        appendLine("-----------------------------------")
                                                        appendLine()
                                                    }
                                            }
                                            appendLine()

                                            appendLine("----------------------------------------")
                                            appendLine("Overall Summary")
                                            appendLine("----------------------------------------")
                                            appendLine()
                                            appendLine("Total Unique Products: ${productGroups.size}")
                                            appendLine("Total Sales Transactions: ${filteredTransactions.size}")
                                            appendLine()
                                            appendLine("Total Sales by Product:")
                                            productGroups.forEach { (product, sales) ->
                                                appendLine("$product: ${sales.size} sales")
                                            }
                                            appendLine()

                                            val dates = filteredTransactions.map { it.date }
                                            if (dates.isNotEmpty()) {
                                                appendLine("Report Period:")
                                                appendLine("First Transaction Date: ${dates.minOrNull()}")
                                                appendLine("Last Transaction Date: ${dates.maxOrNull()}")
                                            } else {
                                                appendLine("Report Period: No transactions found")
                                            }
                                            appendLine()
                                            appendLine("----------------------------------------")
                                            appendLine()
                                            appendLine("Sales by Item Report Summary")
                                            appendLine()
                                            appendLine("Product,Date,Status")

                                            filteredTransactions.forEach { transaction ->
                                                appendLine("${transaction.productName},${transaction.date},${transaction.status}")
                                            }
                                        }

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            val contentValues = ContentValues().apply {
                                                put(
                                                    MediaStore.MediaColumns.DISPLAY_NAME,
                                                    "Sales_by_Item_${System.currentTimeMillis()}.txt"
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

                                            val fileName = "Sales_by_Item_${System.currentTimeMillis()}.txt"
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
                else if (!isAdmin) {
                    NoFacilityScreen()
                }
            }
        }
    }
}