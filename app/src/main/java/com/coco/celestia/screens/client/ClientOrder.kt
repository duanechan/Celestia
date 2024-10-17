    package com.coco.celestia.screens.client

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CoffeeBean
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.RavenBlack
import com.coco.celestia.ui.theme.VeryDarkGreen
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
//@Preview
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
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Order Icon",
                    modifier = Modifier.size(35.dp),
                    tint = CoffeeBean
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Orders",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = RavenBlack
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { /* Handle notification click */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.notification_icon),
                        contentDescription = "Notification Icon",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            var text by remember { mutableStateOf("") }
            var selectedStatus by remember { mutableStateOf("All") }
            var expanded by remember { mutableStateOf(false) }
            val statuses = listOf("All", "Pending", "Preparing", "Rejected") //removed 'accept' since status changed to preparing

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 15.dp, start = 25.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = text,
                    onQueryChange = { newText -> text = newText },
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text(text = "Search...", color = Color.Black, fontSize = 15.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .offset(y = -13.dp)
                ){

                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "Filter: $selectedStatus", fontSize = 15.sp, maxLines = 1)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
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
                    .padding(16.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .size(width = 50.dp, height = 150.dp)
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
                            text = orderStatus,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightOrange,
                            modifier = Modifier.padding(top = 0.dp, start = 10.dp)
                        )
                        }
                    }
                }
            }
        }
    }