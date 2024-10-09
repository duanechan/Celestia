package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.screens.farmer.dialogs.FarmerConfirmationDialog
import com.coco.celestia.screens.farmer.dialogs.FarmerDecisionDialog
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.OrderData

@Composable
fun FarmerRequestDetails(
    navController: NavController,
    orderId: String,
    onAccept: () -> Unit,
    onReject: (String) -> Unit
) {
    val orderViewModel: OrderViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        if (allOrders.isEmpty()) {
            orderViewModel.fetchAllOrders(
                filter = "",
                role = "Farmer"
            )
        }
        productViewModel.fetchProductByType("Vegetable")
    }

    val cardWidth = 500.dp

    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    var showDecisionDialog by remember { mutableStateOf(false) }
    var decisionType by remember { mutableStateOf<String?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isOrderAccepted by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf<String?>(null) }

    when {
        orderState == OrderState.LOADING || productState == ProductState.LOADING -> {
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
            val products = orderData.orderData.filter { it.type == "Vegetable" }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5E1CF))
                    .padding(top = 30.dp)
            ) {
                // Order details card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(cardWidth),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE0A83B),
                                        Color(0xFF7A5C20)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                top = 60.dp,
                                start = 40.dp,
                                end = 40.dp,
                                bottom = 20.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "«",
                                    fontSize = 50.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { navController.popBackStack() }
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Order ID",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = orderData.orderId.substring(5, 38),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Delivery Address",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "${orderData.street}, ${orderData.barangay}",
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Date of Order Request",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "${orderData.orderDate.take(10)}${
                                        orderData.orderDate.takeLast(5)}",
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(30.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 70.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Ordered Products Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(5.dp))

                                    Text(
                                        text = "Ordered Products",
                                        color = Color.White,
                                        textAlign = TextAlign.Start,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Products and quantity
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .padding(top = 10.dp, bottom = 8.dp)
                                        .fillMaxHeight(0.2f),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFB06520)),
                                        contentAlignment = if (products.size == 1) Alignment.Center else Alignment.TopStart
                                    ) {
                                        if (products.isNotEmpty()) {
                                            if (products.size == 1) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize(),
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    val product = products[0]
                                                    Text(
                                                        text = product.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 30.sp,
                                                        color = Color.White,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = "${product.quantity} kg",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 30.sp,
                                                        color = Color.White,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    items(products.size) { index ->
                                                        val product = products[index]
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(8.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = product.name,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 30.sp,
                                                                color = Color.White,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Spacer(modifier = Modifier.width(20.dp))
                                                            Text(
                                                                text = "${product.quantity} kg",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 30.sp,
                                                                color = Color.White,
                                                                modifier = Modifier.weight(1f),
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
                                                        Divider(
                                                            color = Color(0x808B4513),
                                                            thickness = 6.dp,
                                                            modifier = Modifier.padding(6.dp)
                                                        )
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

                Spacer(modifier = Modifier.height(20.dp))

                // Inventory check card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(top = 10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE0A83B))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(
                                    top = 10.dp,
                                    bottom = 140.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Items Check",
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(start = 40.dp, end = 40.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF8B4513))
                                ) {
                                    if (products.isNotEmpty()) {
                                        if (products.size == 1) {
                                            val requestedProduct = products[0]
                                            val availableProduct = productData.find {
                                                it.name.equals(
                                                    requestedProduct.name,
                                                    ignoreCase = true
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = requestedProduct.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "${availableProduct?.quantity ?: "N/A"} kg Avail",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = Color.White
                                                )
                                            }
                                        } else {
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                items(products) { requestedProduct ->
                                                    val availableProduct = productData.find {
                                                        it.name.equals(
                                                            requestedProduct.name,
                                                            ignoreCase = true
                                                        )
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(14.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = requestedProduct.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 20.sp,
                                                            color = Color.White,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Text(
                                                            text = "${availableProduct?.quantity ?: "N/A"} kg Avail",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 20.sp,
                                                            color = Color.White,
                                                            modifier = Modifier.weight(1f),
                                                            textAlign = TextAlign.End
                                                        )
                                                    }
                                                    Divider(
                                                        color = Color(0xFF9A5F32),
                                                        thickness = 6.dp,
                                                        modifier = Modifier.padding(6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Check the inventory for more details",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(bottom = 5.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Accept button
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        onClick = {
                                            decisionType = "ACCEPT"
                                            showDecisionDialog = true
                                        },
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF7CC659))
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
                                // Reject button
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        onClick = {
                                            decisionType = "REJECT"
                                            showDecisionDialog = true
                                        },
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE83333))
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
            }
            // Decision dialog
            if (showDecisionDialog && decisionType != null) {
                FarmerDecisionDialog(
                    decisionType = decisionType!!,
                    onConfirm = { reason ->
                        showDecisionDialog = false
                        if (decisionType == "ACCEPT") {
                            isOrderAccepted = true
                            // Update order status to preparing
                            val updatedOrder = orderData.copy(status = "PREPARING")
                            orderViewModel.updateOrder(updatedOrder)
                            onAccept()
                        } else {
                            isOrderAccepted = false
                            rejectionReason = reason
                            // Update order status to rejected with reason
                            val updatedOrder = orderData.copy(status = "REJECTED")
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
    }
}



