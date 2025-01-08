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
import androidx.compose.ui.res.painterResource
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
                    SalesContent(filteredAndSortedSales)
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
private fun SalesContent(sales: List<SalesData>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sales) { sale ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = "${sale.productName} (sales number)",
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
                            text = "Quantity: ${sale.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Price: ₱${sale.price}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sale.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${sale.notes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


// TODO: I'll modify this kapag naayos na yung ordering sa client side
// TODO: Pati yung Sales tab modify nalang din since currently nagpapakita yung in-store sales
@Composable
private fun OnlineSalesContentUI(
    navController: NavController,
    facilityName: String,
    viewModel: SalesViewModel
) {
    var selectedTab by remember { mutableStateOf("Orders") }
    var searchQuery by remember { mutableStateOf("") }
    var showSortDropdown by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("A-Z") }

    val tabs = listOf("Orders", "Sales")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val salesState by viewModel.salesState.observeAsState(SalesState.LOADING)
    val salesData by viewModel.salesData.observeAsState(emptyList())

    // Fetch sales for the facility when the component loads
    LaunchedEffect(facilityName) {
        viewModel.fetchSales(facility = facilityName)
    }

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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Orders functionality coming soon")
                }
            }
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
                        SalesContent(filteredAndSortedSales)
                    }
                }
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

//@Composable
//fun SaleCard(
//    sale: SalesData,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = White1
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 2.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = sale.item,
//                    style = MaterialTheme.typography.titleMedium,
//                    color = Green1
//                )
//                Text(
//                    text = "PHP ${"%.2f".format(sale.amount)}",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = Green1
//                )
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = sale.id,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                Text(
//                    text = sale.date,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}