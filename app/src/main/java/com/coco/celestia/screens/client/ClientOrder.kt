package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CLGText
import com.coco.celestia.ui.theme.ClientBG
import com.coco.celestia.ui.theme.CDarkOrange
import com.coco.celestia.ui.theme.LGContainer
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrder(
    navController: NavController,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
) {
    val userData by userViewModel.userData.observeAsState()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    LaunchedEffect(Unit) {
        orderViewModel.fetchOrders(
            uid = uid,
            filter = "Coffee, Meat, Vegetable"
        )
        userViewModel.fetchUser(uid)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClientBG)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .semantics { testTag = "android:id/ClientOrderColumn" }
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            var text by remember { mutableStateOf("") }
            var selectedStatus by remember { mutableStateOf("All") }
            var expanded by remember { mutableStateOf(false) }
            val statuses = listOf("All", "Pending", "Preparing", "Rejected", "Delivering", "Completed", "Received")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp, start = 25.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SearchBar(
                    query = text,
                    onQueryChange = { newText -> text = newText },
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    placeholder = {
                        Text(
                            text = "Search...",
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    content =  {}
                )

                Box(
                    modifier = Modifier
                        .padding(top = 40.dp)
                ) {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(LGContainer)
                                .padding(17.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = " Filter Icon",
                                tint = Color.White
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(text = status) },
                                onClick = {
                                    selectedStatus = status
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            when (orderState) {
                is OrderState.LOADING -> {
                    Text("Loading orders...")
                }

                is OrderState.ERROR -> {
                    Text("Failed to load orders: ${(orderState as OrderState.ERROR).message}")
                }

                is OrderState.EMPTY -> {
                    Text("No orders available.")
                }

                is OrderState.SUCCESS -> {
                    val filteredOrders = orderData.filter { order ->
                        (selectedStatus == "All" || order.status.equals(selectedStatus, ignoreCase = true)) &&
                                (order.orderId.contains(text, ignoreCase = true) ||
                                        userData?.let { "${it.firstname} ${it.lastname}" }
                                            ?.contains(text, ignoreCase = true) == true)
                    }

                    if (filteredOrders.isEmpty()) {
                        Text(
                            text = "No results.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        var orderCount = 1
                        filteredOrders.forEach { order ->
                            userData?.let { user ->
                                OrderCards(orderCount, order, user, navController)
                            } ?: run {
                                CircularProgressIndicator()
                            }
                            orderCount++
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun OrderCards(orderCount: Int, order: OrderData, user: UserData, navController: NavController) {
    val clientName = "${user.firstname} ${user.lastname}"
    val orderId = order.orderId.substring(6, 10).uppercase()
    val orderStatus = order.status

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, start = 16.dp, end = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(165.dp)
                .clickable {
                    navController.navigate(Screen.ClientOrderDetails.createRoute(order.orderId))
                }
                .semantics { testTag = "android:id/OrderCard_$orderId" },
            colors = CardDefaults.cardColors(containerColor = CDarkOrange)
        ) {
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                // Main content in a Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = 100.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = orderCount.toString(),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .widthIn(max = 40.dp)
                                    .wrapContentSize(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = orderStatus,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CLGText,
                                modifier = Modifier.padding(top = 5.dp, start = 10.dp)
                            )
                            Text(
                                text = "Order ID: $orderId",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                                modifier = Modifier.padding(start = 10.dp)
                            )

                            Text(
                                text = "Client Name: $clientName",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                                modifier = Modifier.padding(top = 5.dp, start = 10.dp)
                            )

                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Navigate to Details",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}