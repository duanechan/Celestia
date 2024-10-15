package com.coco.celestia.screens.coop

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CompletedStatus
import com.coco.celestia.ui.theme.DeliveringStatus
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.ui.theme.PendingStatus
import com.coco.celestia.ui.theme.PreparingStatus
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRequest(
    navController: NavController,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel,
) {
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var query by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    LaunchedEffect(keywords) {
        orderViewModel.fetchAllOrders(
            filter = keywords,
            role = "Coop"
        )
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Row (modifier = Modifier
            .height(75.dp)
            .padding(top = 5.dp)
            .background(Color.Transparent)) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { keywords = query },
                active = false,
                onActiveChange = {},
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                placeholder = { Text("Search") },
                modifier = Modifier.height(50.dp)

            ) {

            }
            // TODO: Implement filter button here.
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .padding(8.dp)
                .padding(top = 8.dp)
                .background(Color.Transparent)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Button(
                onClick = { /* Handle filter click */ },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PendingStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Pending", fontFamily = mintsansFontFamily)
            }
            Button(
                onClick = { /* Handle filter click */ },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PreparingStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Preparing", fontFamily = mintsansFontFamily)
            }
            Button(
                onClick = { /* Handle filter click */ },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeliveringStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Delivering", fontFamily = mintsansFontFamily)
            }
            Button(
                onClick = { /* Handle filter click */ },
                modifier = Modifier
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CompletedStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Completed", fontFamily = mintsansFontFamily)
            }
        }

        when (orderState) {
            is OrderState.LOADING -> LoadingOrders()
            is OrderState.ERROR -> OrdersError(errorMessage = (orderState as OrderState.ERROR).message)
            is OrderState.EMPTY -> EmptyOrders()
            is OrderState.SUCCESS -> {
                LazyColumn {
                    itemsIndexed(orderData) { index, order ->
                        OrderItem(
                            order = order,
                            auth = auth,
                            navController = navController,
                            orderViewModel = orderViewModel,
                            transactionViewModel = transactionViewModel,
                            orderCount = index + 1
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
fun LoadingOrders() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun OrdersError(errorMessage: String) {
    Box(modifier = Modifier.fillMaxSize()) {
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
    Box(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No pending orders.",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun OrderItem(
    order: OrderData,
    orderCount: Int,
    auth: FirebaseAuth,
    navController: NavController,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel,
) {
    val orderStatus = order.status
    when(orderStatus) {
        "PENDING" -> {
            PendingOrderItem(
                navController = navController,
                order = order,
                onAccept = {
                    orderViewModel.updateOrder(order.copy(status = "PREPARING"))
                    transactionViewModel.recordTransaction(
                        auth.currentUser?.uid.toString(),
                        TransactionData(
                            "TRNSCTN{${order.orderId}}",
                            order.copy(status = "PREPARING")
                        )
                    )
                }
            )
        }
        "PREPARING" -> {
            PreparingOrderItem(
                order = order,
                navController = navController,
                orderViewModel = orderViewModel,
                orderCount = orderCount
            )
        }
        // TODO: Add other order statuses here
    }
}

@Composable
fun PreparingOrderItem(
    order: OrderData,
    orderCount: Int,
    navController: NavController,
    orderViewModel: OrderViewModel,
) {
    val orderId = order.orderId.substring(5,9).uppercase()
    val orderClient = order.client

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Card () {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(),
            ) {
                Row {
                    Text(
                        text = orderCount.toString(),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(5.dp)
                    )
                    Column (modifier = Modifier.padding(16.dp)) {
                        Text(text = "Order ID: $orderId",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold)
                        Text(
                            text = "${order.orderData.name}, ${order.orderData.quantity}kg",
                            fontSize = 25.sp, fontWeight = FontWeight.Bold
                        )
                        Text(text = "Client Name: $orderClient",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .background(
                            when (order.status) {
                                "PREPARING" -> PreparingStatus
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "${order.street}, ${order.barangay}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = order.orderDate)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.CoopProcessOrder.createRoute(order.orderId))
                        },
                        colors = ButtonDefaults.buttonColors(contentColor = Color.DarkGray, containerColor = Color.Transparent)
                    ) {
                        Text("View Order")
                    }
                }
            }
        }
    }
}

@Composable
fun PendingOrderItem(
    navController: NavController,
    order: OrderData,
    onAccept: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                Text(text = "${order.orderData.name}, ${order.orderData.quantity}kg",
                    fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .background(
                            when (order.status) {
                                "PENDING" -> PendingStatus
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(text = "${order.street}, ${order.barangay}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = order.orderDate)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.CoopProcessOrder.createRoute(order.orderId))
                        },
                        colors = ButtonDefaults.buttonColors(contentColor = Color.DarkGray, containerColor = Color.Transparent)
                    ) {
                        Text("View Order")
                    }
                    Button(
                        onClick = {
                            action = "Accept"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CompletedStatus)
                    ) {
                        Text("Accept", fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(text = "Order ${order.orderId.substring(5,9).uppercase()}")
            },
            text = {
                Text(text = "Are you sure you want to accept this order?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onAccept()
                    }
                ) {
                    Text("Accept")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
