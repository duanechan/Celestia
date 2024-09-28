package com.coco.celestia.screens.coop

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.coco.celestia.screens.Screen
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRequest(
    navController: NavController,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel
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
        Row {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { keywords = query },
                active = false,
                onActiveChange = {},
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                placeholder = { Text("Search") }
            ) {

            }
            // TODO: Implement filter button here.
        }

        when (orderState) {
            is OrderState.LOADING -> {
                CircularProgressIndicator()
            }
            is OrderState.ERROR -> {
                Text("Failed to load orders: ${(orderState as OrderState.ERROR).message}")
            }
            is OrderState.EMPTY -> {
                Text("Awit man! No pending orders.")
            }
            is OrderState.SUCCESS -> {
                LazyColumn {
                    items(orderData) { order ->
                        OrderItem(
                            order = order,
                            auth = auth,
                            navController = navController,
                            orderViewModel = orderViewModel,
                            transactionViewModel = transactionViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(
    order: OrderData,
    auth: FirebaseAuth,
    navController: NavController,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel,
) {
    val orderStatus = order.status
    when(orderStatus) {
        "PENDING" -> {
            PendingOrderItem(
                order,
                onAccept = {
                    orderViewModel.updateOrder(order.copy(status = "PREPARING"))
                    transactionViewModel.recordTransaction(
                        auth.currentUser?.uid.toString(),
                        TransactionData(
                            "TRNSCTN{${order.orderId}}",
                            order.copy(status = "PREPARING")
                        )
                    )
                },
                onReject = {
                    orderViewModel.updateOrder(order.copy(status = "REJECTED"))
                    transactionViewModel.recordTransaction(
                        auth.currentUser?.uid.toString(),
                        TransactionData(
                            "TRNSCTN{${order.orderId}}",
                            order.copy(status = "REJECTED")
                        )
                    )
                }
            )
        }
        "PREPARING" -> {
            PreparingOrderItem(
                order = order,
                navController = navController,
                orderViewModel = orderViewModel
            )
        }
        // TODO: Add other order statuses here
    }
}

@Composable
fun PreparingOrderItem(
    order: OrderData,
    navController: NavController,
    orderViewModel: OrderViewModel
) {
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
                Text(
                    text = if (order.orderData.type != "Vegetable") "${order.orderData.name}, ${order.orderData.quantity}kg" else order.orderData.name,
                    fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif
                )
                Text(
                    text = "${order.status} ●",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = Orange
                )
                Text(text = "${order.street}, ${order.barangay}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = order.orderDate)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            orderViewModel.fetchOrder(order.orderId)
                            navController.navigate(Screen.CoopProcessOrder.route)
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
    order: OrderData,
    onAccept: () -> Unit,
    onReject: () -> Unit
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
                Text(text = if (order.orderData.type != "Vegetable") "${order.orderData.name}, ${order.orderData.quantity}kg" else order.orderData.name,
                    fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text(text = "${order.status} ●", fontSize = 20.sp, fontWeight = FontWeight.Light, color = Orange)
                Text(text = "${order.street}, ${order.barangay}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = order.orderDate)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (order.orderData.type != "Vegetable") {
                        OutlinedButton(
                            onClick = {
                                action = "Reject"
                                showDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(contentColor = Color.Red, containerColor = Color.White)
                        ) {
                            Text("Reject")
                        }
                    }

                    Button(
                        onClick = {
                            action = "Accept"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Accept")
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
                Text(text = "$action Order")
            },
            text = {
                Text(text = "Are you sure you want to $action this order?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (action == "Accept") onAccept() else onReject()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}
