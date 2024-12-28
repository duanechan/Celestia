package com.coco.celestia.screens.coop.admin

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.UserIdentifier
import com.coco.celestia.util.exportToExcel
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData

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
    var selectedCategory by remember { mutableStateOf("") }
    var filterActionExpanded by remember { mutableStateOf(false) }
    var filteredOrderDataTran by remember { mutableStateOf<Map<UserData, List<TransactionData>>>(emptyMap()) }
    var filteredSearch by remember { mutableStateOf<Map<String, Pair<UserData, List<TransactionData>>>>(
        emptyMap()
    ) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                exportToExcel(context, filteredOrderDataTran)
            } else {
                Toast.makeText(context, "Storage permission is required to save the file.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(transactionData) {
        transactionViewModel.fetchAllTransactions()

        val filterTransaction = mutableMapOf<String, List<TransactionData>>()
        val filterOrderDataTran = mutableMapOf<UserData, List<TransactionData>>()
        val filterSearch = mutableMapOf<String, Pair<UserData, List<TransactionData>>>()

        transactionData.forEach { (userId, transaction) ->
            UserIdentifier.getUserData(userId) {
                userData = it
            }

            if (userData?.role?.contains("Coop") == true) {
                filterTransaction[userId] = transaction
                filterOrderDataTran[userData!!] = transaction
                filterSearch[userId] = userData!! to transaction
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
            modifier = Modifier
            .padding(10.dp)
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
                    placeholder = { Text("Search user...", color = Green1) },
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
                        if (actionStatusView) {
                            listOf("All", "Product Updated", "Order Updated", "Product Added").forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = status,
                                            color = if (selectedStatus == status) Color.White else Green1
                                        )
                                    },
                                    modifier = Modifier
                                        .background(
                                            color = if (selectedStatus == status) Green1 else Color.Transparent
                                        ),
                                    onClick = {
                                        selectedStatus = status
                                        filterActionExpanded = false
                                    }
                                )
                            }
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "All Actions",
                                        color = if (selectedCategory.isEmpty()) Color.White else Green1
                                    )
                                },
                                modifier = Modifier
                                    .background(
                                        color = if (selectedCategory.isEmpty()) Green1 else Color.Transparent
                                    ),
                                onClick = {
                                    selectedCategory = ""
                                    filterActionExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))

                IconButton (
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            exportToExcel(context, filteredOrderDataTran)
                        } else {
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    },
                    modifier = Modifier
                        .background(color = Green2 , shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.download_icon),
                        contentDescription = "Export to Excel",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(5.dp)
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
            // Sticky Header
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(13.dp)
                        .semantics { testTag = "android:id/AuditLogsHeader" }
                ) {
                    Text(
                        text = "DATE",
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/DateHeader" },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "USER",
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/UserHeader" },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "ACTION",
                        modifier = Modifier
                            .weight(1f)
                            .offset(x = (-20).dp)
                            .semantics { testTag = "android:id/ActionHeader" },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
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
                    item { Text("Loading logs...") }
                }
                TransactionState.SUCCESS -> {
                    filteredSearch.entries.forEach { (userId, pair) ->
                        val (user, transactions) = pair
                        items(transactions.filter { transaction ->
                            val formattedStatus = selectedStatus.replace(" ", "_")
                            val fullName = "${user.firstname} ${user.lastname}"
                            (transaction.type == formattedStatus || formattedStatus == "All") &&
                                    (fullName.contains(searchQuery, ignoreCase = true) || searchQuery == "")
                        }) { transaction ->
                            LogItem(userId, transaction, isEvenRow = transactions.indexOf(transaction) % 2 == 0)
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
    var userData by remember { mutableStateOf<UserData?>(null) }
    var showDescriptionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        UserIdentifier.getUserData(uid) { result ->
            userData = result
        }
    }
    val formattedType = transaction.type.replace("_", " ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isEvenRow) White1 else Color.White)
            .padding(10.dp)
            .semantics { testTag = "android:id/LogItem" }
    ) {
        Text(
            text = transaction.date,
            fontSize = 13.sp,
            fontFamily = mintsansFontFamily,
            color = Green1,
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/transactionDate" }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/userDataColumn" }
        ) {
            // First name and last name
            Text(
                text = if (userData != null) "${userData?.firstname} ${userData?.lastname}" else "Unknown",
                fontSize = 13.sp,
                fontFamily = mintsansFontFamily,
                fontWeight = FontWeight.Bold,
                color = Green1
            )
            // Role
            Text(
                text = userData?.role ?: "",
                fontSize = 13.sp,
                fontFamily = mintsansFontFamily,
                color = Green1
            )
        }
        Text(
            text = formattedType,
            fontSize = 12.sp,
            fontFamily = mintsansFontFamily,
            color = Green1,
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/transactionType" }
        )

        IconButton(
            onClick = { showDescriptionDialog = true },
            modifier = Modifier
                .weight(0.5f)
                .semantics { testTag = "android:id/viewDetailsButton" }
        ) {
            Icon(
                painter = painterResource(R.drawable.viewmore),
                contentDescription = "View Details",
                tint = Green1,
                modifier = Modifier
                    .size(25.dp)
            )
        }

        if (showDescriptionDialog) {
            AlertDialog(
                onDismissRequest = { showDescriptionDialog = false },
                title = {
                    Text(text = "Transaction Details", fontFamily = mintsansFontFamily, modifier = Modifier.semantics { testTag = "android:id/dialogTitle" })
                },
                text = {
                    Text(text = transaction.description, modifier = Modifier.semantics { testTag = "android:id/transactionDescription" })
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


