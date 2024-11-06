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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.coco.celestia.ui.theme.ContainerGray
import com.coco.celestia.ui.theme.LightGray
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.RavenBlack
import com.coco.celestia.ui.theme.VeryDarkGreen
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import kotlinx.coroutines.delay

//TODO: add vertical scrolling and spacer at the end for the scrolling
// + fix icons sizes of each methods
@Composable
fun ClientDashboard(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel
) {
    val userData by userViewModel.userData.observeAsState(UserData())
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val featuredProducts by productViewModel.featuredProducts.observeAsState(emptyList())
    val notifications by transactionViewModel.notifications.observeAsState(emptyList())
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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
            .background(LightGray)
            .semantics { testTag = "android:id/ClientDashboardScreen" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 75.dp)
                .semantics { testTag = "android:id/ClientDashboardColumn" }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp)
                    .semantics { testTag = "android:id/DashboardHeaderRow" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                userData.let { user ->
                    Text(
                        text = "Welcome, ${user.firstname} ${user.lastname}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RavenBlack,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/WelcomeText" }
                    )

                    Button(
                        onClick = {
                            if (notifications.isNotEmpty()) {
                                // Show the dialog if there are notifications
                                showDialog.value = true
                            } else {
                                // Show a message if there are no notifications
                                Toast.makeText(context, "No new notifications", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .semantics { testTag = "android:id/NotificationButton" }
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
            FeaturedProducts(featuredProducts, navController)

            Spacer(modifier = Modifier.height(12.dp))
            OrderHistory(
                orderData = orderData,
                orderState = orderState,
                userData = userData,
                navController = navController
            )
        }

        // Show the notifications dialog if showDialog is true
        if (showDialog.value) {
            showNotificationsDialog(notifications) {
                showDialog.value = false // Dismiss the dialog
            }
        }
    }
}

@Composable
fun showNotificationsDialog(notifications: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Notifications") },
        text = {
            Column {
                notifications.forEach { notification ->
                    Text(text = notification)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("OK")
            }
        }
    )
}

@Composable
fun BrowseCategories(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { testTag = "android:id/BrowseCategoriesSection" }
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
                colorFilter = ColorFilter.tint(RavenBlack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Browse Categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RavenBlack,
                modifier = Modifier.semantics { testTag = "android:id/BrowseCategoriesText" }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ContainerGray, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .semantics { testTag = "android:id/CategoriesContainer" }
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
            .semantics { testTag = "android:id/CategoryBox_$productName" }
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
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.featuredproducts),
                contentDescription = "Featured Products Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Featured Products",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RavenBlack
            )
        }
        Box(
            modifier = Modifier
                .background(ContainerGray, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(featuredProducts) { product ->
                    ProductTypeCard(product = product, navController = navController)
                }
            }
        }
    }
}

@Composable
fun ProductTypeCard(
    product: ProductData,
    navController: NavController
) {
    val (iconId, gradientBrush) = when (product.type) {
        "Meat" -> Pair(R.drawable.meaticon, Brush.linearGradient(colors = listOf(Color(0xFFD45C5C), Color(0xFFAA3333))))
        "Coffee" -> Pair(R.drawable.coffeeicon, Brush.linearGradient(colors = listOf(Color(0xFFB79276), Color(0xFF91684A))))
        "Vegetable" -> Pair(R.drawable.vegetable, Brush.linearGradient(colors = listOf(Color(0xFF4CB05C), Color(0xFF4F8A45))))
        else -> Pair(R.drawable.incomplete, Brush.linearGradient(colors = listOf(Color.Gray, Color.LightGray)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
                navController.navigate(Screen.OrderDetails.createRoute(product.type))
            },
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
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
                    modifier = Modifier.size(40.dp)
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
    val completedOrders = orderData.filter { order ->
        order.status.trim().equals("Completed", ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { testTag = "android:id/OrderHistory" }
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

        Spacer(modifier = Modifier.height(2.dp))

        when (orderState) {
            is OrderState.LOADING -> CircularProgressIndicator(color = Color.White)
            is OrderState.ERROR -> Text(text = "Error fetching orders: ${orderState.message}", color = Color.Red)
            is OrderState.EMPTY -> Text(text = "No completed orders found.", color = Color.Red)
            is OrderState.SUCCESS -> {
                if (completedOrders.isNotEmpty()) {
                    Column {
                        completedOrders.forEachIndexed { index, order ->
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(ContainerGray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clickable {
                        navController.navigate("ClientOrderDetails/${order.orderId}")
                    }
                    .padding(end = 8.dp)
                    .semantics { testTag = "android:id/OrderCard_$orderId" },
                colors = CardDefaults.cardColors(containerColor = VeryDarkGreen)
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
                            text = "Order ID: $orderId",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "Client Name: $clientName",
                            fontSize = 15.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "$orderStatus",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightOrange,
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

            Box(
                modifier = Modifier
                    .height(140.dp)
                    .width(90.dp)
                    .background(LightOrange, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .clickable {
                        navController.navigate(Screen.OrderDetails.createRoute(order.orderData.type))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.buyagain),
                        contentDescription = "Buy Again Icon",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Buy Again",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}