package com.coco.celestia.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FarmerTransactions(navController: NavController) {
    val transactionViewModel: TransactionViewModel = viewModel()
    val transactionData by transactionViewModel.transactionData.observeAsState(hashMapOf())
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    val userId = currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            transactionViewModel.fetchTransactions(uid = userId, filter = "")
        }
    }

    when (transactionState) {
        is TransactionState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/loadingBox" },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Blue,
                    modifier = Modifier.semantics { testTag = "android:id/loadingIndicator" })
            }
        }
        is TransactionState.ERROR -> {
            val errorMessage = (transactionState as TransactionState.ERROR).message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/errorBox" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load transactions: $errorMessage",
                    color = Color.Red,
                    modifier = Modifier.semantics { testTag = "android:id/errorMessage" }
                )
            }
        }
        is TransactionState.EMPTY -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/emptyBox" },
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions available.", modifier = Modifier.semantics { testTag = "android:id/emptyMessage" })
            }
        }
        is TransactionState.SUCCESS -> {
            if (transactionData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = BgColor)
                        .semantics { testTag = "android:id/noTransactionsBox" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found.", modifier = Modifier.semantics { testTag = "android:id/noTransactionsMessage" })
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = BgColor)
                        .semantics { testTag = "android:id/transactionList" }
                ) {
                    transactionData.forEach { (userId, transactions) ->
                        val sortedTransactions = transactions.sortedByDescending { transaction ->
                            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(transaction.date)
                        }

                        items(sortedTransactions) { transaction ->
                            TransactionItem(transaction, navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionData, navController: NavController) {
    Card(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, bottom = 15.dp, top = 15.dp)
            .fillMaxWidth()
            .height(200.dp)
            .semantics { testTag = "android:id/transactionCard" },
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(
                    colors = listOf(PaleGold, GoldenYellow)
                ))
                .padding(16.dp)
        ) {
            StyledText(
                transactionDate = transaction.date,
                productDescription = transaction.description,
                productType = transaction.type
            )
        }
    }
}

@Composable
fun StyledText(transactionDate: String, productDescription: String, productType: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                append("$transactionDate \n")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                append("$productType\n")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp)) {
                append("$productDescription\n")
            }
        },
        color = Cocoa,
        modifier = Modifier
            .padding(20.dp)
            .semantics { testTag = "android:id/styledText" }
    )
}