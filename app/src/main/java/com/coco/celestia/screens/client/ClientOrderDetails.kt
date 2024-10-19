package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun ClientOrderDetails(
    navController: NavController,
    orderId: String,
    orderCount: Int
) {
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    LaunchedEffect(Unit) {
        if (allOrders.isEmpty()) {
            orderViewModel.fetchAllOrders(
                filter = "",
                role = "Client"
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
                    .background(color = LightGray),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }


        orderData == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Order not found",
                    color = Copper,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Display the order details when data is available
        else -> {
            val product = orderData.orderData

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp), //adjust size of orange box here
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(650.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(LightOrange),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 90.dp,
                                    start = 16.dp,
                                    end = 16.dp
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                // white box order count
                                Box(
                                    modifier = Modifier
                                        .size(width = 50.dp, height = 117.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = orderCount.toString(),
                                        fontSize = 50.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(5.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Order ID")
                                            }
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                                append("#" + orderData.orderId.substring(5, 38))
                                            }
                                        },
                                        fontSize = 20.sp,
                                        color = White,
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Delivery Address: ")
                                            }
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                                append("${orderData.street}, ${orderData.barangay}")
                                            }
                                        },
                                        fontSize = 20.sp,
                                        color = White,
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Estimated Date of Arrival: ")
                                            }
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                                append("DATE HERE")
                                            }
                                        },
                                        fontSize = 15.sp,
                                        color = White,
                                    )
                                }
                            }

                            // display ordered products
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Ordered Products Icon",
                                    tint = White,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Ordered Products",
                                    color = (White),
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start = 15.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            // products list display
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.width(50.dp))
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 20.dp, end = 50.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.elevatedCardElevation(
                                            defaultElevation = 4.dp
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(TreeBark)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(30.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = product.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 30.sp,
                                                    color = (White),
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "${product.quantity} kg",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 40.sp,
                                                    color = (White),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //TODO: add location function in the clickable icons
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.warehouse),
                                contentDescription = "Warehouse",
                                tint = Color(0xFF5A7F54),
                                modifier = Modifier
                                    .size(50.dp)
                                    .clickable {
                                    }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "The product has already left the warehouse.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Spacer(modifier = Modifier.width(22.dp))
                            Divider(
                                color = Color(0xFFFFA500),
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(50.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.deliveryicon),
                                contentDescription = "DeliveryTruck",
                                tint = Color(0xFF5A7F54),
                                modifier = Modifier
                                    .size(50.dp)
                                    .clickable {
                                    }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "The delivery is on the way.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Spacer(modifier = Modifier.width(22.dp))
                            Divider(
                                color = Color(0xFFFFA500),
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(50.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFFFFA500),
                                modifier = Modifier
                                    .size(50.dp)
                                    .clickable {
                                    }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "The order has been received.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(50.dp))
                }
            }
        }
    }
}