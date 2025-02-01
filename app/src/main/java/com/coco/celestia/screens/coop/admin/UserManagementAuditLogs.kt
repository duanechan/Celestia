package com.coco.celestia.screens.coop.admin

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.UserIdentifier
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserManagementAuditLogs(navController: NavController, transactionViewModel: TransactionViewModel) {
    val transactionData by transactionViewModel.transactionData.observeAsState(hashMapOf())
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)
    var userData by remember { mutableStateOf<UserData?>(null) }
    var filteredTransaction by remember { mutableStateOf<Map<String, List<TransactionData>>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    val actionStatusView by remember { mutableStateOf(true) }
    var selectedStatus by remember { mutableStateOf("All") }
    var filterActionExpanded by remember { mutableStateOf(false) }
    var filteredOrderDataTran by remember { mutableStateOf<Map<UserData, List<TransactionData>>>(emptyMap()) }
    var filteredSearch by remember { mutableStateOf<Map<String, Pair<UserData, List<TransactionData>>>>(emptyMap()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val transactionTypes = listOf(
        "All",
        "Online Sale",
        "In-Store Sale",
        "Purchased"
    )

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

    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Storage permission is required to download audit logs.") },
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

    LaunchedEffect(transactionData) {
        transactionViewModel.fetchAllTransactions()

        val filterTransaction = mutableMapOf<String, List<TransactionData>>()
        val filterOrderDataTran = mutableMapOf<UserData, List<TransactionData>>()
        val filterSearch = mutableMapOf<String, Pair<UserData, List<TransactionData>>>()

        transactionData.forEach { (userId, transactions) ->
            UserIdentifier.getUserData(userId) { userData ->
                val relevantTransactions = transactions
                    .filter { transaction ->
                        transaction.type in listOf("Online Sale", "In-Store Sale", "Purchased")
                    }
                    .sortedByDescending { it.date }

                if (relevantTransactions.isNotEmpty()) {
                    filterTransaction[userId] = relevantTransactions
                    userData?.let {
                        filterOrderDataTran[it] = relevantTransactions
                        filterSearch[userId] = it to relevantTransactions
                    }
                }
            }
        }

        filteredTransaction = filterTransaction
        filteredOrderDataTran = filterOrderDataTran
        filteredSearch = filterSearch
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Green4)
            .semantics { testTag = "android:id/AuditLogsScreen" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search user or product...", color = Green1) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Green1
                        )
                    },
                    modifier = Modifier
                        .background(color = Color.White, shape = RoundedCornerShape(20.dp))
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box {
                    IconButton(
                        onClick = { filterActionExpanded = true },
                        modifier = Modifier
                            .background(color = Green2, shape = RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = filterActionExpanded,
                        onDismissRequest = { filterActionExpanded = false },
                        modifier = Modifier
                            .background(color = White1, shape = RoundedCornerShape(8.dp))
                            .heightIn(max = 250.dp)
                    ) {
                        transactionTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = type,
                                        color = if (selectedStatus == type) Color.White else Green1
                                    )
                                },
                                modifier = Modifier
                                    .background(
                                        color = if (selectedStatus == type) Green1 else Color.Transparent
                                    ),
                                onClick = {
                                    selectedStatus = type
                                    filterActionExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !checkAndRequestPermissions()) {
                            return@IconButton
                        }

                        try {
                            val csvContent = buildString {
                                appendLine("Date,User,Role,Action,Description")
                                filteredSearch.forEach { (_, pair) ->
                                    val (user, transactions) = pair
                                    transactions.forEach { transaction ->
                                        val formattedType = transaction.type.replace("_", " ")
                                        appendLine("${transaction.date},${user.firstname} ${user.lastname},${user.role},${formattedType},${transaction.description}")
                                    }
                                }
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val contentValues = ContentValues().apply {
                                    put(
                                        MediaStore.MediaColumns.DISPLAY_NAME,
                                        "audit_logs_${System.currentTimeMillis()}.txt"
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
                                    resolver.openOutputStream(fileUri)?.use { outputStream ->
                                        outputStream.write(csvContent.toByteArray())
                                    }
                                    Toast.makeText(
                                        context,
                                        "Audit logs downloaded to Downloads folder",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } ?: throw Exception("Failed to create file")
                            } else {
                                val downloadDir = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )
                                if (!downloadDir.exists()) {
                                    downloadDir.mkdirs()
                                }

                                val fileName = "audit_logs_${System.currentTimeMillis()}.txt"
                                val downloadFile = File(downloadDir, fileName)
                                downloadFile.writeText(csvContent)
                                Toast.makeText(
                                    context,
                                    "Audit logs downloaded to Downloads folder",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Failed to download audit logs: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .background(color = Green2, shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.download_icon),
                        contentDescription = "Export to CSV",
                        tint = Color.White,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .semantics { testTag = "android:id/AuditLogsList" }
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(13.dp)
                        .semantics { testTag = "android:id/AuditLogsHeader" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DATE",
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/DateHeader" },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ITEM",
                        modifier = Modifier
                            .weight(1.5f)
                            .semantics { testTag = "android:id/DetailsHeader" },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "TYPE",
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/TypeHeader" },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(0.3f))
                }
                Divider()
            }

            when (transactionState) {
                TransactionState.EMPTY -> {
                    item { Text("Empty logs.") }
                }
                is TransactionState.ERROR -> {
                    item { Text("Error loading logs.") }
                }
                TransactionState.LOADING -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Green1,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading logs...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Green1
                                )
                            }
                        }
                    }
                }
                TransactionState.SUCCESS -> {
                    filteredSearch.entries.forEach { (userId, pair) ->
                        val (user, transactions) = pair
                        val sortedAndFilteredTransactions = transactions
                            .filter { transaction ->
                                val formattedType = when (selectedStatus) {
                                    "Online Sale" -> "Online Sale"
                                    "In-Store Sale" -> "In-Store Sale"
                                    "Purchased" -> "Purchased"
                                    else -> selectedStatus
                                }

                                val fullName = "${user.firstname} ${user.lastname}"
                                val searchMatches = fullName.contains(searchQuery, ignoreCase = true) ||
                                        transaction.productName.contains(searchQuery, ignoreCase = true) ||
                                        searchQuery.isEmpty()

                                (transaction.type == formattedType || formattedType == "All") && searchMatches
                            }
                            .sortedByDescending { transaction ->
                                try {
                                    when {
                                        transaction.date.contains(":") -> {
                                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                                .parse(transaction.date)?.time ?: 0
                                        }
                                        transaction.date.contains(" ") -> {
                                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                                .parse(transaction.date)?.time ?: 0
                                        }
                                        else -> {
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                .parse(transaction.date)?.time ?: 0
                                        }
                                    }
                                } catch (e: Exception) {
                                    0L
                                }
                            }

                        items(sortedAndFilteredTransactions) { transaction ->
                            LogItem(
                                uid = userId,
                                transaction = transaction,
                                isEvenRow = sortedAndFilteredTransactions.indexOf(transaction) % 2 == 0
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(uid: String, transaction: TransactionData, isEvenRow: Boolean) {
    var showDescriptionDialog by remember { mutableStateOf(false) }
    val formattedType = transaction.type.replace("_", " ")

    val formattedDate = remember(transaction.date) {
        try {
            val parsedDate = when {
                transaction.date.contains(":") -> {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .parse(transaction.date)
                }
                transaction.date.contains(" ") && !transaction.date.contains("-") -> {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .parse(transaction.date)
                }
                else -> {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(transaction.date)
                }
            }

            parsedDate?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
            } ?: transaction.date
        } catch (e: Exception) {
            transaction.date
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isEvenRow) White1 else Color.White)
            .padding(10.dp)
            .semantics { testTag = "android:id/LogItem" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/dateColumn" },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formattedDate,
                fontSize = 13.sp,
                fontFamily = mintsansFontFamily,
                color = Green1,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .weight(1.5f)
                .semantics { testTag = "android:id/productColumn" },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = transaction.productName,
                fontSize = 13.sp,
                fontFamily = mintsansFontFamily,
                fontWeight = FontWeight.Bold,
                color = Green1,
                textAlign = TextAlign.Center
            )
            Text(
                text = transaction.facilityName,
                fontSize = 12.sp,
                fontFamily = mintsansFontFamily,
                color = Green1,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/typeColumn" },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formattedType,
                fontSize = 13.sp,
                fontFamily = mintsansFontFamily,
                color = Green1,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        IconButton(
            onClick = { showDescriptionDialog = true },
            modifier = Modifier
                .weight(0.3f)
                .semantics { testTag = "android:id/viewDetailsButton" }
        ) {
            Icon(
                painter = painterResource(R.drawable.viewmore),
                contentDescription = "View Details",
                tint = Green1,
                modifier = Modifier.size(25.dp)
            )
        }

        if (showDescriptionDialog) {
            AlertDialog(
                onDismissRequest = { showDescriptionDialog = false },
                title = {
                    Text(
                        text = "Transaction Details",
                        fontFamily = mintsansFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { testTag = "android:id/dialogTitle" }
                    )
                },
                text = {
                    Column(modifier = Modifier.semantics { testTag = "android:id/transactionDetails" }) {
                        DetailRow("Date", formattedDate)  // Use formatted date here too
                        DetailRow("Product", transaction.productName)
                        DetailRow("Facility", transaction.facilityName)
                        DetailRow("Type", formattedType)
                        DetailRow("Product ID", transaction.productId)
                        if (transaction.vendorName.isNotEmpty()) {
                            DetailRow("Vendor", transaction.vendorName)
                        }
                        DetailRow("Description", transaction.description)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDescriptionDialog = false },
                        modifier = Modifier.semantics { testTag = "android:id/closeDialogButton" }
                    ) {
                        Text("Close", fontFamily = mintsansFontFamily)
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = mintsansFontFamily,
            fontWeight = FontWeight.Bold,
            color = Green1
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = mintsansFontFamily,
            color = Green1,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}