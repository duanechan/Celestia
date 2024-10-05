package com.coco.celestia.screens.farmer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData

@Composable
fun FarmerRequestDetails(
    navController: NavController,
    orderId: String,
    onAccept: () -> Unit,
    onReject: (String) -> Unit
) {
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    LaunchedEffect(Unit) {
        if (allOrders.isEmpty()) {
            orderViewModel.fetchAllOrders(
                filter = "",
                role = "Farmer"
            )
        }
    }
    val cardWidth = 500.dp

    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    // Variables for dialogs
    var showDecisionDialog by remember { mutableStateOf(false) }
    var decisionType by remember { mutableStateOf<String?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isOrderAccepted by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf<String?>(null) }

    when {
        orderState == OrderState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5E1CF)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF9A5F32))
            }
        }

        orderData == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5E1CF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Order not found",
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        else -> {
            val product = orderData.orderData[0]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5E1CF))
                    .padding(16.dp)
            ) {
                // Back button and header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.height(200.dp))
                    Text(
                        text = "«",
                        fontSize = 50.sp,
                        color = Color(0xFF9A5F32),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { navController.popBackStack() }
                    )
                    Text(
                        text = "Order Request",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A5F32)
                    )
                }

                // Order details card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD279)),
                    modifier = Modifier.width(cardWidth)
                ) {
                    Column(
                        modifier = Modifier.padding(40.dp)
                    ) {
                        Text(
                            text = "Order ID #${orderData.orderId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF6D4A26)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Delivery Address: ${orderData.street}, ${orderData.barangay}")
                        Text(text = "Date of Order Request: ${orderData.orderDate}")

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Product Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "${product.quantity} kg", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Inventory check card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.width(cardWidth)
                ) {
                    Column(
                        modifier = Modifier.padding(30.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Inventory Check", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${product.quantity} kg",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF6D4A26)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Check the inventory for more details",
                            color = Color(0xFF9A5F32),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Accept and reject buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                decisionType = "ACCEPT"
                                showDecisionDialog = true
                            },
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accept Order",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Accept",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D4A26),
                            fontSize = 14.sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                decisionType = "REJECT"
                                showDecisionDialog = true
                            },
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFA2453D))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Reject Order",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Reject",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D4A26),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Decision dialog
    if (showDecisionDialog && decisionType != null) {
        FarmerDecisionDialog(
            decisionType = decisionType!!,
            onConfirm = { reason ->
                showDecisionDialog = false
                if (decisionType == "ACCEPT") {
                    isOrderAccepted = true
                    // Update order status to accepted
                    val updatedOrder = orderData!!.copy(status = "ACCEPTED")
                    orderViewModel.updateOrder(updatedOrder)
                    onAccept()
                } else {
                    isOrderAccepted = false
                    rejectionReason = reason
                    // Update order status to rejected with reason
                    val updatedOrder = orderData!!.copy(status = "REJECTED")
                    orderViewModel.updateOrder(updatedOrder)
                    onReject(reason!!)
                }
                showConfirmationDialog = true
            },
            onDismiss = {
                showDecisionDialog = false
            }
        )
    }

    // Confirmation dialog
    if (showConfirmationDialog) {
        FarmerConfirmationDialog(
            isAccepted = isOrderAccepted,
            rejectionReason = rejectionReason,
            onDismiss = {
                showConfirmationDialog = false
            }
        )
    }
}


