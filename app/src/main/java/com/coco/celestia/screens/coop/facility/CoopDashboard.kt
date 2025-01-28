package com.coco.celestia.screens.coop.facility

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CoopDashboard(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    facilityViewModel: FacilityViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val currentFacility by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val userData by userViewModel.userData.observeAsState()
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)
    val products by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault()) }
    val today = dateFormat.format(Date())

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
        userViewModel.fetchUser(uid)
    }

    val userFacility = when (facilityState) {
        is FacilityState.SUCCESS -> {
            currentFacility.find { facility ->
                facility.emails.any { email -> email == currentUserEmail }
            }
        }
        else -> null
    }

    val facilityName = userFacility?.name
    LaunchedEffect(facilityName) {
        if (facilityName != null) {
            orderViewModel.fetchAllOrders(filter = "", role = facilityName)
            productViewModel.fetchProducts(filter = "", role = "Coop$facilityName")
        }
    }

    val currentFacilityName = when (facilityState) {
        is FacilityState.LOADING -> "LOADING..."
        is FacilityState.EMPTY -> "NO FACILITIES AVAILABLE"
        is FacilityState.ERROR -> "ERROR LOADING FACILITY"
        is FacilityState.SUCCESS -> facilityName?.plus(" FACILITY") ?: "NO FACILITY ASSIGNED"
    }

    val scrollState = rememberScrollState()

    val facilityOrders = if (userFacility != null) {
        allOrders.filter { order ->
            order.orderData.any { product -> product.type == facilityName }
        }
    } else emptyList()

    val pendingCount = facilityOrders.count { it.status == "Pending" }
    val confirmedCount = facilityOrders.count { it.status == "Confirmed" }
    val toDeliverCount = facilityOrders.count { it.status == "To Deliver" }
    val toReceiveCount = facilityOrders.count { it.status == "To Receive" }
    val completedCount = facilityOrders.count { it.status == "Completed" }
    val cancelledCount = facilityOrders.count { it.status == "Cancelled" }
    val returnRefundCount = facilityOrders.count { it.status == "Return/Refund" }

    val activeProducts = products.count { it.isActive }
    val inactiveProducts = products.count { !it.isActive }
    val lowStockProducts = products.filter { it.quantity <= it.reorderPoint }
    var showLowStockDialog by remember { mutableStateOf(false) }
    var showActiveProductsDialog by remember { mutableStateOf(false) }
    var showInactiveProductsDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = White2
    ) {
        when (facilityState) {
            FacilityState.LOADING -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            is FacilityState.ERROR -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = (facilityState as FacilityState.ERROR).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            userFacility?.let { facility ->
                                Icon(
                                    painter = painterResource(id = facility.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = currentFacilityName.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (userFacility != null && (
                                    facilityState is FacilityState.LOADING ||
                                            productState is ProductState.LOADING ||
                                            orderState is OrderState.LOADING
                                    )) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(White1, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = today,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start,
                                color = DarkGreen,
                                fontFamily = mintsansFontFamily
                            )

                            userData?.let { user ->
                                Text(
                                    text = "$greeting, ${user.firstname} ${user.lastname}!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start,
                                    color = DarkGreen,
                                    modifier = Modifier.padding(top = 8.dp),
                                    fontFamily = mintsansFontFamily
                                )
                            }
                        }
                    }

                    Text(
                        text = "Items",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily,
                        color = Green1,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ItemCard(
                        "Active Products",
                        activeProducts.toString(),
                        onClick = { if (userFacility != null) showActiveProductsDialog = true }
                    )
                    ItemCard(
                        "Inactive Products",
                        inactiveProducts.toString(),
                        onClick = { if (userFacility != null) showInactiveProductsDialog = true }
                    )

                    if (showActiveProductsDialog) {
                        ProductListDialog(
                            title = "Active Products",
                            products = products.filter { it.isActive },
                            onDismiss = { showActiveProductsDialog = false },
                            navController = navController
                        )
                    }

                    if (showInactiveProductsDialog) {
                        ProductListDialog(
                            title = "Inactive Products",
                            products = products.filter { !it.isActive },
                            onDismiss = { showInactiveProductsDialog = false },
                            navController = navController
                        )
                    }

                    Text(
                        text = "Stock Alerts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily,
                        color = Green1,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ItemCard(
                        label = "Low Stocks",
                        count = lowStockProducts.size.toString(),
                        onClick = { if (userFacility != null) showLowStockDialog = true }
                    )

                    if (showLowStockDialog) {
                        LowStockDialog(
                            products = lowStockProducts,
                            onDismiss = { showLowStockDialog = false }
                        )
                    }

                    Text(
                        text = "Order Statuses",
                        fontSize = 16.sp,
                        color = Green1,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OrderStatusCard("Pending", pendingCount.toString(), R.drawable.review)
                    OrderStatusCard("Confirmed", confirmedCount.toString(), R.drawable.progress)
                    OrderStatusCard("To Deliver", toDeliverCount.toString(), R.drawable.progress)
                    OrderStatusCard("To Receive", toReceiveCount.toString(), R.drawable.progress)
                    OrderStatusCard("Completed", completedCount.toString(), R.drawable.progress)
                    OrderStatusCard("Cancelled", cancelledCount.toString(), R.drawable.cancelled)
                    OrderStatusCard("Return/Refund", returnRefundCount.toString(), R.drawable.cancelled)
                }
            }
        }
    }
}

@Composable
fun LowStockDialog(
    products: List<ProductData>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Low Stock Alert",
                    fontWeight = FontWeight.Bold,
                    color = Green1,
                    fontFamily = mintsansFontFamily
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Green1
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (products.isNotEmpty()) {
                        items(products) { product ->
                            LowStockCard(
                                label = product.name,
                                reorderPoint = "Reorder Point: ${product.reorderPoint}",
                                currentStock = "Current Stock: ${product.quantity}"
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "No low stock products available.",
                                fontSize = 14.sp,
                                fontFamily = mintsansFontFamily,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = White1,
        titleContentColor = Green1,
        iconContentColor = Green1,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun LowStockCard(label: String, reorderPoint: String, currentStock: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White2),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontFamily = mintsansFontFamily
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = reorderPoint,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = currentStock,
                    fontSize = 14.sp,
                    color = if (currentStock.contains("0")) Cinnabar else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}


@Composable
fun ItemCard(label: String, count: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White1),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = count,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Arrow",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun OrderStatusCard (label: String, count: String, iconResId: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = White1
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = label,
                        tint = Green1,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 12.dp),
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = count,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}

@Composable
fun ProductListDialog(
    title: String,
    products: List<ProductData>,
    onDismiss: () -> Unit,
    navController: NavController
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = White1),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = mintsansFontFamily
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Products Found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontFamily = mintsansFontFamily
                        )
                    }
                } else {
                    LazyColumn {
                        items(products) { product ->
                            ProductListItem(
                                product = product,
                                onClick = {
                                    navController.navigate(Screen.CoopInventoryDetails.createRoute(product.productId))
                                    onDismiss()
                                }
                            )
                            if (product != products.last()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ProductListItem(
    product: ProductData,
    onClick: () -> Unit
) {
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(product.productId) {
        isLoading = true
        ImageService.fetchProductImage(product.productId) { uri ->
            productImage = uri
            isLoading = false
        }
        onDispose { }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading || productImage == null) {
                    Text(
                        text = "+ Add\nImage",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Image(
                        painter = rememberImagePainter(productImage),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${product.quantity} ${product.weightUnit}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "₱${product.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}