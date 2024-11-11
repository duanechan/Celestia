package com.coco.celestia.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.UserIdentifier
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

    LaunchedEffect(Unit) {
        transactionViewModel.fetchAllTransactions() // Put filter keyword here if search functionality exists
        val filterTransaction = transactionData.mapNotNull { (userId, transaction) ->
            UserIdentifier.getUserData(userId) {
                userData = it
            }

            if (userData?.role?.contains("Coop") == true) {
                userId to transaction
            } else {
                null
            }
        }.toMap()

        filteredTransaction = filterTransaction
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(BlueGradientBrush)
            .semantics { testTag = "android:id/AuditLogsScreen" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/HeaderRow" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigate(Screen.AdminUserManagement.route) },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(top = 5.dp)
                    .semantics { testTag = "android:id/BackButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Audit Logs",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(top = 5.dp)
                    .semantics { testTag = "android:id/AuditLogsTitle" }
            )
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
                        text = "USER-ROLE",
                        modifier = Modifier
                            .weight(1f)
                            .offset(x = (-25).dp)
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
                    filteredTransaction.entries.forEach { (userId, transactions) ->
                        items(transactions) { transaction ->
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isEvenRow) PaleBlue else Color.White)
            .padding(10.dp)
            .semantics { testTag = "android:id/LogItem" }
    ) {
        Text(
            text = transaction.date,
            fontSize = 13.sp,
            fontFamily = mintsansFontFamily,
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/transactionDate" }
        )
        Text(
            text = if (userData != null) "${userData?.firstname} ${userData?.lastname}- ${userData?.role}" else "Unknown",
            fontSize = 13.sp,
            fontFamily = mintsansFontFamily,
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/userData" }
        )
        Text(
            text = transaction.type,
            fontSize = 12.sp,
            fontFamily = mintsansFontFamily,
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
                tint = DarkBlue,
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


