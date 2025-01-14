@file:OptIn(ExperimentalFoundationApi::class)

package com.coco.celestia.screens.farmer

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.viewmodel.ProductViewModel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import kotlinx.coroutines.launch

@Composable
fun FarmerManageOrder(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
) {
    val userData by userViewModel.userData.observeAsState()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val productData by productViewModel.productData.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val farmerItemViewModel: FarmerItemViewModel = viewModel()

    var selectedSection by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(filter = "", role = "Farmer")
        productViewModel.fetchProducts(filter = "", role = "Farmer")
        userViewModel.fetchUser(uid)
        if (uid.isNotEmpty()) {
            farmerName = farmerItemViewModel.fetchFarmerName(uid)
        }
    }

    Spacer(modifier = Modifier.width(30.dp))

    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        val pagerState = rememberPagerState (
            pageCount = { 2 }
        )
        val coroutineScope = rememberCoroutineScope()

        TabRow(
            selectedTabIndex = pagerState.pageCount,
            containerColor = Green4,
            contentColor = Green1,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = Green1
                )
            }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                text = {
                    Text(text = "In Progress")
                },
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            )

            Tab(
                selected = pagerState.currentPage == 1,
                text = {
                    Text(text = "Completed")
                },
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color = BgColor)
                    .verticalScroll(rememberScrollState())
                    .semantics { testTag = "android:id/farmerManageOrderColumn" }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .semantics { testTag = "android:id/farmerManageOrderSearchRow" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search orders...", color = DarkGreen) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Cocoa
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .background(color = White2, shape = RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, color = DarkGreen), shape = RoundedCornerShape(16.dp))
                            .semantics { testTag = "android:id/searchBar" },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            focusedTextColor = Cocoa, // Text color when TextField is focused
                            unfocusedTextColor = DarkGreen // Text color when TextField is not focused
                        )
                    )
                }
            }
        }
    }
}

//DONT REMOVE ATM
@Composable
fun FarmerManageRequest(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    searchQuery: String,
    selectedCategory: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (orderState) {
            is OrderState.LOADING -> {
                Text("Loading pending orders...")
            }
            is OrderState.ERROR -> {
                Text("Failed to load orders: ${orderState.message}")
            }
            is OrderState.EMPTY -> {
                Text("No pending orders available.")
            }
            is OrderState.SUCCESS -> {
                val filteredOrders = orderData
                    .filter { order ->
                        order.status.equals("PENDING", ignoreCase = true) ||
                                (order.status == "PARTIALLY_FULFILLED" &&
                                        order.orderData[0].quantity - order.partialQuantity != 0)
                    }
                    .filter { order ->
                        order.orderId.contains(searchQuery, ignoreCase = true) &&
                                (selectedCategory.isEmpty() || order.orderData[0].name.equals(selectedCategory, ignoreCase = true))
                    }
                Log.d("orders", filteredOrders.toString())
                if (filteredOrders.isEmpty()) {
                    Text("No pending orders available.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        filteredOrders.forEach { order ->
                            if (userData == null) {
                                CircularProgressIndicator()
                            } else {
                                RequestCards(navController, order)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageOrderCards(
    navController: NavController,
    order: OrderData,
    farmerName: String,
    cardHeight: Dp = 150.dp,
    showStatus: Boolean = true
) {
    val clientName = order.client
    val orderId = order.orderId
    var displayStatus by remember { mutableStateOf("") }
    val fulfilledByFarmer = order.fulfilledBy.find { it.farmerName == farmerName }

    if (order.status == "PARTIALLY_FULFILLED") {
        if (fulfilledByFarmer != null) {
            displayStatus = fulfilledByFarmer.status
        }
    } else {
        displayStatus = if (order.status == "RECEIVED") "COMPLETED" else order.status
    }

    val backgroundColor = when (displayStatus) {
        "ACCEPTED" -> SageGreen
        "PLANTING" -> Tangerine
        "PLANTED" -> DeepTangerine
        "GROWING" -> BrownTangerine
        "READY FOR HARVEST" -> Brown2
        "HARVESTING" -> Brown3
        "HARVESTED" -> Brown1
        "DELIVERING" -> Green
//        "PICKUP" -> Blue
        "COMPLETED" -> SageGreen.copy(alpha = 0.7f)
        "CALAMITY AFFECTED" -> SolidRed
        "REJECTED" -> NylonRed.copy(alpha = 0.4f)
        "CANCELLED" -> Copper3
//        "HARVESTING_MEAT" -> Brown1
        else -> Color.Gray
    }

    val iconPainter: Painter? = when (displayStatus) {
        "ACCEPTED" -> painterResource(id = R.drawable.preparing)
        "PLANTING" -> painterResource(id = R.drawable.plant_hand)
        "PLANTED" -> painterResource(id = R.drawable.plant)
        "GROWING" -> painterResource(id = R.drawable.planting)
        "READY FOR HARVEST" -> painterResource(id = R.drawable.harvest)
        "HARVESTING" -> painterResource(id = R.drawable.harvest_basket)
        "HARVESTED" -> painterResource(id = R.drawable.harvested)
//        "HARVESTING_MEAT" -> painterResource(id = R.drawable.cow_animal)
        "DELIVERING" -> painterResource(id = R.drawable.deliveryicon)
//        "PICKUP" -> painterResource(id = R.drawable.pickup)
        "CANCELLED" -> painterResource(id = R.drawable.cancelled)
        "CALAMITY AFFECTED" -> painterResource(id = R.drawable.calamity)
        else -> null
    }

    val iconVector: ImageVector = when (displayStatus) {
        "REJECTED" -> Icons.Default.Clear
        "COMPLETED" -> Icons.Default.CheckCircle
        else -> Icons.Default.Warning
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .semantics { testTag = "android:id/manageOrderRow_${order.orderId}" }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .semantics { testTag = "android:id/manageOrderCard+${order.orderId}" },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Green3, Green4)
                        )
                    )
                    .fillMaxSize()
                    .clickable {
                        navController.navigate(Screen.FarmerOrderDetails.createRoute(order.orderId))
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (showStatus) {
                            Box(
                                modifier = Modifier
                                    .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .semantics { testTag = "android:id/orderStatusRow_${order.orderId}" }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (iconPainter != null) {
                                        Icon(
                                            painter = iconPainter,
                                            contentDescription = displayStatus,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .semantics { testTag = "android:id/statusIcon_${order.orderId}" },
                                            tint = Cocoa
                                        )
                                    } else {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = displayStatus,
                                            tint = Cocoa,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .semantics { testTag = "android:id/statusIcon_${order.orderId}" }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = displayStatus.replace("_", " "),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Cocoa,
                                        modifier = Modifier
                                            .semantics { testTag = "android:id/statusText_${order.orderId}" }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 10.dp)
                                .semantics { testTag = "android:id/orderInfoColumn_${order.orderId}" },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Order ID: $orderId",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/orderIdText_${order.orderId}" }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Client Name: $clientName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/clientNameText_${order.orderId}" }
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically),
                        tint = Cocoa
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun RequestCards(
    navController: NavController,
    order: OrderData,
) {
    val clientName = order.client
    val orderId = order.orderId.substring(6, 10).uppercase()
    val displayStatus = when (order.status) {
        "PENDING" -> "PENDING"
        "PARTIALLY_FULFILLED" -> "PARTIALLY FULFILLED"
        else -> "Unknown"
    }

    val backgroundColor = when (order.status) {
        "PENDING" -> Sand2
        "PARTIALLY_FULFILLED" -> Tangerine
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .semantics { testTag = "android:id/requestRow_${order.orderId}" }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .semantics { testTag = "android:id/requestCard_${order.orderId}" },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PaleGold, GoldenYellow)
                        )
                    )
                    .fillMaxSize()
                    .clickable {
                        navController.navigate(Screen.FarmerRequestDetails.createRoute(order.orderId))
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {

                        Box(
                            modifier = Modifier
                                .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .semantics { testTag = "android:id/requestStatusBox_${order.orderId}" }
                        ) {
                            Text(
                                text = displayStatus,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa,
                                modifier = Modifier
                                    .semantics { testTag = "android:id/requestStatusText_${order.orderId}" }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 10.dp)
                                .semantics { testTag = "android:id/requestInfoColumn_${order.orderId}" },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Order ID: $orderId",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/requestOrderIdText_${order.orderId}" }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Client Name: $clientName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/requestClientNameText_${order.orderId}" }
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Forward",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically),
                        tint = Cocoa
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}