package com.coco.celestia.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.BlueGradientBrush
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.PaleBlue
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.util.UserIdentifier
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserManagementAuditLogs(navController: NavController, transactionViewModel: TransactionViewModel) {
    val transactionData by transactionViewModel.transactionData.observeAsState(hashMapOf())
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)

    LaunchedEffect(Unit) {
        transactionViewModel.fetchAllTransactions() // Put filter keyword here if search functionality exists
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(BlueGradientBrush)
            .semantics { testTag = "android:id/AuditLogsScreen" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/HeaderRow" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
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
                            .offset(x = (-15).dp)
                            .semantics { testTag = "android:id/ActionHeader" },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                }
                Divider()
            }
            // Rows for audit logs
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
                    transactionData.entries.forEach { (userId, transactions) ->
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
            .padding(13.dp)
            .semantics { testTag = "android:id/LogItem" }
    ) {
        Text(
            text = transaction.date,
            fontSize = 14.sp,
            fontFamily = mintsansFontFamily,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (userData != null) "${userData?.firstname} ${userData?.lastname}" else "Unknown",
            fontSize = 14.sp,
            fontFamily = mintsansFontFamily,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = transaction.type,
            fontSize = 14.sp,
            fontFamily = mintsansFontFamily,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = { showDescriptionDialog = true },
            modifier = Modifier.weight(0.5f)
        ) {
            Icon(
                painter = painterResource(R.drawable.viewmore),
                contentDescription = "Show Description",
                tint = DarkBlue,
                modifier = Modifier
                    .size(25.dp)

            )
        }

        if (showDescriptionDialog) {
            AlertDialog(
                onDismissRequest = { showDescriptionDialog = false },
                title = {
                    Text(text = "Transaction Details", fontFamily = mintsansFontFamily,)
                },
                text = {
                    Text(text = transaction.description)
                },
                confirmButton = {
                    Button(
                        onClick = { showDescriptionDialog = false }
                    ) {
                        Text("Close", fontFamily = mintsansFontFamily)
                    }
                }
            )
        }
    }
}

