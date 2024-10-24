package com.coco.celestia.screens.client

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.LightGray
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.RavenBlack
import com.coco.celestia.ui.theme.VeryDarkGreen
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun ClientDashboard(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel
) {
    val userData by userViewModel.userData.observeAsState(UserData())
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("", "Client")
        Log.d("OrderData", "Observed orders: ${orderData.size}")
        delay(1000)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 75.dp)
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    userData.let { user ->
                        Text(
                            text = "Welcome, ${user.firstname} ${user.lastname}!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RavenBlack,
                            modifier = Modifier.weight(1f)
                        )

                    Button(
                        onClick = { /* Handle notification click */ },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.notification_icon),
                            contentDescription = "Notification Icon",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            BrowseCategories(navController)

            Spacer(modifier = Modifier.height(16.dp))
            OrderHistory(
                orderData = orderData,
                orderState = orderState,
                userData = userData,
                navController = navController
            )
        }
    }
}

//TODO: adjust icon and text size 
@Composable
fun BrowseCategories(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.browsecategories),
                contentDescription = "Browse Categories Icon",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(RavenBlack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Browse Categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RavenBlack
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VeryDarkGreen, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryBox(
                    productName = "Coffee",
                    iconId = R.drawable.coffeeicon,
                    navController = navController,
                    iconColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CategoryBox(
                    productName = "Meat",
                    iconId = R.drawable.meaticon,
                    navController = navController,
                    iconColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CategoryBox(
                    productName = "Vegetable",
                    iconId = R.drawable.vegetable,
                    navController = navController,
                    iconColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CategoryBox(
    productName: String,
    iconId: Int,
    navController: NavController,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val gradientBrush = when (productName) {
        "Coffee" -> Brush.linearGradient(
            colors = listOf(Color(0xFFB79276), Color(0xFF91684A))
        )
        "Meat" -> Brush.linearGradient(
            colors = listOf(Color(0xFFD45C5C), Color(0xFFAA3333))
        )
        "Vegetable" -> Brush.linearGradient(
            colors = listOf(Color(0xFF4CB05C), Color(0xFF4F8A45))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color.Gray, Color.LightGray)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(brush = gradientBrush, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable {
                navController.navigate(Screen.OrderDetails.createRoute(productName))
            }
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = "$productName icon",
            modifier = Modifier.size(50.dp),
            colorFilter = ColorFilter.tint(iconColor)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = productName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

//top ordered products of all clients (?)
//@Composable
//fun FeaturedProducts() {
//    Box(
//        modifier = Modifier
//            .background(VeryDarkGreen, shape = RoundedCornerShape(8.dp))
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//    ) {
//        Text(
//            text = "Featured Products",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.White
//        )
//    }
//}

//TODO:  Add buy again button, adjust spacer and icon/text sizes, and add design in error messages
@Composable
fun OrderHistory(
    orderData: List<OrderData>,
    orderState: OrderState,
    userData: UserData,
    navController: NavController
) {
    val completedOrders = orderData.filter { order ->
        order.status.trim().equals("Completed", ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.orderhistory),
                contentDescription = "Order History Icon",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(RavenBlack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Order History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RavenBlack
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (orderState) {
            is OrderState.LOADING -> {
                CircularProgressIndicator(color = Color.White)
            }
            is OrderState.ERROR -> {
                Text(
                    text = "Error fetching orders: ${orderState.message}",
                    color = Color.Red
                )
            }
            is OrderState.EMPTY -> {
                Text(text = "No completed orders found.", color = Color.Red)
            }
            is OrderState.SUCCESS -> {
                if (completedOrders.isNotEmpty()) {
                    LazyColumn {
                        items(completedOrders.size) { index ->
                            val order = completedOrders[index]
                            OrderCardDetails(
                                orderCount = index + 1,
                                order = order,
                                user = userData,
                                navController = navController
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No completed orders found.",
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCardDetails(
    orderCount: Int,
    order: OrderData,
    user: UserData,
    navController: NavController
) {
    val clientName = "${user.firstname} ${user.lastname}"
    val orderId = order.orderId.substring(6, 10).uppercase()
    val orderStatus = order.status

    Row {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .height(165.dp)
                .padding(top = 0.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
                .clickable {
                    navController.navigate("ClientOrderDetails/${order.orderId}/$orderCount")
                },
            colors = CardDefaults.cardColors(containerColor = VeryDarkGreen)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row {
                    Column {
                        Text(
                            text = "Order ID: $orderId",
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 15.dp, start = 10.dp)
                        )

                        Text(
                            text = "Client Name: $clientName",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            modifier = Modifier.padding(top = 0.dp, start = 10.dp)
                        )

                        Text(
                            text = "Status: $orderStatus",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightOrange,
                            modifier = Modifier.padding(top = 0.dp, start = 10.dp)
                        )

//                        Button(
//                            onClick = {
//                                navController.navigate("Reorder/${order.orderId}")
//                            },
//                            modifier = Modifier.padding(top = 8.dp)
//                        ) {
//                            Text(text = "Buy Again")
//                        }
                    }
                }
            }
        }
    }
}