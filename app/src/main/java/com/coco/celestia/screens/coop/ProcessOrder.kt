package com.coco.celestia.screens.coop

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.Cinnabar
import com.coco.celestia.ui.theme.CompletedStatus
import com.coco.celestia.ui.theme.DeliveringStatus
import com.coco.celestia.ui.theme.GreenBeans
import com.coco.celestia.ui.theme.Kiniing
import com.coco.celestia.ui.theme.Packed
import com.coco.celestia.ui.theme.PendingStatus
import com.coco.celestia.ui.theme.PreparingStatus
import com.coco.celestia.ui.theme.RawMeat
import com.coco.celestia.ui.theme.RoastedBeans
import com.coco.celestia.ui.theme.Sorted
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData

@Composable
fun ProcessOrderPanel(
    orderId: String,
    orderViewModel: OrderViewModel,
) {
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    LaunchedEffect(Unit) {
        orderViewModel.fetchOrder(orderId)
    }    

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Spacer(modifier = Modifier.height(35.dp))
        when (orderState) {
            OrderState.EMPTY -> EmptyProcessOrder()
            is OrderState.ERROR -> ProcessOrderError(errorMessage = (orderState as OrderState.ERROR).message)
            OrderState.LOADING -> LoadingProcessOrder()
            OrderState.SUCCESS -> ProcessOrder(order = orderData[0])
        }
    }
}

@Composable
fun EmptyProcessOrder() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "No order found.",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProcessOrderError(errorMessage: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = errorMessage,
            color = Cinnabar,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProcessOrder(order: OrderData) {
    val productViewModel: ProductViewModel = viewModel()
    val productData by productViewModel.productData.observeAsState(emptyList())
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 75.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OrderItem(
                product = order.orderData,
                order = order,
                orderCount = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery Info section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column {
                    Text(
                        text = "Target Delivery:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(text = "Location:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text(text = "December 10, 2024", fontSize = 14.sp)
                    Text(text = "Idjay igid", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Status Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* Handle delivering status */ },
                    colors = ButtonDefaults.buttonColors(containerColor = DeliveringStatus),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(text = "Delivering",
                        color = Color.White,
                        fontFamily = mintsansFontFamily,
                        fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { /* Handle complete status */ },
                    colors = ButtonDefaults.buttonColors(containerColor = CompletedStatus),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(text = "Complete",
                        color = Color.White,
                        fontFamily = mintsansFontFamily,
                        fontWeight = FontWeight.Bold)
                }
            }

        }
    }
}

@Composable
fun OrderItem(product: ProductData,
              order: OrderData,
              orderCount: Int) {

    val orderId = order.orderId.substring(5,9).uppercase()
    val orderClient = order.client

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(16.dp)) {

        Card () {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(),
            ) {
                Row {
                    Text(
                        text = orderCount.toString(),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(5.dp)
                    )
                    Column (modifier = Modifier.padding(16.dp)) {
                        Text(text = "Order ID: $orderId",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold)
                        Text(text = "Client Name:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold)
                        Text(text = "$orderClient",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = "Current Status: ",

                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 5.dp))
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when (order.status) {
                                "PREPARING" -> PreparingStatus
                                "PENDING" -> PendingStatus
                                "Delivering" -> DeliveringStatus
                                "COMPLETED" -> CompletedStatus
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = order.status,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Order: ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 5.dp))
//                Spacer(modifier = Modifier.height(10.dp))
//                Text(text = "${order.street}, ${order.barangay}")
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(text = order.orderDate)
//                Spacer(modifier = Modifier.height(8.dp))
//                Row(
//                    horizontalArrangement = Arrangement.SpaceAround,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                ) {
//                }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (product.name) {
                                "Green Beans" -> GreenBeans
                                "Roasted Beans" -> RoastedBeans
                                "Packaged Beans" -> Packed
                                "Sorted Beans" -> Sorted
                                "Kiniing" -> Kiniing
                                "Raw Meat" -> RawMeat
                                else -> Color.White
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = order.orderData.name,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${order.orderData.quantity}kg",
                                fontSize = 35.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }

            }
        }
    }
}

@Composable
fun LoadingProcessOrder() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}
