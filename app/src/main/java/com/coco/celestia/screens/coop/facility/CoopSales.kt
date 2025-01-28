package com.coco.celestia.screens.coop.facility

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.SalesState
import com.coco.celestia.viewmodel.SalesViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.SalesData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.coco.celestia.viewmodel.model.OrderItem

@Composable
fun CoopSales(
    navController: NavController,
    facilityName: String,
    userEmail: String,
    isInStore: Boolean,
    modifier: Modifier = Modifier
) {
    val facilityViewModel: FacilityViewModel = viewModel()
    val salesViewModel: SalesViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (facilityState) {
            is FacilityState.LOADING -> LoadingScreen("Loading facilities...")
            is FacilityState.ERROR -> ErrorScreen((facilityState as FacilityState.ERROR).message)
            else -> {
                val userFacility = facilitiesData.find { facility ->
                    facility.emails.contains(userEmail)
                }

                if (userFacility == null) {
                    NoFacilityScreen()
                } else if (userFacility.name != facilityName) {
                    LaunchedEffect(Unit) {
                        if (isInStore) {
                            navController.navigate(Screen.CoopInStoreSales.createRoute(userFacility.name)) {
                                popUpTo(navController.graph.startDestinationId)
                            }
                        } else {
                            navController.navigate(Screen.CoopOnlineSales.createRoute(userFacility.name)) {
                                popUpTo(navController.graph.startDestinationId)
                            }
                        }
                    }
                } else {
                    if (isInStore) {
                        InStoreSalesContentUI(
                            navController = navController,
                            facilityName = facilityName,
                            viewModel = salesViewModel
                        )
                    } else {
                        OnlineSalesContentUI(
                            navController = navController,
                            facilityName = facilityName,
                            viewModel = salesViewModel,
                            orderViewModel = orderViewModel
                        )
                    }
                }
            }
        }
    }
}

//IN-STORE SALES
@Composable
private fun InStoreSalesContentUI(
    navController: NavController,
    facilityName: String,
    viewModel: SalesViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSortDropdown by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("A-Z") }

    val salesState by viewModel.salesState.observeAsState(SalesState.LOADING)
    val salesData by viewModel.salesData.observeAsState(emptyList())

    // Fetch sales for the facility when the component loads
    LaunchedEffect(facilityName) {
        viewModel.fetchSales(facility = facilityName)
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val filteredAndSortedSales = salesData
        .filter {
            searchQuery.isBlank() || listOf(
                it.salesNumber,
                it.productName,
                it.price.toString(),
                it.quantity.toString(),
                it.date,
                it.notes
            ).any { field -> field.contains(searchQuery, ignoreCase = true) }
        }
        .sortedWith(
            when (sortOption) {
                "A-Z" -> compareBy { it.productName }
                "Z-A" -> compareByDescending { it.productName }
                "By Year" -> compareBy { LocalDate.parse(it.date, formatter).year }
                "By Month" -> compareBy { LocalDate.parse(it.date, formatter).withDayOfMonth(1) }
                "By Week" -> compareBy { LocalDate.parse(it.date, formatter).dayOfYear / 7 }
                "By Day" -> compareBy { LocalDate.parse(it.date, formatter) }
                else -> compareBy { it.productName }
            }
        )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White2)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newQuery -> searchQuery = newQuery },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(48.dp),
                    placeholder = {
                        Text(text = "Search sales...")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Green1,
                        focusedBorderColor = Green1,
                        unfocusedBorderColor = Green1,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Sort Icon Button
                Box {
                    IconButton(
                        onClick = { showSortDropdown = !showSortDropdown },
                        modifier = Modifier
                            .background(White1, CircleShape)
                            .size(48.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sort),
                            contentDescription = "Sort",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false },
                        modifier = Modifier.background(White1)
                    ) {
                        DropdownMenuItem(
                            text = { Text("A-Z") },
                            onClick = {
                                sortOption = "A-Z"
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Z-A") },
                            onClick = {
                                sortOption = "Z-A"
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("By Year") },
                            onClick = {
                                sortOption = "By Year"
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("By Month") },
                            onClick = {
                                sortOption = "By Month"
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("By Week") },
                            onClick = {
                                sortOption = "By Week"
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("By Day") },
                            onClick = {
                                sortOption = "By Day"
                                showSortDropdown = false
                            }
                        )
                    }
                }
            }

            when (salesState) {
                SalesState.LOADING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green1)
                    }
                }
                SalesState.EMPTY -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No sales found")
                    }
                }
                is SalesState.ERROR -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((salesState as SalesState.ERROR).message)
                    }
                }
                SalesState.SUCCESS -> {
                    SalesCard(sales = filteredAndSortedSales, navController = navController)
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                navController.navigate(Screen.CoopAddSales.route)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = White1,
            contentColor = Green1
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Sale"
            )
        }
    }
}

@Composable
private fun SalesCard(
    sales: List<SalesData>,
    navController: NavController
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sales) { sale ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.CoopSalesDetails.createRoute(sale.salesNumber))
                    },
                colors = CardDefaults.cardColors(containerColor = Green4)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${sale.productName} (${sale.salesNumber})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Green1,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                        Text(
                            text = sale.date,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${sale.quantity} ${sale.weightUnit}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                        Text(
                            text = "₱${sale.price}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sale.notes.isNotBlank()) {
                        Text(
                            text = sale.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                    } else {
                        Text(
                            text = "No note provided",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }
        }
    }
}


// ONLINE SALES
@Composable
private fun OnlineSalesContentUI(
    navController: NavController,
    facilityName: String,
    viewModel: SalesViewModel,
    orderViewModel: OrderViewModel,
) {
    var selectedTab by remember { mutableStateOf("Orders") }
    var selectedOrderStatus by remember { mutableStateOf("Pending") }
    var searchQuery by remember { mutableStateOf("") }
    var showSortDropdown by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Newest First") }

    val tabs = listOf("Orders", "Sales")
    val statuses = listOf(
        OrderItem("Pending", 0),
        OrderItem("Confirmed", 0),
        OrderItem("To Deliver", 0),
        OrderItem("To Receive", 0),
        OrderItem("Completed", 0),
        OrderItem("Refund Requested", 0),
        OrderItem("Refund Approved", 0),
        OrderItem("Refund Rejected", 0),
        OrderItem("Cancelled", 0)
    )

    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    LaunchedEffect(facilityName) {
        viewModel.fetchSales(facility = facilityName)
        orderViewModel.fetchAllOrders(filter = "", role = facilityName)
    }

    val facilityOrders = orderData.filter { order ->
        order.orderData.any { product -> product.type == facilityName }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Tabs
        Row(
            modifier = Modifier
                .background(Green4)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = { selectedTab = tab },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedTab == tab) Green1
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    }

                    AnimatedVisibility(
                        visible = selectedTab == tab,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    color = White1,
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
        }

        // Box wrapper for all content after tabs
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White2)
                .padding(horizontal = 16.dp)
        ) {
            // Tab Content
            when (selectedTab) {
                "Orders" -> {
                    Column {
                        // Update status counters
                        statuses.forEach { order ->
                            order.totalActivities = facilityOrders.count { it.status == order.status }
                        }

                        // Status filters
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(statuses) { statusItem ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(
                                        onClick = { selectedOrderStatus = statusItem.status },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedOrderStatus == statusItem.status)
                                                Green4
                                            else
                                                MaterialTheme.colorScheme.surface,
                                            contentColor = if (selectedOrderStatus == statusItem.status)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .height(40.dp)
                                    ) {
                                        Text(
                                            text = "${statusItem.status} (${statusItem.totalActivities})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = mintsansFontFamily
                                        )
                                    }
                                }
                            }
                        }

                        // Search and Sort
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .height(48.dp),
                                placeholder = { Text("Search orders...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = if (searchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    cursorColor = Green1,
                                    focusedBorderColor = Green1,
                                    unfocusedBorderColor = Green1
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Box {
                                IconButton(
                                    onClick = { showSortDropdown = !showSortDropdown },
                                    modifier = Modifier
                                        .background(White1, CircleShape)
                                        .size(48.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.sort),
                                        contentDescription = "Sort",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(4.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortDropdown,
                                    onDismissRequest = { showSortDropdown = false },
                                    modifier = Modifier.background(White1)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Newest First") },
                                        onClick = {
                                            sortOption = "Newest First"
                                            showSortDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Oldest First") },
                                        onClick = {
                                            sortOption = "Oldest First"
                                            showSortDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("A-Z") },
                                        onClick = {
                                            sortOption = "A-Z"
                                            showSortDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Z-A") },
                                        onClick = {
                                            sortOption = "Z-A"
                                            showSortDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Filter and sort orders
                        val filteredAndSortedOrders = facilityOrders
                            .filter { order ->
                                when (selectedOrderStatus) {
                                    "Return/Refund" -> order.status in listOf("Refund Requested", "Refund Approved", "Refund Rejected")
                                    else -> order.status == selectedOrderStatus
                                } &&
                                        (searchQuery.isBlank() || listOf(
                                            order.orderId,
                                            order.status,
                                            order.collectionMethod,
                                            order.paymentMethod
                                        ).any { field -> field.contains(searchQuery, ignoreCase = true) }
                                                || order.orderData.any { product ->
                                            listOf(
                                                product.name,
                                                product.price.toString(),
                                                product.quantity.toString()
                                            ).any { field -> field.contains(searchQuery, ignoreCase = true) }
                                        })
                            }
                            .sortedWith(
                                when (sortOption) {
                                    "A-Z" -> compareBy { it.orderId }
                                    "Z-A" -> compareByDescending { it.orderId }
                                    "Newest First" -> compareByDescending { it.timestamp }
                                    "Oldest First" -> compareBy { it.timestamp }
                                    else -> compareByDescending { it.timestamp }
                                }
                            )

                        if (filteredAndSortedOrders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "No Orders",
                                        modifier = Modifier.size(48.dp),
                                        tint = Green1
                                    )
                                    Text(
                                        text = "No orders found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = mintsansFontFamily,
                                        color = Green1,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            OrdersCard(
                                filteredOrders = filteredAndSortedOrders,
                                navController = navController,
                                facilityName = facilityName
                            )
                        }
                    }
                }

                "Sales" -> {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .height(48.dp),
                                placeholder = { Text("Search sales...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = if (searchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    cursorColor = Green1,
                                    focusedBorderColor = Green1,
                                    unfocusedBorderColor = Green1
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Box {
                                IconButton(
                                    onClick = { showSortDropdown = !showSortDropdown },
                                    modifier = Modifier
                                        .background(White1, CircleShape)
                                        .size(48.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.sort),
                                        contentDescription = "Sort",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(4.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortDropdown,
                                    onDismissRequest = { showSortDropdown = false },
                                    modifier = Modifier.background(White1)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Newest First") },
                                        onClick = {
                                            sortOption = "Newest First"
                                            showSortDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Oldest First") },
                                        onClick = {
                                            sortOption = "Oldest First"
                                            showSortDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        when (orderState) {
                            OrderState.LOADING -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Green1)
                                }
                            }
                            OrderState.EMPTY -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No orders found")
                                }
                            }
                            is OrderState.ERROR -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text((orderState as OrderState.ERROR).message)
                                }
                            }
                            OrderState.SUCCESS -> {
                                val completedOrders = facilityOrders
                                    .filter { order -> order.status == "Completed" }
                                    .sortedWith(
                                        when (sortOption) {
                                            "Newest First" -> compareByDescending { it.timestamp }
                                            "Oldest First" -> compareBy { it.timestamp }
                                            else -> compareByDescending { it.timestamp }
                                        }
                                    )

                                if (completedOrders.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No completed orders found",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    OrdersCard(
                                        filteredOrders = completedOrders,
                                        navController = navController,
                                        facilityName = facilityName
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersCard(
    filteredOrders: List<OrderData>,
    navController: NavController,
    facilityName: String
) {
    if (filteredOrders.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredOrders) { order ->
                OrderStatusesCard(
                    order = order,
                    navController = navController,
                    modifier = Modifier.padding(vertical = 8.dp),
                    facilityName = facilityName
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No orders found.")
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun OrderStatusesCard(
    order: OrderData,
    navController: NavController,
    facilityName: String,
    modifier: Modifier = Modifier
) {
    val facilityItems = order.orderData.filter { it.type == facilityName }
    val showAllItems = facilityItems.size <= 2
    val displayItems = if (showAllItems) facilityItems else facilityItems.take(2)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate(Screen.CoopOrderDetails.createRoute(orderId = order.orderId))
            },
        colors = CardDefaults.cardColors(containerColor = Green4)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.orderId,
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = order.orderDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Client Name
            if (order.client.isNotEmpty()) {
                Text(
                    text = order.client,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Items Count and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Items: ${facilityItems.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = order.status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            displayItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var image by remember { mutableStateOf<Uri?>(null) }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        LaunchedEffect(item.productId) {
                            try {
                                ImageService.fetchProductImage(productId = item.productId) {
                                    image = it
                                }
                            } catch(e: Exception) {
                                image = null
                            }
                        }

                        if (image != null) {
                            Image(
                                painter = rememberImagePainter(image),
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.product_icon),
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                        Text(
                            text = "${item.quantity} ${item.weightUnit}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                        Text(
                            text = "PHP ${item.price}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }

            if (!showAllItems) {
                TextButton(
                    onClick = {
                        navController.navigate(Screen.CoopOrderDetails.createRoute(orderId = order.orderId))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green1,
                        fontFamily = mintsansFontFamily
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Collection Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = order.collectionMethod,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
            }

            // Payment Method and Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "${order.paymentMethod} * Unpaid",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = "PHP ${facilityItems.sumOf { it.price }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ItemCard(item: ProductData) {
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(item.productId) {
        isLoading = true
        ImageService.fetchProductImage(item.productId) { uri ->
            productImage = uri
            isLoading = false
        }

        onDispose { }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                color = Green1
            )
            Text(
                text = "${item.quantity} ${item.weightUnit}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = mintsansFontFamily
            )
            Text(
                text = "PHP ${item.price}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = mintsansFontFamily
            )
        }
    }
}