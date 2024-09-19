package com.coco.celestia

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.LightGreen
import com.coco.celestia.ui.theme.PurpleGrey40
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FarmerManageOrder(
    mainNavController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel
) {
    val userData by userViewModel.userData.observeAsState()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    LaunchedEffect(Unit) {
        orderViewModel.fetchOrders(
            uid = uid,
            filter = "Vegetable"
        )
        userViewModel.fetchUser(uid)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .background(LightGreen)
                    .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp)
            ) {
                Text(
                    text = "Orders",
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { /* Handle notification click */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.notification_icon),
                        contentDescription = "Notification Icon",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                var text by remember { mutableStateOf("") }
                var active by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp)
                        .background(PurpleGrey40)
                        .padding(top = 10.dp, bottom = 15.dp)
                ) {
                    SearchBar(
                        query = text,
                        onQueryChange = { /* Handle query change */ },
                        onSearch = { /* Handle search */ },
                        active = false,
                        onActiveChange = {},
                        placeholder = { Text(text = "Search...", color = Color.Black, fontSize = 15.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                        modifier = Modifier
                            .width(225.dp)
                            .height(35.dp)){
                        //TO DO
                    }
                }
            }

            Row {
                Text(
                    text = "Order Status",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 0.dp, start = 30.dp)
                )
                Text(
                    text = "Status",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 0.dp, start = 231.dp)
                )
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
                    if (orderData.isEmpty()) {
                        Text("No orders available.")
                    } else {
                        var orderCount = 1
                        orderData.forEach { order ->
                            if (userData == null) {
                                CircularProgressIndicator() // Show loading indicator if userData is not yet ready
                            } else {
                                ManageOrderCards(orderCount, order, userData!!)
                                orderCount++
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageOrderCards(orderCount: Int, order: OrderData, user: UserData) {
    var expanded by remember { mutableStateOf(false) }
    val clientName = "${user.firstname} ${user.lastname}"
    val orderId = order.orderId.substring(5, 9).uppercase()
    val orderStatus = order.status

    Row {
        Card(
            modifier = Modifier
                .width(355.dp)
                .height(125.dp)
                .offset(x = (-16).dp, y = 0.dp)
                .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
            colors = CardDefaults.cardColors(containerColor = LightGreen)
        ) {
            Column(
                Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .size(width = 50.dp, height = 150.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                    ) {
                        Text(
                            text = orderCount.toString(),
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 5.dp, start = 10.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Order ID: $orderId",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 15.dp, start = 10.dp)
                        )

                        Text(
                            text = "Client Name: $clientName",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            modifier = Modifier.padding(top = 0.dp, start = 10.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(0.dp))
        Box(
            modifier = Modifier
                .size(width = 75.dp, height = 120.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when (orderStatus) {
                        "PENDING" -> Color(0xFFFF8C00)
                        "ACCEPTED" -> Color.Green
                        else -> Color.Red
                    }
                )
        ) {
            Text(
                text = orderStatus,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp)
            )
        }
    }
}
