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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.screens.coop.facility.ItemCard
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ClientOrder(
    navController: NavController,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
) {
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    var searchText by remember { mutableStateOf("") }

    val filters = listOf(
        "Pending",
        "Confirmed",
        "To Deliver",
        "To Receive",
        "Completed",
        "Cancelled",
        "Refund Requested",
        "Refund Approved",
        "Refund Rejected",
    )
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        orderViewModel.fetchOrders(
            uid = uid,
            filter = filters[selectedTabIndex]
        )
        userViewModel.fetchUser(uid)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Green4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.padding(start = 16.dp),
                    tint = Green1
                )

                TextField(
                    value = searchText,
                    onValueChange = { newText -> searchText = newText },
                    placeholder = { Text("Search Orders...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Clear Button for Search
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = { searchText = "" },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            tint = Green1
                        )
                    }
                }
            }

            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                containerColor = Green4,
                contentColor = Green1
            ) {
                filters.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = label,
                                fontFamily = mintsansFontFamily,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = Green1,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            // Orders List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                val filteredOrders = orderData.filter { order ->
                    when (selectedTabIndex) {
                        filters.indexOf("Return/Refund") -> order.status in listOf("Refund Requested", "Refund Approved", "Refund Rejected")
                        else -> order.status.equals(filters[selectedTabIndex], ignoreCase = true)
                    } && (searchText.isEmpty() || order.toString().contains(searchText, ignoreCase = true))
                }

                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "No Orders",
                                modifier = Modifier.size(48.dp),
                                tint = Green1
                            )
                            Text(
                                text = "No orders found",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = mintsansFontFamily,
                                color = Green1,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    filteredOrders.forEachIndexed { index, order ->
                        OrderCard(
                            order = order,
                            index = "ORDER-$index",
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderData,
    index: String,
    navController: NavController
) {
    val showAllItems = order.orderData.size <= 2
    val displayItems = if (showAllItems) order.orderData else order.orderData.take(2)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.ClientOrderDetails.createRoute(order.orderId))
            },
        colors = CardDefaults.cardColors(containerColor = White1),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = index,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = order.orderDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Items: ${order.orderData.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = order.status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (order.status) {
                        "Refund Requested" -> Yellow4
                        "Refund Approved" -> Green1
                        "Refund Rejected" -> Cinnabar
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontFamily = mintsansFontFamily
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            displayItems.forEach { product ->
                ItemCard(product)
            }

            if (!showAllItems) {
                TextButton(
                    onClick = {
                        navController.navigate(Screen.ClientOrderDetails.createRoute(order.orderId))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green1,
                        fontFamily = mintsansFontFamily
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.collectionMethod,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = order.paymentMethod,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Total: PHP ${order.orderData.sumOf { it.price }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green1,
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}