package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.ui.theme.Copper
import com.coco.celestia.ui.theme.LightGray
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.TreeBark
import com.coco.celestia.util.formatDate
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData

@Composable
fun ClientOrderDetails(
    navController: NavController,
    orderId: String,
    orderCount: Int
) {
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

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

    when {
        orderState == OrderState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = LightGray)
                    .semantics { testTag = "LoadingIndicator" },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        orderData == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White)
                    .semantics { testTag = "OrderNotFound" },
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
                modifier = Modifier.fillMaxSize()
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
                            .height(520.dp)
                            .semantics { testTag = "OrderDetailsCard" },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(520.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(LightOrange),
                            contentAlignment = Alignment.TopStart,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 110.dp, start = 16.dp, end = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .semantics { testTag = "OrderCountBox" }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 50.dp, height = 117.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = orderCount.toString(),
                                            fontSize = 50.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(5.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append("Order ID ")
                                                }
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                                    append("#" + orderData.orderId.substring(5, 38))
                                                }
                                            },
                                            fontSize = 20.sp,
                                            color = White,
                                            modifier = Modifier.semantics { testTag = "OrderID" }
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append("Delivery Address: ")
                                                }
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                                    append("${orderData.street}, ${orderData.barangay}")
                                                }
                                            },
                                            fontSize = 20.sp,
                                            color = White,
                                            modifier = Modifier.semantics {
                                                testTag = "DeliveryAddress"
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append("Estimated Date of Arrival: ")
                                                }
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                                    append(formatDate(orderData.targetDate))
                                                }
                                            },
                                            fontSize = 15.sp,
                                            color = White,
                                            modifier = Modifier.semantics { testTag = "ETA" }
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                        .semantics { testTag = "OrderedProducts" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Ordered Products Icon",
                                        tint = White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ordered Products",
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(start = 15.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(TreeBark)
                                            .padding(start = 10.dp, end = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = product.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 30.sp,
                                                color = White,
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "${product.quantity} kg",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 40.sp,
                                                color = White,
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 16.dp, bottom = 16.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Check if the order status is "PENDING"
                                    if (orderData.status == "PENDING") {
                                        Button(
                                            onClick = { showCancelConfirmation.value = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            modifier = Modifier
                                                .height(40.dp)
                                                .width(130.dp)
                                        ) {
                                            Text(text = "Cancel Order", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    OrderStatusTracker(status = orderData.status)
                    Spacer(modifier = Modifier.height(110.dp))
                }
                if (showCancelConfirmation.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { showCancelConfirmation.value = false }
                    ) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                                .fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Are you sure you want to cancel this order?",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(onClick = {
                                        showCancelConfirmation.value = false
                                        // TODO: Add cancellation functionality here
                                    }) {
                                        Text("Yes")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { showCancelConfirmation.value = false }) {
                                        Text("No")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusTracker(status: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .semantics { testTag = "OrderStatusTracker" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (status in listOf("PREPARING", "DELIVERING", "COMPLETED")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics { testTag = "PreparingStatus" }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.warehouse),
                        contentDescription = "Warehouse",
                        tint = Color(0xFF5A7F54),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { }
                            .semantics { testTag = "WarehouseIcon" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The product is being prepared in the warehouse.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.semantics { testTag = "PreparingText" }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (status in listOf("DELIVERING", "COMPLETED")) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.semantics { testTag = "DeliveryDivider" }
                ) {
                    Spacer(modifier = Modifier.width(22.dp))
                    Divider(
                        color = Color(0xFFFFA500),
                        modifier = Modifier
                            .width(2.dp)
                            .height(50.dp)
                            .semantics { testTag = "DividerLine" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics { testTag = "DeliveringStatus" }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.deliveryicon),
                        contentDescription = "DeliveryTruck",
                        tint = Color(0xFF5A7F54),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { }
                            .semantics { testTag = "DeliveryTruckIcon" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The delivery is on the way.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.semantics { testTag = "DeliveringText" }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (status == "COMPLETED") {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.semantics { testTag = "CompletedDivider" }
                ) {
                    Spacer(modifier = Modifier.width(22.dp))
                    Divider(
                        color = Color(0xFFFFA500),
                        modifier = Modifier
                            .width(2.dp)
                            .height(50.dp)
                            .semantics { testTag = "CompletedDividerLine" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics { testTag = "CompletedStatus" }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFFFA500),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { }
                            .semantics { testTag = "LocationIcon" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The order has been received.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.semantics { testTag = "CompletedText" }
                    )
                }
            }
        }
    }
}