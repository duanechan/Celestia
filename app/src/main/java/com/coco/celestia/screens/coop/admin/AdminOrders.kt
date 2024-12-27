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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.R
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
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4)
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Change to total number of orders
            Text("All(0)")

            Row {
                Icon(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    painter = painterResource(R.drawable.filter2),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
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
