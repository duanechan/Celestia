package com.coco.celestia.screens.coop.facility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CoopBackground
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// TODO: Lipat ito sa Data.kt
data class SalesData(
    val id: String = "",
    val item: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val inStore: Boolean = true
)

data class OrderItem(
    val status: String,
    val totalActivities: Int
)

@Composable
fun CoopSales(
    navController: NavController,
    facilityName: String,
    userEmail: String,
    isInStore: Boolean,
    modifier: Modifier = Modifier
) {
    val facilityViewModel: FacilityViewModel = viewModel()
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
                        InStoreSalesContentUI(navController, facilityName)
                    } else {
                        OnlineSalesContentUI(navController, facilityName)
                    }
                }
            }
        }
    }
}

@Composable
private fun InStoreSalesContentUI(
    navController: NavController,
    facilityName: String
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSortDropdown by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("A-Z") }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val sampleInStoreSales = remember {
        listOf(
            SalesData("InStoreSale #001", "Rice", 70.0, "2023-11-01"),
            SalesData("InStoreSale #002", "Fertilizer", 120.0, "2023-12-05"),
            SalesData("InStoreSale #003", "Corn", 80.0, "2024-01-10")
        )
    }

    val filteredAndSortedSales = sampleInStoreSales
        .filter {
            // Filter by any detail in the SalesData
            searchQuery.isBlank() || listOf(
                it.id,
                it.item,
                it.amount.toString(),
                it.date
            ).any { field -> field.contains(searchQuery, ignoreCase = true) }
        }
        .sortedWith(
            when (sortOption) {
                "A-Z" -> compareBy { it.item }
                "Z-A" -> compareByDescending { it.item }
                "By Year" -> compareBy { LocalDate.parse(it.date, formatter).year }
                "By Month" -> compareBy { LocalDate.parse(it.date, formatter).withDayOfMonth(1) }
                "By Week" -> compareBy { LocalDate.parse(it.date, formatter).dayOfYear / 7 }
                "By Day" -> compareBy { LocalDate.parse(it.date, formatter) }
                else -> compareBy { it.item }
            }
        )

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

        // Sales Content
        SalesContent(filteredAndSortedSales)
    }
//
//    // Floating Action Button
//    FloatingActionButton(
//        onClick = {
//            navController.navigate(Screen.AddInStoreSale.createRoute(facilityName))
//        },
//        modifier = Modifier
//            .align(Alignment.BottomEnd)
//            .padding(16.dp),
//        containerColor = White1,
//        contentColor = Green1
//    ) {
//        Icon(
//            imageVector = Icons.Default.Add,
//            contentDescription = "Add In-Store Sale"
//        )
//    }
}




// Floating Action Button
//    //TODO: Add nalang this kung kailangan, cinomment out ko muna kase wala pang route ng pag-add
//    FloatingActionButton(
//        onClick = {
//            val route = if (isInStore) {
//                Screen.AddInStoreSale.createRoute(facilityName)
//            } else {
//                Screen.AddOnlineSale.createRoute(facilityName)
//            }
//            navController.navigate(route)
//        },
//        modifier = Modifier
//            .padding(16.dp),
//        containerColor = White1,
//        contentColor = Green1
//    ) {
//        Icon(
//            imageVector = Icons.Default.Add,
//            contentDescription = "Add ${if (isInStore) "In-Store" else "Online"} Sale"
//        )
//    }
@Composable
private fun OnlineSalesContentUI(
    navController: NavController,
    facilityName: String
) {
    var selectedTab by remember { mutableStateOf("Orders") }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Orders", "Sales")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Data for Orders
    val statuses = listOf(
        "Pending", "Confirmed", "To Deliver", "To Receive",
        "Completed", "Cancelled", "Return/Refund"
    )

    val sampleOrders = remember {
        statuses.map { status ->
            OrderItem(status = status, totalActivities = (0..20).random())
        }
    }

    val sampleOnlineSales = remember {
        listOf(
            SalesData("OnlineSale #001", "Corn", 80.0, "2024-01-01"),
            SalesData("OnlineSale #002", "Vegetables", 50.0, "2024-02-10"),
            SalesData("OnlineSale #003", "Rice", 100.0, "2023-12-05"),
            SalesData("OnlineSale #004", "Fertilizer", 200.0, "2023-11-20")
        )
    }

    // Sorting logic
    var showSortDropdown by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("A-Z") }
    val filteredAndSortedSales = sampleOnlineSales
        .filter {
            // Filter by any detail in the SalesData
            searchQuery.isBlank() || listOf(
                it.id,
                it.item,
                it.amount.toString(),
                it.date
            ).any { field -> field.contains(searchQuery, ignoreCase = true) }
        }
        .sortedWith(
            when (sortOption) {
                "A-Z" -> compareBy { it.item }
                "Z-A" -> compareByDescending { it.item }
                "By Year" -> compareBy { LocalDate.parse(it.date, formatter).year }
                "By Month" -> compareBy { LocalDate.parse(it.date, formatter).withDayOfMonth(1) }
                "By Week" -> compareBy { LocalDate.parse(it.date, formatter).dayOfYear / 7 }
                "By Day" -> compareBy { LocalDate.parse(it.date, formatter) }
                else -> compareBy { it.item }
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
            "Orders" -> OrdersContent(sampleOrders)
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
                SalesContent(filteredAndSortedSales)
            }
        }
    }
}

@Composable
fun OrdersContent(filteredOrders: List<OrderItem>) {
    if (filteredOrders.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredOrders) { order ->
                OrderCard(order = order, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No orders found matching your query.")
        }
    }
}

@Composable
fun SalesContent(filteredSales: List<SalesData>) {
    if (filteredSales.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSales) { sale ->
                SaleCard(sale = sale, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No sales found matching your query.")
        }
    }
}

@Composable
fun OrderCard(
    order: OrderItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = White1
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon on the left side
            Icon(
                imageVector = when (order.status) {
                    "Pending" -> Icons.Default.Info
                    "Confirmed" -> Icons.Default.CheckCircle
                    "To Deliver" -> Icons.Default.LocationOn
                    "To Receive" -> Icons.Default.KeyboardArrowDown
                    "Completed" -> Icons.Default.Done
                    "Cancelled" -> Icons.Default.Close
                    "Return/Refund" -> Icons.Default.ArrowBack
                    else -> Icons.Default.Info
                },
                contentDescription = order.status,
                tint = Green1,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${order.totalActivities}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = order.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate to Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SaleCard(
    sale: SalesData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = White1
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sale.item,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = "PHP ${"%.2f".format(sale.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sale.id,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sale.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}