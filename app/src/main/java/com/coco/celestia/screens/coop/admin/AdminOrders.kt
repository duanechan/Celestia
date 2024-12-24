package com.coco.celestia.screens.coop.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.coop.facility.OrderItem
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun AdminOrders(
    userRole: String,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    var keywords by remember { mutableStateOf("RECENT") }

    LaunchedEffect(keywords) {
        orderViewModel.fetchAllOrders(
            filter = keywords,
            role = userRole
        )
    }

    Column(
        modifier = Modifier.background(CoopBackground)
    ) {
        // Filter Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .padding(8.dp)
                .padding(top = 8.dp)
                .background(Color.Transparent)
                .horizontalScroll(rememberScrollState())
                .semantics { testTag = "android:id/FilterButtonsRow" },
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            // Button labels
            val filters = listOf(
                "RECENT",
                "PENDING",
                "ONGOING",
                "AFFECTED",
                "COMPLETED",
                "CANCELLED",
                "REFUNDED/RETURNED"
            )

            filters.forEach { label ->
                val isSelected = keywords == label

                Button(
                    onClick = {
                        keywords = if (isSelected) "" else label
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .semantics { testTag = "android:id/${label.replace("/", "_")}Button" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Green4 else White1,
                        contentColor = Green1
                    )
                ) {
                    Text(
                        text = label,
                        fontFamily = mintsansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (orderState) {
            is OrderState.LOADING -> LoadingOrders()
            is OrderState.ERROR -> OrdersError(errorMessage = (orderState as OrderState.ERROR).message)
            is OrderState.EMPTY -> EmptyOrders()
            is OrderState.SUCCESS -> {
                LazyColumn(modifier = Modifier.semantics { testTag = "android:id/OrderList" }) {
                    itemsIndexed(orderData) { index, order ->
                        OrderItem(
                            order = order,
                            orderViewModel = orderViewModel,
                            orderCount = index + 1,
                            onUpdateOrder = {
                                onUpdateOrder(it)
                                transactionViewModel.recordTransaction(
                                    uid = uid,
                                    transaction = TransactionData(
                                        transactionId = "Transaction-${UUID.randomUUID()}",
                                        type = "Order_Updated",
                                        date = formattedDateTime,
                                        description = "Order#${order.orderId.substring(6, 11).uppercase()} status updated by $userRole to $keywords.",
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingOrders() {
    Box(modifier = Modifier
        .fillMaxSize()
        .semantics { testTag = "android:id/LoadingOrders" }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun OrdersError(errorMessage: String) {
    Box(modifier = Modifier
        .fillMaxSize()
        .semantics { testTag = "android:id/OrdersError" }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Error: $errorMessage",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun EmptyOrders() {
    Box(modifier = Modifier
        .fillMaxSize()
        .semantics { testTag = "android:id/EmptyOrders" }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Empty orders",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
