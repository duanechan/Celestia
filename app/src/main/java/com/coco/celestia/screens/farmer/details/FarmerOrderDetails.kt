package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.Cocoa
import com.coco.celestia.ui.theme.LightApricot
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*

@Composable
fun FarmerOrderDetails(
    navController: NavController,
    orderId: String
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

    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    when {
        orderState == OrderState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = LightApricot),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Cocoa)
            }
        }

        orderData == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = LightApricot),
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
                    .background(color = LightApricot)
                    .padding(top = 80.dp)
            ) {
                // Order details card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    shape = RectangleShape
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Yellow4, Sand)
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Back button and header inside the card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "«",
                                    fontSize = 50.sp,
                                    color = Cocoa,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { navController.popBackStack() }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Order details
                            Text(
                                text = "Order ID #${orderData.orderId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Cocoa,
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(text = "Delivery Address: ${orderData.street}, ${orderData.barangay}", color = Cocoa)

                            Spacer(modifier = Modifier.height(50.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.width(50.dp))
                                LazyColumn(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(products) { product ->
                                        Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Cocoa)
                                        Text(text = "${product.quantity} kg", fontWeight = FontWeight.Bold, fontSize = 40.sp, color = Cocoa)
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

//will add function for tracking orders