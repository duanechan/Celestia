package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth

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

    var searchText by remember { mutableStateOf("") }

    val filters = listOf(
        "Pending",
        "Confirmed",
        "To Deliver",
        "To Receive",
        "Completed",
        "Cancelled",
        "Return/Refund"
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
                    .padding(8.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.padding(start = 16.dp)
                )

                TextField(
                    value = searchText,
                    onValueChange = { newText -> searchText = newText },
                    placeholder = { Text("Search") },
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
                    .padding(horizontal = 8.dp)
            ) {
                val filteredOrders = orderData.filter { order ->
                    order.status.equals(filters[selectedTabIndex], ignoreCase = true) &&
                            (searchText.isEmpty() || order.toString().contains(searchText, ignoreCase = true))
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.ClientOrderDetails.createRoute(order.orderId)) // Use the order parameter
            },
        colors = CardDefaults.cardColors(containerColor = White1),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // First Row: Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order ID: $index",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = order.orderDate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Items: ${order.orderData.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = order.status, // Placeholder status
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Placeholder Items
            order.orderData.forEach { product ->
                ItemCard(product)
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.paymentMethod,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: PHP ${order.orderData.sumOf { it.price }}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ItemCard(item: ProductData) {
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(item.productId) {
        isLoading = true
        ImageService.fetchProductImage(item.productId) { uri ->
            productImage = uri
            isLoading = false
        }

        onDispose { }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading || productImage == null) {
                    Image(
                        painter = painterResource(R.drawable.product_image),
                        contentDescription = "Loading",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = rememberImagePainter(productImage),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${item.quantity} kg",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "PHP ${item.price}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}