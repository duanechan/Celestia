package com.coco.celestia.screens.coop.facility

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.SalesState
import com.coco.celestia.viewmodel.SalesViewModel
import com.coco.celestia.viewmodel.model.SalesData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                            viewModel = salesViewModel
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
                .padding(16.dp)
        ) {
            // Search Field and Sort Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Search Field
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
                colors = CardDefaults.cardColors(containerColor = White1)
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
                            color = Green1
                        )
                        Text(
                            text = sale.date,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${sale.quantity} ${sale.weightUnit}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "₱${sale.price}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sale.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${sale.notes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "No note provided",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


// ONLINE SALES
// TODO: Remove this data class later kapag meron na yung ordering sa retail
data class OrderItem(
    val status: String,
    val totalActivities: Int
)
// TODO: I'll modify this kapag naayos na yung ordering sa client side
// TODO: Pati yung Sales tab modify nalang din since currently nagpapakita yung in-store sales
@Composable
private fun OnlineSalesContentUI(
    navController: NavController,
    facilityName: String,
    viewModel: SalesViewModel
) {
    var selectedTab by remember { mutableStateOf("Orders") }
    var selectedOrderStatus by remember { mutableStateOf("Pending") }
    var searchQuery by remember { mutableStateOf("") }
    var showSortDropdown by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("A-Z") }

    val tabs = listOf("Orders", "Sales")
    val statuses = listOf(
        OrderItem("Pending", 3),
        OrderItem("Confirmed", 5),
        OrderItem("Delivering", 2),
        OrderItem("Receiving", 1),
        OrderItem("Completed", 8),
        OrderItem("Cancelled", 0),
        OrderItem("Returned", 1)
    )

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tabs for Orders and Sales
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = { selectedTab = tab },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedTab == tab)
                                Green1
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.titleMedium
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
                                    color = Green1,
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            "Orders" -> {
                // Scrollable Status Tabs
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
                                        White1
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
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Display orders for the selected status
                OrdersCard(
                    filteredOrders = statuses.filter { it.status == selectedOrderStatus }
                )
//                SalesContent(sales = filteredAndSortedSales, navController = navController)
            }




            //Sales Tab
            "Sales" -> {
                // Search Field and Sort Button Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newQuery -> searchQuery = newQuery },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(48.dp),
                        placeholder = { Text(text = "Search sales...") },
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
//                        // Display orders for the selected status
                          // TODO: Orders with "Completed" status will also be displayed in the sales tab
                        OrdersCard(
                            filteredOrders = statuses.filter { it.status == selectedOrderStatus }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersCard(filteredOrders: List<OrderItem>) {
    if (filteredOrders.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredOrders) { order ->
                OrderStatusesCard(order = order, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No orders found for the selected status.")
        }
    }
}

@Composable
fun OrderStatusesCard(
    order: OrderItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = White1)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // First Row: Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "OrderID",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = "Jan 11, 2025 13:11",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Second Row: Items and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Items: 2",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = order.status,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Product Details
            ItemCard()
            ItemCard()

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "Pick Up", //collection method
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "COD * Unpaid", //collection method and payment
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "PHP 200", //total
                    style = MaterialTheme.typography.titleMedium
                )
            }

        }
    }
}


@Composable
fun ItemCard(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add Image Box
        Card(
            modifier = Modifier
                .size(60.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("+ Add\nImage", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall )
            }
        }

        // Product Name and Price
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Potato",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "10 kg", //quantity ordered
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "PHP 100", //total price of order per item
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}