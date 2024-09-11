package com.coco.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientTransaction : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB)) // Hex color))
                ) {
                    TransactionPanel(transactionViewModel = viewModel())
                }
            }
        }
    }
}

@Composable
fun TransactionPanel(transactionViewModel: TransactionViewModel) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid.toString()
    val transactionData by transactionViewModel.transactionData.observeAsState(emptyList())
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)

    LaunchedEffect(Unit) {
        transactionViewModel.fetchTransactions(uid, "")
    }
    LazyColumn {
        when (transactionState) {
            is TransactionState.LOADING -> {
                item {
                    Text("Loading transactions...")
                }
            }
            is TransactionState.ERROR -> {
                item {
                    Text("Failed to load transactions: ${(transactionState as TransactionState.ERROR).message}")
                }
            }
            is TransactionState.EMPTY -> {
                item {
                    Text("No transactions available.")
                }
            }
            is TransactionState.SUCCESS -> {
                items(transactionData) { transaction ->
                    TransactionItem(transaction)
                }
            }

            else -> {}
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionData) {
    val transactionDate = formatDate(transaction.order.orderDate).toString()
    val productType = transaction.order.orderData.type
    val productQuantity = transaction.order.orderData.quantity
    val orderStatus = transaction.order.status

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
                append("ordered ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${productQuantity}kg of $productType. ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                append("Current order status: ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,
                color = when (orderStatus) {
                    "PENDING" -> Color(0xFFFF4F00)
                    "ACCEPTED" -> Color.Green
                    else -> Color.Red
            }
            )) {
                append(orderStatus)
            }
        },
        modifier = Modifier.padding(20.dp)
    )
}

fun formatDate(dateString: String): String? {
    return try {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("M/dd/yy - h:mm a", Locale.ENGLISH)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}