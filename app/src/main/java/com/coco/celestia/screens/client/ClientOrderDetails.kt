package com.coco.celestia.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CDText
import com.coco.celestia.ui.theme.ClientBG
import com.coco.celestia.ui.theme.Copper
import com.coco.celestia.ui.theme.GDivider
import com.coco.celestia.ui.theme.GreenBeans
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.ODivider
import com.coco.celestia.ui.theme.TrackOrder
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData

@Composable
fun ClientOrderDetails(
    navController: NavController,
    orderId: String
) {
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val orderIdSub = orderId.substring(6, 29).uppercase()
    LaunchedEffect(Unit) {
        if (allOrders.isEmpty()) {
            orderViewModel.fetchAllOrders(
                filter = "",
                role = "Client"
            )
        }
    }

    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    val showCancelConfirmation = remember { mutableStateOf(false) }
    val showOrderCancelledDialog = remember { mutableStateOf(false) }
    val showReceivedConfirmation = remember { mutableStateOf(false) }

    when {
        orderState == OrderState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTag = "android:id/LoadingIndicator" },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        orderData == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTag = "android:id/OrderNotFound" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Order not found",
                    color = Copper,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        else -> {
            val product = orderData.orderData

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightOrange)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/OrderDetailsCard" },
//                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        // Upper Part
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
//                                .clip(RoundedCornerShape(5.dp))
                                .background(LightOrange),
                            contentAlignment = Alignment.TopStart,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .semantics { testTag = "android:id/OrderCountBox" }
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.vegetable_meat),
                                        contentDescription = "vegetable_meat_icon",
                                        modifier = Modifier.size(70.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = orderIdSub,
                                            fontSize = 15.sp,
                                            color = White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.semantics {
                                                testTag = "android:id/OrderID"
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        val formattedStatus = orderData.status.replace("_", " ")
                                        Text(
                                            text = formattedStatus,
                                            fontSize = 25.sp,
                                            color = White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Order Details
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 24.dp)
                            ) {
                                Text(
                                    text = "Order Details",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                OrderDetailsColumn("Items Ordered", product.name)
                                OrderDetailsColumn("Product Quantity", "${product.quantity} kg")
                                OrderDetailsColumn("Date Ordered", orderData.orderDate)
                                OrderDetailsColumn(
                                    "Deliver to",
                                    "${orderData.street}, ${orderData.barangay}"
                                )
                                OrderDetailsColumn("Target Date", orderData.targetDate)

                                Divider(
                                    modifier = Modifier
                                        .padding(top = 15.dp, bottom = 5.dp),
                                    thickness = 3.dp,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )

                                Text(
                                    "Fulfilled By",
                                    fontSize = 16.sp,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .padding(top = 10.dp, bottom = 6.dp)
                                )
                                Row (
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                ){
                                    Text(
                                        "Name",
                                        fontSize = 16.sp,
                                        color = Color.Black.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .padding(end = 5.dp)
                                            .weight(1.2f)
                                    )
                                    Text(
                                        "Quantity",
                                        fontSize = 16.sp,
                                        color = Color.Black.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .weight(1f)
                                    )
                                    Text(
                                        "Status",
                                        fontSize = 16.sp,
                                        color = Color.Black.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .weight(1.2f)
                                    )
                                }

                                if (orderData.status == "PARTIALLY_FULFILLED") {
                                    orderData.fulfilledBy.forEach { fulFillData ->
                                        Row {
                                            Text(
                                                fulFillData.farmerName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .weight(1.2f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                fulFillData.quantityFulfilled.toString(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .padding(horizontal = 10.dp)
                                                    .weight(1f),
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                fulFillData.status,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .weight(1.2f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                } else {
                                    orderData.fulfilledBy.forEach { fulFillData ->
                                        Row {
                                            Text(
                                                fulFillData.farmerName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .weight(1.2f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                fulFillData.quantityFulfilled.toString(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .padding(horizontal = 10.dp)
                                                    .weight(1f),
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                orderData.status,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .weight(1.2f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                Divider(
                                    modifier = Modifier
                                        .padding(top = 15.dp, bottom = 5.dp),
                                    thickness = 3.dp,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // Buttons For Pending and Completed Orders
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (orderData.status == "PENDING") {
                                    Button(
                                        onClick = {
                                            showCancelConfirmation.value = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = White
                                        ),
                                        modifier = Modifier
                                            .height(50.dp)
                                            .width(170.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel),
                                            contentDescription = "Cancel Icon",
                                            tint = Color.Red,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Cancel Order",
                                            color = Color.Red
                                        )
                                    }
                                }

                                if (orderData.status == "COMPLETED") {
                                    Button(
                                        onClick = {
                                            showReceivedConfirmation.value = true
                                            orderViewModel.markOrderReceived(orderData.orderId)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = White
                                        ),
                                        modifier = Modifier
                                            .height(50.dp)
                                            .width(170.dp)
                                    ) {
                                        val greenColor =
                                            Color(0xFF4CAF50) //to move in colors.kt
                                        Icon(
                                            painter = painterResource(id = R.drawable.received),
                                            contentDescription = "Received Icon",
                                            tint = greenColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Received",
                                            color = greenColor,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Track Order
                        if (orderData.status != "PENDING") {
                            OrderStatusTracker(status = orderData.status)
                        }
                        Spacer(modifier = Modifier.height(130.dp))

                    }
                    if (showCancelConfirmation.value) {
                        AlertDialog(
                            onDismissRequest = { showCancelConfirmation.value = false },
                            title = {
                                Text(
                                    text = "Cancel Order",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            text = {
                                Text(
                                    text = "Are you sure you want to cancel this order?",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showCancelConfirmation.value = false
                                        orderViewModel.cancelOrder(orderData.orderId)
                                        showOrderCancelledDialog.value = true
                                    }
                                ) {
                                    Text("Yes", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showCancelConfirmation.value = false }
                                ) {
                                    Text("No")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }

                    if (showOrderCancelledDialog.value) {
                        AlertDialog(
                            onDismissRequest = {
                                showOrderCancelledDialog.value = false
                                navController.navigate(Screen.ClientOrder.route)
                            },
                            title = {
                                Text(
                                    text = "Order Cancelled",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            text = {
                                Text(
                                    text = "Your order has been successfully cancelled.",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showOrderCancelledDialog.value = false
                                        navController.navigate(Screen.ClientOrder.route)
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }
                    if (showReceivedConfirmation.value) {
                        AlertDialog(
                            onDismissRequest = {
                                showReceivedConfirmation.value = false
                                navController.navigate(Screen.ClientOrder.route)
                            },
                            title = {
                                Text(
                                    text = "Order Received",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            text = {
                                Text(
                                    text = "You have confirmed that you received the order.",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showReceivedConfirmation.value = false
                                        navController.navigate(Screen.ClientOrder.route)
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailsColumn(label: String, value: String) {
    Text(
        text = label,
        fontSize = 16.sp,
        color = Color.Black.copy(alpha = 0.5f),
        modifier = Modifier
            .padding(top = 15.dp, bottom = 3.dp)
    )

    Text(
        text = value,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun OrderStatusTracker(status: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { testTag = "android:id/OrderStatusTracker" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 0.dp)
        ) {
            Text(
                text = "Track Order",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(15.dp))

            if (status in listOf("PREPARING", "DELIVERING", "COMPLETED", "RECEIVED")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .semantics { testTag = "android:id/PreparingStatus" }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.warehouse),
                        contentDescription = "Warehouse",
                        tint = TrackOrder,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The product is being prepared in the warehouse.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (status in listOf("DELIVERING", "COMPLETED", "RECEIVED")) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .semantics { testTag = "android:id/DeliveryDivider" }
                ) {
                    Spacer(modifier = Modifier.width(22.dp))
                    Divider(
                        color = Color(0xFFFFA500),
                        modifier = Modifier
                            .width(2.dp)
                            .height(50.dp)
                            .semantics { testTag = "android:id/DividerLine" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .semantics { testTag = "android:id/DeliveringStatus" }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.deliveryicon),
                        contentDescription = "DeliveryTruck",
                        tint = TrackOrder,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The delivery is on the way.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (status in listOf("COMPLETED", "RECEIVED")) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .semantics { testTag = "android:id/CompletedDivider" }
                ) {
                    Spacer(modifier = Modifier.width(22.dp))
                    Divider(
                        color = ODivider,
                        modifier = Modifier
                            .width(2.dp)
                            .height(50.dp)
                            .semantics { testTag = "android:id/CompletedDividerLine" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .semantics { testTag = "android:id/CompletedStatus" }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = LightOrange,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The order has been delivered.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (status == "RECEIVED") {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .semantics { testTag = "android:id/ReceivedDivider" }
                ) {
                    Spacer(modifier = Modifier.width(22.dp))
                    Divider(
                        color = GDivider,
                        modifier = Modifier
                            .width(2.dp)
                            .height(50.dp)
                            .semantics { testTag = "android:id/ReceivedDividerLine" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .semantics { testTag = "android:id/ReceivedStatus" }
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Received",
                        tint = GreenBeans,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You have received your order!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}