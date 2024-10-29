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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.R
import com.coco.celestia.components.dialogs.PendingOrderDialog
import com.coco.celestia.components.dialogs.UpdateOrderStatusDialog
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.client.OrderStatusTracker
import com.coco.celestia.ui.theme.Cinnabar
import com.coco.celestia.ui.theme.CompletedStatus
import com.coco.celestia.ui.theme.DeliveringStatus
import com.coco.celestia.ui.theme.JadeGreen
import com.coco.celestia.ui.theme.PendingStatus
import com.coco.celestia.ui.theme.PreparingStatus
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.util.formatDate
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRequest(
    userRole: String,
    orderViewModel: OrderViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var query by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    LaunchedEffect(keywords) {
        orderViewModel.fetchAllOrders(
            filter = keywords,
            role = userRole
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
                modifier = Modifier
                    .height(50.dp)
                    .semantics { testTag = "android:id/OrderSearchBar" }
            ) {

            }

        }
// TODO: Implement filter button functionality
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
                onClick = { keywords = "PENDING" },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp)
                    .semantics { testTag = "android:id/PendingButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PendingStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Pending", fontFamily = mintsansFontFamily)
            }
            Button(
                onClick = { keywords = "PREPARING" },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp)
                    .semantics { testTag = "android:id/PreparingButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PreparingStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Preparing", fontFamily = mintsansFontFamily)
            }
            Button(
                onClick = { keywords = "DELIVERING" },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp)
                    .semantics { testTag = "android:id/DeliveringButton" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeliveringStatus,
                    contentColor = Color.White
                )
            ) {
                Text("Delivering", fontFamily = mintsansFontFamily)
            }
            Button(
                onClick = { keywords = "COMPLETED" },
                modifier = Modifier
                    .height(40.dp)
                    .semantics { testTag = "android:id/CompletedButton" },
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
                LazyColumn (modifier = Modifier.semantics { testTag = "android:id/OrderList" }){
                    itemsIndexed(orderData) { index, order ->
                        OrderItem(
                            order = order,
                            orderViewModel = orderViewModel,
                            orderCount = index + 1,
                            onUpdateOrder = { onUpdateOrder(it) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(150.dp)) }
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
            .background(Color.White)
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
                Row {
                    Text(
                        text = orderCount.toString(),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(5.dp)
                    )
                    Column (modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${order.orderData.name}, ${order.orderData.quantity}kg",
                            fontSize = 30.sp, fontWeight = FontWeight.Bold
                        )
                        Text(text = "Client Name: $orderClient",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }

                StatusSelector(
                    order = order,
                    orderViewModel = orderViewModel,
                    onUpdateOrder = { onUpdateOrder(it) }
                )

            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Order ID: $orderId")
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "${order.street}, ${order.barangay}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formatDate(order.orderDate))
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
                        "PREPARING" -> PreparingOrderActions(
                            order = order,
                            orderViewModel = orderViewModel,
                            onUpdateOrder = { onUpdateOrder(it) }
                        )
                        "DELIVERING" -> DeliveringOrderActions()
                        "COMPLETED" -> CompletedOrderActions()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelector(
    order: OrderData,
    orderViewModel: OrderViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var statusExpanded by remember { mutableStateOf(false) }
    var statusDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }
    val orderStatus = order.status
    val color = when (orderStatus) {
        "PENDING" -> PendingStatus
        "PREPARING" -> PreparingStatus
        "DELIVERING" -> DeliveringStatus
        "COMPLETED" -> CompletedStatus
        else -> Color.Gray
    }

    ExposedDropdownMenuBox(
        expanded = statusExpanded,
        onExpandedChange = {
            if (orderStatus != "PENDING") {
                statusExpanded = !statusExpanded
            }
        }
    ) {
        TextField(
            readOnly = true,
            value = orderStatus,
            enabled = orderStatus != "PENDING",
            onValueChange = {},
            trailingIcon = {
                if (orderStatus != "PENDING") {
                    ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded)
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = color,
                focusedContainerColor = color,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedTrailingIconColor = Color.White,
                focusedTrailingIconColor = Color.White,
                disabledContainerColor = color,
                disabledTrailingIconColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .menuAnchor()
                .clip(RoundedCornerShape(16.dp))
                .width(175.dp)
        )
        ExposedDropdownMenu(
            expanded = statusExpanded,
            onDismissRequest = { statusExpanded = false }
        ) {
            listOf("Preparing", "Delivering", "Completed").forEach { status ->
                DropdownMenuItem(
                    text = { Text(text = status, fontFamily = mintsansFontFamily)  },
                    onClick = {
                        selectedStatus = status
                        statusDialog = true
                    }
                )
            }
        }
    }
    if (statusDialog) {
        UpdateOrderStatusDialog(
            status = selectedStatus,
            onDismiss = { statusDialog = false },
            onAccept = {
                orderViewModel.updateOrder(order.copy(status = selectedStatus.uppercase()))
                onUpdateOrder(Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                statusDialog = false
                statusExpanded = false
            }
        )
    }
}

@Composable
fun CompletedOrderActions() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(CompletedStatus)
            .padding(8.dp),
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
fun PreparingOrderActions(
    order: OrderData,
    orderViewModel: OrderViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var statusDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ship this order?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(8.dp, 0.dp)
        )
        IconButton(
            onClick = { statusDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(DeliveringStatus)
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
fun DeliveringOrderActions() {
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
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order is being delivered.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
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
            )
        }
        AnimatedVisibility(expanded) {
            OrderStatusTracker(status = "DELIVERING")
        }
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(15.dp),
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
            Text(text = "Reject", modifier = Modifier.padding(top = 16.dp))
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
            Text(text = "Accept", modifier = Modifier.padding(top = 16.dp))
        }
    }

    if (showDialog) {
        PendingOrderDialog(
            order = order,
            action = action,
            onDismiss = { showDialog = false },
            onAccept = {
                if (action == "Accept") {
                    orderViewModel.updateOrder(order.copy(status = "PREPARING"))
                } else {
                    orderViewModel.updateOrder(order.copy(status = "REJECTED"))
                }
                onUpdateOrder(Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                showDialog = false
            }
        )
    }
}

