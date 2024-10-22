package com.coco.celestia.screens.farmer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FarmerDashboard(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    selectedCategory: String,
    searchQuery: String,
    productViewModel: ProductViewModel = viewModel()
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(filter = "", role = "Farmer")
    }

    when (productState) {
        is ProductState.LOADING -> {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        }
        is ProductState.SUCCESS -> {
            if (productData.isEmpty()) {
                Text("No products available", modifier = Modifier.padding(16.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgColor)
                        .padding(top = 120.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 20.dp, end = 10.dp, top = 20.dp)
                    ) {
                        val dateFormat = SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault())
                        val today = dateFormat.format(Date())

                        Text(
                            text = today,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                            color = Cocoa
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        userData?.let { user ->
                            Text(
                                text = "Welcome, ${user.firstname} ${user.lastname}!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                                color = Cocoa
                            )
                        }
                        Spacer(modifier = Modifier.height(80.dp))


                        Text(
                            text = "Product Stock Levels",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(start = 20.dp),
                            color = Cocoa
                        )

                        StockLevelBarGraph(products = productData)
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Order Listings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 25.dp),
                            color = Cocoa
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OrderStatusSection(
                            navController = navController,
                            userData = userData,
                            orderData = orderData,
                            orderState = orderState,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery
                        )
                    }
                }
            }
        }
        is ProductState.ERROR -> {
            Text(
                text = "Error loading product data: ${(productState as ProductState.ERROR).message}",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
        is ProductState.EMPTY -> {
            Text("No products available", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun OrderStatusSection(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    selectedCategory: String,
    searchQuery: String
) {
    when (orderState) {
        is OrderState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is OrderState.ERROR -> {
            Text(
                "Failed to load orders: ${(orderState as OrderState.ERROR).message}",
                color = Color.Red,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
        is OrderState.EMPTY -> {
            Text(
                "No orders available.",
                modifier = Modifier
                    .padding(16.dp)
            )
        }
        is OrderState.SUCCESS -> {
            val filteredOrders = orderData.filter { order ->
                order.status != "PENDING" && order.status != "REJECTED" &&
                        order.orderId.contains(searchQuery, ignoreCase = true)
            }.take(2)

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching orders found.")
                }
            } else {
                LazyColumn {
                    items(filteredOrders) { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            ManageOrderCards(navController, order, cardWidth = 250.dp, cardHeight = 90.dp)

                            OrderStatusCard(orderStatus = order.status, orderId = order.orderId, cardWidth = 400.dp, cardHeight = 90.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockLevelBarGraph(products: List<ProductData>) {
    val maxQuantity = products.maxOfOrNull { it.quantity } ?: 1

    Column(modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 10.dp)) {
        products.forEach { product ->
            Text(text = product.name, fontSize = 14.sp, color = Cocoa)
            Spacer(modifier = Modifier.height(4.dp))

            var animationPlayed by remember { mutableStateOf(false) }
            val animatedWidth by animateDpAsState(
                targetValue = if (animationPlayed) (product.quantity.toFloat() / maxQuantity.toFloat() * 100).dp else 0.dp,
                animationSpec = tween(durationMillis = 1000)
            )

            LaunchedEffect(Unit) {
                animationPlayed = true
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(animatedWidth)
                        .background(GoldenYellow, shape = RoundedCornerShape(10.dp))
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}


