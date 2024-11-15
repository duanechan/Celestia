package com.coco.celestia.screens.client

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.NotificationService
import com.coco.celestia.ui.theme.BABText
import com.coco.celestia.ui.theme.BAButton
import com.coco.celestia.ui.theme.CLGText
import com.coco.celestia.ui.theme.ClientBG
import com.coco.celestia.ui.theme.ContainerLO
import com.coco.celestia.ui.theme.CDText
import com.coco.celestia.ui.theme.CDarkOrange
import com.coco.celestia.ui.theme.LGContainer
import com.coco.celestia.ui.theme.SoftCOrange
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientDashboard(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val featuredProducts by productViewModel.featuredProducts.observeAsState(emptyList())
    var notifications = remember { mutableListOf<Notification>() }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        NotificationService.pushNotifications(
            uid = uid,
            onComplete = {
                notifications.clear()
                notifications.addAll(it)
            },
            onError = {

            }
        )
        Log.d("Notifs", notifications.toString())
        orderViewModel.fetchAllOrders("", "Client")
        Log.d("OrderData", "Observed orders: ${orderData.size}")
        delay(1000)
    }

    LaunchedEffect(Unit) {
        productViewModel.fetchFeaturedProducts()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClientBG)
            .semantics { testTag = "android:id/ClientDashboardScreen" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 0.dp)
                .semantics { testTag = "android:id/ClientDashboardColumn" }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 27.dp,
                        bottom = 8.dp,
                        start = 25.dp,
                        end = 25.dp
                    )
                    .semantics { testTag = "android:id/DashboardHeaderRow" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                userData.let { user ->
                    Text(
                        text = "Welcome, ${user.firstname} ${user.lastname}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CDText,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/WelcomeText" }
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (notifications.isNotEmpty()) {
                                    showDialog = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No new notifications",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(LGContainer)
                                    .padding(17.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notification_icon),
                                    contentDescription = "Notification Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            Divider(
                color = Color.Gray.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            BrowseCategories(navController)

            Spacer(modifier = Modifier.height(12.dp))
            FeaturedProducts(featuredProducts, navController)

            Spacer(modifier = Modifier.height(12.dp))
            OrderHistory(
                orderData = orderData,
                orderState = orderState,
                userData = userData,
                navController = navController
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
    if (showDialog) {
        ClientNotification(notifications = notifications, onDismiss = { showDialog = false })
    }
}

@Composable
fun BrowseCategories(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { testTag = "android:id/BrowseCategoriesSection" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ContainerLO, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .semantics { testTag = "android:id/CategoriesContainer" }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .semantics { testTag = "android:id/BrowseCategoriesHeader" }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.browsecategories),
                    contentDescription = "Browse Categories Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(CDText)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Browse Categories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CDText,
                    modifier = Modifier.semantics { testTag = "android:id/BrowseCategoriesText" }
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryBox(
                    productName = "Coffee",
                    iconId = R.drawable.coffeeicon,
                    navController = navController,
                    iconColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "android:id/Category_Coffee" }
                )
                Spacer(modifier = Modifier.width(12.dp))
                CategoryBox(
                    productName = "Meat",
                    iconId = R.drawable.meaticon,
                    navController = navController,
                    iconColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "android:id/Category_Meat" }
                )
                Spacer(modifier = Modifier.width(12.dp))
                CategoryBox(
                    productName = "Vegetable",
                    iconId = R.drawable.vegetable,
                    navController = navController,
                    iconColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "android:id/Category_Vegetable" }
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                Log.d("Product Route", productName)
                navController.navigate(Screen.AddOrder.route)
            }
            .semantics { testTag = "android:id/CategoryBox_$productName" }
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SoftCOrange, shape = CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = "$productName icon",
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(White)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = productName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CDText,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .semantics { testTag = "android:id/CategoryText_$productName" }
        )
    }
}

@Composable
fun FeaturedProducts(
    featuredProducts: List<ProductData>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(ContainerLO, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.featuredproducts),
                contentDescription = "Featured Products Icon",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(CDText)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Featured Products",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CDText
            )
        }

        LazyRow(
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(featuredProducts) { product ->
                ProductTypeCard(product = product, navController = navController)
            }
        }
    }
}

@Composable
fun ProductTypeCard(
    product: ProductData,
    navController: NavController
) {
    val iconId = when (product.type) {
        "Meat" -> R.drawable.meaticon
        "Coffee" -> R.drawable.coffeeicon
        "Vegetable" -> R.drawable.vegetable
        else -> R.drawable.incomplete
    }

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clickable {
                navController.navigate(Screen.AddOrder.route)
            },
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(
            modifier = Modifier
                .background(SoftCOrange)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = "${product.name} icon",
                    modifier = Modifier.size(40.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun OrderHistory(
    orderData: List<OrderData>,
    orderState: OrderState,
    userData: UserData,
    navController: NavController
) {
    val receivedOrders = orderData.filter { order ->
        order.status.trim().equals("Received", ignoreCase = true) ||
        order.status.trim().equals("Completed", ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(ContainerLO, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .semantics { testTag = "android:id/OrderHistory" }
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.orderhistory),
                contentDescription = "Order History Icon",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(CDText)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Order History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CDText,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    navController.navigate("client_order")
                }
            ) {
                Text(text = "See All", color = CLGText)
            }
        }

        when (orderState) {
            is OrderState.LOADING -> CircularProgressIndicator(color = Color.White)
            is OrderState.ERROR -> Text(text = "Error fetching orders: ${orderState.message}", color = Color.Red)
            is OrderState.EMPTY -> Text(text = "No received orders found.", color = Color.Red)
            is OrderState.SUCCESS -> {
                if (receivedOrders.isNotEmpty()) {
                    Column {
                        receivedOrders.take(3).forEach { order ->  // Limit to 3 cards
                            OrderCardDetails(
                                order = order,
                                user = userData,
                                navController = navController
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No orders found.",
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
    order: OrderData,
    user: UserData,
    navController: NavController
) {
    val orderId = order.orderId.substring(6, 10).uppercase()
    val orderStatus = order.status

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(125.dp)
                .clickable {
                    navController.navigate(Screen.ClientOrderDetails.createRoute(order.orderId))
                }
                .padding(end = 8.dp)
                .semantics { testTag = "android:id/OrderCard_$orderId" },
            colors = CardDefaults.cardColors(containerColor = CDarkOrange)
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "RECEIVED",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CLGText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Order ID: $orderId",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Navigate to Details",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
//REMOVED BUY AGAIN
//        Box(
//            modifier = Modifier
//                .height(125.dp)
//                .width(135.dp)
//                .background(BAButton, shape = RoundedCornerShape(8.dp))
//                .padding(8.dp)
//                .clickable {
//                    navController.navigate("add_order/${order.orderData.type}")
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center,
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.buyagain),
//                    contentDescription = "Buy Again Icon",
//                    colorFilter = ColorFilter.tint(BABText),
//                    modifier = Modifier.size(24.dp)
//                )
//                Spacer(modifier = Modifier.height(6.dp))
//                Text(
//                    text = "Buy Again",
//                    color = BABText,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
    }
}