package com.coco.celestia.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.formatDate
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FarmerTransactions(navController: NavController) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val transactionViewModel: TransactionViewModel = viewModel()
    val transactionData by transactionViewModel.transactionData.observeAsState(emptyList())
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)

    LaunchedEffect(Unit, transactionData) {
        if (transactionData.isEmpty()) {
            transactionViewModel.fetchTransactions(uid = uid, filter = "")
        }
    }

    when (transactionState) {
        is TransactionState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Blue)
            }
        }
        is TransactionState.ERROR -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load transactions: ${(transactionState as TransactionState.ERROR).message}",
                    color = Color.Red
                )
            }
        }
        is TransactionState.EMPTY -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions available.")
            }
        }
        is TransactionState.SUCCESS -> {
            if (transactionData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = BgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = BgColor)
                ) {
                    items(transactionData) { transaction ->
                        TransactionItem(transaction, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionData, navController: NavController) {
    val transactionDate = formatDate(transaction.order.orderDate).toString()
    val orderStatus = transaction.order.status
    val productType = transaction.order.orderData.type
    val productQuantity = transaction.order.orderData.quantity

    Card(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        StyledText(transactionDate, productQuantity.toString(), productType, orderStatus)
    }
}

@Composable
fun StyledText(transactionDate: String, productQuantity: String, productType: String, orderStatus: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$transactionDate \nYou ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                append("sold ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${productQuantity}kg of $productType. ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                append("Current order status: ")
            }
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = when (orderStatus) {
                        "PENDING" -> Color(0xFFFF4F00)
                        "PREPARING" -> Color.Green
                        else -> Color.Red
                    }
                )
            ) {
                append(orderStatus)
            }
        },
        modifier = Modifier.padding(20.dp)
    )
}
