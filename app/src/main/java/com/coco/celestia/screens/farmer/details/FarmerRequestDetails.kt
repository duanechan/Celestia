package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.coco.celestia.screens.farmer.dialogs.PendingStatusDialog
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.ItemState
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FarmerRequestDetails(
    navController: NavController,
    orderId: String,
) {
    val orderViewModel: OrderViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    val uid = FirebaseAuth.getInstance().uid.toString()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val itemState by farmerItemViewModel.itemState.observeAsState(ItemState.LOADING)
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    var farmerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (allOrders.isEmpty()) {
            orderViewModel.fetchAllOrders(filter = "", role = "Farmer")
        }
        if (usersData.isEmpty()) {
            userViewModel.fetchUsers()
        }
        productViewModel.fetchProductByType("Vegetable")
        farmerItemViewModel.getItems(uid = uid)

        if (uid.isNotEmpty()) {
            farmerName = farmerItemViewModel.fetchFarmerName(uid)
        }
    }

    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    when {
        orderState == OrderState.LOADING || productState == ProductState.LOADING || itemState == ItemState.LOADING || userState == UserState.LOADING -> {
            LoadingIndicator()
        }
        orderData == null -> {
            OrderNotFound()
        }
        else -> {
            OrderDetails(
                navController = navController,
                orderData = orderData,
                orderViewModel = orderViewModel,
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
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

@Composable
fun OrderNotFound() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BgColor)
            .semantics { testTag = "android:id/orderNotFound" },
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

@Composable
fun OrderDetails(
    navController: NavController,
    orderData: OrderData,
    orderViewModel: OrderViewModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BgColor)
            .semantics { testTag = "android:id/orderDetailsScreen" }
    ) {
        item {
            OrderDetailsCard(orderData = orderData, navController = navController)
        }

        item {
            PendingStatusDialog(
                orderData = orderData,
                orderViewModel = orderViewModel,
                navController = navController
            )
        }
    }
}


@Composable
fun OrderDetailsCard(orderData: OrderData, navController: NavController) {
    Card(
        shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp),
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
                        top = 10.dp,
                        start = 40.dp,
                        end = 40.dp,
                        bottom = 20.dp
                    )
            ) {
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
                        text = orderData.orderId.substring(6, 10).uppercase(),
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
                        text = "Target Date",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Cocoa,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/orderDateLabel" }
                    )
                    Text(
                        text = orderData.targetDate.ifEmpty { "Not Specified" },
                        color = Cocoa,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/orderDateText" }
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
                                .semantics { testTag = "android:id/orderProductLabel" }
                        )
                    }

                    // Card for Quantity Ordered
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .semantics { testTag = "android:id/orderProductQuantityCard" },
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
                                    .semantics { testTag = "android:id/orderedProductQuantityColumn" }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = orderData.orderData[0].name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 25.sp,
                                        color = Cocoa,
                                        modifier = Modifier.semantics { testTag = "android:id/productNameText" }
                                    )
                                    Text(
                                        text = "${orderData.orderData[0].quantity} kg",
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