package com.coco.celestia.screens.coop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.R
import com.coco.celestia.components.dialogs.PendingOrderDialog
import com.coco.celestia.components.dialogs.UpdateOrderStatusDialog
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.client.OrderStatusTracker
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.UserIdentifier
import com.coco.celestia.util.formatDate
import com.coco.celestia.util.orderStatusConfig
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.FullFilledBy
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRequest(
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
    var query by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    LaunchedEffect(keywords) {
        orderViewModel.fetchAllOrders(
            filter = keywords,
            role = userRole
        )
    }

    Column(
        modifier = Modifier.background(CoopBackground)
    ) {
        Row (modifier = Modifier
            .height(75.dp)
            .width(800.dp)
            .background(Color.Transparent)
            .padding(start = 8.dp, end = 8.dp)
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { keywords = query },
                active = false,
                onActiveChange = {},
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                placeholder = { Text("Search") },
                modifier = Modifier
                    .height(50.dp)
                    .width(800.dp)
                    .semantics { testTag = "android:id/OrderSearchBar" }
            ) {
            }
        }

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
            Button(
                onClick = {
                    keywords = if (keywords == "PENDING") {
                        ""
                    } else {
                        "PENDING"
                    }
                },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp)
                    .semantics { testTag = "android:id/PendingButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PendingStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Pending", fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    keywords = if (keywords == "ACCEPTED") {
                        ""
                    } else {
                        "ACCEPTED"
                    }
                },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp)
                    .semantics { testTag = "android:id/PreparingButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PreparingStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Accepted", fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    keywords = if (keywords == "PROCESSING_COFFEE") {
                        ""
                    } else {
                        "PROCESSING_COFFEE"
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .semantics { testTag = "android:id/CompletedButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProcessingCoffee,
                    contentColor = Color.White
                )
            ) {
                Text("Processing Coffee", fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    keywords = if (keywords == "PROCESSING_MEAT") {
                        ""
                    } else {
                        "PROCESSING_MEAT"
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .semantics { testTag = "android:id/CompletedButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProcessingMeat,
                    contentColor = Color.White
                )
            ) {
                Text("Processing Meat", fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    keywords = if (keywords == "DELIVERING") {
                        ""
                    } else {
                        "DELIVERING"
                    }
                },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp)
                    .semantics { testTag = "android:id/DeliveringButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeliveringStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Delivering", fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    keywords = if (keywords == "COMPLETED") {
                        ""
                    } else {
                        "COMPLETED"
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .semantics { testTag = "android:id/CompletedButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CompletedStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Completed", fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        when (orderState) {
            is OrderState.LOADING -> LoadingOrders()
            is OrderState.ERROR -> OrdersError(errorMessage = (orderState as OrderState.ERROR).message)
            is OrderState.EMPTY -> EmptyOrders()
            is OrderState.SUCCESS -> {
                LazyColumn (modifier = Modifier.semantics { testTag = "android:id/OrderList" }){
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
                                        description = "Order#${order.orderId.substring(6, 11).uppercase()} status updated to ${
                                            when(order.status) {
                                                "PENDING" -> "preparing"
                                                "PREPARING" -> "delivering"
                                                "DELIVERING" -> "completed"
                                                "COMPLETED" -> "completed"
                                                else -> "unknown"
                                            }
                                        }.",
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

@Composable
fun OrderItem(
    order: OrderData,
    orderCount: Int,
    orderViewModel: OrderViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val orderStatus = order.status
    val orderId = order.orderId.substring(6,10).uppercase()
    val orderClient = order.client

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CoopBackground)
            .padding(16.dp)
            .semantics { testTag = "android:id/OrderItem_$orderCount" }
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column (modifier = Modifier.padding(top = 10.dp)) {
                        Text(text = "Order ID: $orderId",
                            modifier = Modifier.semantics { testTag = "android:id/OrderIdText_$orderCount" }) //testing
                        Text(
                            text = "${order.orderData.name}, ${order.orderData.quantity}kg",
                            fontSize = 25.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { testTag = "android:id/OrderDetailsText_$orderCount" } //testing
                        )
                        Text(text = orderClient, fontSize = 15.sp,
                            modifier = Modifier.semantics { testTag = "android:id/ClientName_$orderCount" }) //testing
                    }
                }

                if (orderStatus == "REJECTED") {
                    Text(
                        text = orderStatus.lowercase().replaceFirstChar { it.uppercase() },
                        color = orderStatusConfig(orderStatus),
                        modifier = Modifier.semantics { testTag = "android:id/OrderStatus_$orderCount" }
                    )
                }

            }
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "${order.street}, ${order.barangay}", modifier = Modifier.semantics { testTag = "android:id/OrderAddress_$orderCount" })
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Date Ordered: ${formatDate(order.orderDate)}", modifier = Modifier.semantics { testTag = "android:id/OrderDate_$orderCount" })
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    when (orderStatus) {
                        "PENDING" -> PendingOrderActions(
                            order = order,
                            orderViewModel = orderViewModel,
                            onUpdateOrder = { onUpdateOrder(it) }
                        )
                        "ACCEPTED" -> AcceptedOrderActions (
                            order = order,
                            orderViewModel = orderViewModel,
                            type = order.orderData.type,
                            onUpdateOrder = { onUpdateOrder(it) }
                        )

                        "PROCESSING_COFFEE" -> ProcessingMeatAndCoffeeOrderActions (
                            order = order,
                            orderViewModel = orderViewModel,
                            onUpdateOrder = { onUpdateOrder(it) }
                        )

                        "PROCESSING_MEAT" -> ProcessingMeatAndCoffeeOrderActions (
                            order = order,
                            orderViewModel = orderViewModel,
                            onUpdateOrder = { onUpdateOrder(it) }
                        )

                        "DELIVERING" -> DeliveringOrderActions(
                            order = order,
                            orderViewModel = orderViewModel,
                            onUpdateOrder = { onUpdateOrder(it) }
                        )
                        "COMPLETED" -> CompletedOrderActions()
                        "RECEIVED" -> CompletedOrderActions()
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedOrderActions() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(CompletedStatus)
            .padding(8.dp)
            .semantics { testTag = "android:id/CompletedOrderActions" },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Order successfully delivered.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(8.dp, 0.dp)
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Deliver",
            tint = Color.White,
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
        )
    }
}

@Composable
fun AcceptedOrderActions(
    order: OrderData,
    orderViewModel: OrderViewModel,
    type: String,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var statusDialog by remember { mutableStateOf(false) }

    val text = when (type) {
        "CoopCoffee" -> "Process Coffee?"
        "CoopMeat" -> "Process Meat?"
        else -> ""
    }

    val setStatus = when (type) {
        "CoopCoffee" -> "PROCESSING_COFFEE"
        "CoopMeat" -> "PROCESSING_MEAT"
        else -> ""
    }

    val iconPainter: Painter? = when (type) {
        "CoopCoffee" -> painterResource(id = R.drawable.coffee_plant)
        "CoopMeat" -> painterResource(id = R.drawable.cow_animal)
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PreparingStatus)
            .padding(8.dp)
            .semantics { testTag = "android:id/PreparingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { statusDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(DeliveringStatus)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = iconPainter!!,
                contentDescription = "Deliver",
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    if (statusDialog) {
        UpdateOrderStatusDialog(
            status = setStatus,
            onDismiss = { statusDialog = false },
            onAccept = {
                orderViewModel.updateOrder(order.copy(status = setStatus))
                onUpdateOrder(Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                statusDialog = false
            }
        )
    }
}

@Composable
fun ProcessingMeatAndCoffeeOrderActions(
    order: OrderData,
    orderViewModel: OrderViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var statusDialog by remember { mutableStateOf(false) }

    val textColor = when (order.orderData.type) {
        "CoopCoffee" -> ProcessingCoffee
        "CoopMeat" -> ProcessingMeat
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(textColor)
            .padding(8.dp)
            .semantics { testTag = "android:id/PreparingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ship processed product?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { statusDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(DeliveringStatus)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.deliveryicon),
                contentDescription = "Deliver",
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    if (statusDialog) {
        UpdateOrderStatusDialog(
            status = "DELIVERING",
            onDismiss = { statusDialog = false },
            onAccept = {
                orderViewModel.updateOrder(order.copy(status = "DELIVERING"))
                onUpdateOrder(Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                statusDialog = false
            }
        )
    }
}

@Composable
fun DeliveringOrderActions(
    order: OrderData,
    orderViewModel: OrderViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var statusDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "Bouncing animation")
    val bouncingAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bouncing animation"
    )
    val fadeEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.0f at 0 with LinearEasing
                1.0f at 1000 with LinearEasing
                1.0f at 2000 with LinearEasing
                0.0f at 3000 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Fade effect"
    )

    Column {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .fillMaxWidth()
                .background(DeliveringStatus)
                .padding(8.dp)
                .semantics { testTag = "android:id/DeliveringOrderActions" },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order is being delivered.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .semantics { testTag = "android:id/OrderDeliveryText" }
            )
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Deliver",
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .rotate(90f)
                    .padding(8.dp)
                    .offset(y = (-15).dp)
                    .alpha(fadeEffect)
            )
            Icon(
                painter = painterResource(id = R.drawable.deliveryicon),
                contentDescription = "Deliver",
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .offset(y = bouncingAnimation.dp)
                    .clickable { statusDialog = true }
            )
        }
        AnimatedVisibility(expanded) {
            Column {
                OrderStatusTracker(status = "DELIVERING")
                Button(
                    onClick = { statusDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                ) {
                    Text(text = "Finish Delivery", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                }
            }
        }
    }

    if (statusDialog) {
        UpdateOrderStatusDialog(
            status = "COMPLETED",
            onDismiss = { statusDialog = false },
            onAccept = {
                orderViewModel.updateOrder(order.copy(status = "COMPLETED"))
                onUpdateOrder(Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                statusDialog = false
            }
        )
    }
}

@Composable
fun PendingOrderActions(
    order: OrderData,
    orderViewModel: OrderViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }
    val uid = FirebaseAuth.getInstance().uid.toString()
    var fulfiller by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        UserIdentifier.getUserData(uid) { fulfiller = "${it.firstname} ${it.lastname}" }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PendingStatus)
            .padding(15.dp)
            .semantics { testTag = "android:id/PendingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = {
                    showDialog = true
                    action = "Reject"
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Cinnabar)
                    .semantics { testTag = "android:id/RejectButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Reject",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(text = "Reject", modifier = Modifier.padding(top = 16.dp), color = Color.White)
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = {
                    showDialog = true
                    action = "Accept"
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(JadeGreen)
                    .semantics { testTag = "android:id/AcceptButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Check",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()

                )
            }
            Text(text = "Accept", modifier = Modifier.padding(top = 16.dp), color = Color.White)
        }
    }

    if (showDialog) {
        PendingOrderDialog(
            order = order,
            action = action,
            onDismiss = { showDialog = false },
            onAccept = {
                if (action == "Accept") {
                    val farmer = FullFilledBy(
                        farmerName = fulfiller
                    )
                    orderViewModel.updateOrder(
                        order.copy(
                            status = "ACCEPTED",
                            fulfilledBy = order.fulfilledBy.plus(farmer)
                        )
                    )
                } else {
                    orderViewModel.updateOrder(order.copy(status = "REJECTED"))
                }
                onUpdateOrder(Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                showDialog = false
            }
        )
    }
}

