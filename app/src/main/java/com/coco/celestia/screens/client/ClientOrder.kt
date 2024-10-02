package com.coco.celestia.screens.client

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.RavenBlack
import com.coco.celestia.ui.theme.CoffeeBean
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.ui.theme.VeryDarkGreen
import com.coco.celestia.ui.theme.VeryDarkPurple
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun ClientOrder(
    navController: NavController,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel
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
                    .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Order Icon",
                    modifier = Modifier.size(30.dp)
                        .size(35.dp)
                        .align(Alignment.CenterVertically),
                    tint = CoffeeBean
                )
                Spacer(modifier = Modifier.width(8.dp)) //space between text and icon
                Text(
                    text = "Orders",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = RavenBlack
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /* Handle notification click */ },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.notification_icon),
                        contentDescription = "Notification Icon",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            var text by remember { mutableStateOf("") }
            var active by remember { mutableStateOf(false) }
            var selectedStatus by remember { mutableStateOf("All") }
            var expanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
//                    .background(VeryDarkPurple)
                    .padding(top = 10.dp, bottom = 15.dp, start = 25.dp, end = 16.dp)
            ) {
                SearchBar( //not functioning
                    query = text,
                    onQueryChange = { /* Handle query change */ },
                    onSearch = { /* Handle search action */ },
                    active = false,
                    onActiveChange = { },
                    placeholder = { Text(text = "Search...", color = Color.Black, fontSize = 15.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier
                        .width(225.dp)
                        .height(35.dp)
                ){
                }
                //todo: FILTER BY STATUS
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
                    Text("Empty orders.")
                }
                is OrderState.SUCCESS -> {
                    var orderCount = 1
                    orderData.forEach { order ->
                        userData?.let { user ->
                            OrderCards(orderCount, order, user)
                        } ?: run {
                            CircularProgressIndicator() // TODO: Improve UI (Navigate to orders and logout to view)
                        }
                        orderCount++
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        //TODO: make bigger
        FloatingActionButton(
            onClick = {
                navController.navigate(Screen.AddOrder.route)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 100.dp),
            containerColor = LightOrange //change color here for add orders
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Order",
                tint = Color.White
            )
        }
    }

}

@Composable
fun OrderCards(orderCount: Int, order: OrderData, user: UserData) {
    var expanded by remember { mutableStateOf(false) }
    val clientName = "${user.firstname} ${user.lastname}"
    val orderId = order.orderId.substring(5,9).uppercase()
    val orderStatus = order.status

    Row {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .offset(x = (-16).dp, y = 0.dp)
                .padding(top = 0.dp, bottom = 10.dp, start = 27.dp, end = 0.dp),
            colors = CardDefaults.cardColors(containerColor = VeryDarkGreen)
        ) {
            Column(
                Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Row(){
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
                            text = "$orderStatus",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightOrange, // will adjust color
                            modifier = Modifier.padding(top = 0.dp, start = 10.dp)
                        )
                    }

                }

            }
        }
    }
}