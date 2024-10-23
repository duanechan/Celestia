package com.coco.celestia.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ClientDashboard(
    navController: NavController,
    userData: UserData?,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel
) {
    val orderDataState = orderViewModel.orderData.observeAsState()
    val orderStateState = orderViewModel.orderState.observeAsState()
    val orderData = orderDataState.value ?: emptyList()
    val orderState = orderStateState.value ?: OrderState.LOADING

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(
            filter = "",
            role = "Client"
        )
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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                userData?.let { user ->
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

//            // Ensure userData is non-null before passing it to OrderHistory
//            userData?.let {
//                OrderHistory(
//                    orderData = orderData,
//                    orderState = orderState,
//                    userData = it,
//                    navController = navController
//                )
//            }
        }
    }
}

//TODO: will change vegetable icon, and fix the positions here

@Composable
fun BrowseCategories(navController: NavController) {
    Column(
        modifier = Modifier
            .background(VeryDarkGreen, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Browse Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryBox(
                productName = "Coffee",
                iconId = R.drawable.coffeeicon,
                navController = navController,
                iconColor = Color(0xFFB06520)
            )
            CategoryBox(
                productName = "Meat",
                iconId = R.drawable.meaticon,
                navController = navController,
                iconColor = Color(0xFFFF5151)
            )
            CategoryBox(
                productName = "Vegetable",
                iconId = R.drawable.vegetableicon,
                navController = navController,
                iconColor = Color(0xFF41644A)
            )
        }
    }
}

@Composable
fun CategoryBox(
    productName: String,
    iconId: Int,
    navController: NavController,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(100.dp, 120.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
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

        Spacer(modifier = Modifier.height(4.dp))


        Text(
            text = productName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

////TODO: top ordered products of all clients (?)
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

//TODO: show orders that are already delivered + Order history with buy again button
//@Composable
//fun OrderHistory(
//    orderData: List<OrderData>,
//    orderState: OrderState,
//    userData: UserData,
//    navController: NavController
//) {
//    Box(
//        modifier = Modifier
//            .background(VeryDarkGreen, shape = RoundedCornerShape(8.dp))
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//    ) {
//        Column {
//            Text(
//                text = "Order History",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            when (orderState) {
//                is OrderState.LOADING -> {
//                    CircularProgressIndicator(color = Color.White)
//                }
//                is OrderState.ERROR -> {
//                    Text(
//                        text = "Error fetching orders: ${orderState.message}",
//                        color = Color.Red
//                    )
//                }
//                is OrderState.EMPTY -> {
//                    Text(text = "No completed orders found.", color = Color.White)
//                }
//                is OrderState.SUCCESS -> {
//                    if (orderData.isNotEmpty()) {
//                        LazyColumn(modifier = Modifier.fillMaxSize()) {
//                            items(orderData.size) { index ->
//                                val order = orderData[index]
//                                OrderCardDetails(
//                                    orderCount = index + 1,
//                                    order = order,
//                                    user = userData,
//                                    navController = navController
//                                )
//                            }
//                        }
//                    } else {
//                        Text(
//                            text = "No completed orders found.",
//                            fontSize = 16.sp,
//                            color = Color.White
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun OrderCardDetails(
//    orderCount: Int,
//    order: OrderData,
//    user: UserData,
//    navController: NavController
//) {
//    val clientName = "${user.firstname} ${user.lastname}"
//    val orderId = order.orderId.substring(6, 10).uppercase()
//    val orderStatus = order.status
//
//    Row {
//        Card(
//            modifier = Modifier
//                .fillMaxSize()
//                .height(165.dp)
//                .padding(top = 0.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
//                .clickable {
//                    navController.navigate("ClientOrderDetails/${order.orderId}/$orderCount")
//                },
//            colors = CardDefaults.cardColors(containerColor = VeryDarkGreen)
//        ) {
//            Column(
//                Modifier
//                    .padding(16.dp)
//            ) {
//                Row {
//                    Box(
//                        modifier = Modifier
//                            .size(width = 50.dp, height = 150.dp)
//                            .clip(RoundedCornerShape(10.dp))
//                            .background(Color.White),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = orderCount.toString(),
//                            fontSize = 50.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black,
//                            modifier = Modifier.padding(5.dp)
//                        )
//                    }
//
//                    Column {
//                        Text(
//                            text = "Order ID: $orderId",
//                            fontSize = 35.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White,
//                            modifier = Modifier.padding(top = 15.dp, start = 10.dp)
//                        )
//
//                        Text(
//                            text = "Client Name: $clientName",
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.Normal,
//                            color = Color.White,
//                            modifier = Modifier.padding(top = 0.dp, start = 10.dp)
//                        )
//
//                        Text(
//                            text = "Status: $orderStatus",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = LightOrange,
//                            modifier = Modifier.padding(top = 0.dp, start = 10.dp)
//                        )
//
//                        Button(
//                            onClick = {
//                                navController.navigate("Reorder/${order.orderId}")
//                            },
//                            modifier = Modifier.padding(top = 8.dp)
//                        ) {
//                            Text(text = "Buy Again")
//                        }
//                    }
//                }
//            }
//        }
//    }