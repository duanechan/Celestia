package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
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
import com.coco.celestia.ui.theme.*

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
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/loadingIndicator" },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Copper)
            }
        }
        orderData == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/orderNotFound"},
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
            val product = orderData.orderData

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .padding(top = 10.dp)
                    .semantics { testTag = "android:id/orderDetailsScreen" }
            ) {
                item {
                    // Order details card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/orderDetailsCard" },
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Yellow4, Sand)
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(
                                        top = 60.dp,
                                        start = 40.dp,
                                        end = 40.dp,
                                        bottom = 20.dp
                                    )
                            ) {
                                // Back Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "«",
                                        fontSize = 50.sp,
                                        color = Cocoa,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .clickable { navController.popBackStack() }
                                            .semantics { testTag = "android:id/backButton" }
                                    )
                                }

                                // Order Details
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Order ID
                                    Text(
                                        text = "Order ID",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Cocoa,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { testTag = "android:id/orderIdLabel" }
                                    )
                                    Text(
                                        text = orderData.orderId.substring(6, 38),
                                        color = Cocoa,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { testTag = "android:id/orderIdText" }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Delivery Address
                                    Text(
                                        text = "Address",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Cocoa,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { testTag = "android:id/deliveryAddressLabel" }
                                    )
                                    Text(
                                        text = "${orderData.street}, ${orderData.barangay}",
                                        color = Cocoa,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { testTag = "android:id/deliveryAddressText" }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Date of Order Request
                                    Text(
                                        text = "Date of Order Request",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Cocoa,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { testTag = "android:id/orderDateLabel" }
                                    )
                                    Text(
                                        text = orderData.orderDate,
                                        color = Cocoa,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { testTag = "android:id/oderDateText" }
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Ordered Products
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 60.dp)
                                            .semantics { testTag = "android:id/orderProductRow" }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = "Ordered Products Icon",
                                            tint = Cocoa,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .semantics { testTag = "android:id/shoppingCartIcon" }
                                        )

                                        Spacer(modifier = Modifier.width(5.dp))

                                        Text(
                                            text = "Ordered Product",
                                            color = Cocoa,
                                            textAlign = TextAlign.Start,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            modifier = Modifier
                                                .padding(start = 10.dp)
                                                .semantics { testTag = "android:id/orderProductLabel"}
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(5.dp))

                                    // Ordered Product Card
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .semantics { testTag = "android:id/orderProductCard" },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(Apricot2, Copper)
                                                    )
                                                )
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 15.dp, bottom = 15.dp)
                                                    .semantics { testTag = "android:id/orderedProductColumn" }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = product.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 25.sp,
                                                        color = Cocoa,
                                                        modifier = Modifier.semantics { testTag = "android:id/productNameText" }
                                                    )
                                                    Text(
                                                        text = "${product.quantity} kg",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 25.sp,
                                                        color = Cocoa,
                                                        textAlign = TextAlign.End,
                                                        modifier = Modifier.semantics { testTag = "android:id/productQuantityText" }
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

                // Inventory Check Card
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/invenotryCheckCard" },
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(color = Yellow4)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(
                                        top = 20.dp,
                                        bottom = 160.dp
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Inventory Check",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    color = Cocoa,
                                    modifier = Modifier.semantics { testTag = "android:id/inventoryCheckLabel" }
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 60.dp, max = 130.dp)
                                        .padding(start = 40.dp, end = 40.dp)
                                        .semantics { testTag = "android:id/inventoryCheckProductCard" },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Apricot2, Sand)
                                                )
                                            )
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .semantics { testTag = "android:id/inventoryCheckProductColumn" }
                                        ) {
                                            val availableProduct = productData.find {
                                                it.name.equals(
                                                    product.name,
                                                    ignoreCase = true
                                                )
                                            }
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .fillMaxWidth()
//                                                    .padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = product.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 30.sp,
                                                    color = Cocoa,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.semantics { testTag = "android:id/inventoryProductNameText" }
                                                )
                                                Spacer(modifier = Modifier.height(20.dp))
                                                Text(
                                                    text = "${availableProduct?.quantity ?: "N/A"} kg",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 30.sp,
                                                    color = Cocoa,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.semantics { testTag = "android:id/inventoryProductQuantityText" }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Check the inventory for more details",
                                    color = Cocoa,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .padding(bottom = 5.dp)
                                        .semantics { testTag = "android:id/inventoryCheckHint" }
                                )

                                Spacer(modifier = Modifier.height(15.dp))

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
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(color = SageGreen)
                                                .semantics { testTag = "android:id/acceptButton" }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Accept Order",
                                                tint = Cocoa,
                                            )
                                        }
                                        Text(
                                            text = "Accept",
                                            fontWeight = FontWeight.Bold,
                                            color = Cocoa,
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
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(color = Copper)
                                                .semantics { testTag = "android:id/rejectButton" }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Reject Order",
                                                tint = Cocoa
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
                    navController = navController,
                    isAccepted = isOrderAccepted,
                    rejectionReason = rejectionReason,
                    onDismiss = {
                        showConfirmationDialog = false
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}