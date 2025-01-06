package com.coco.celestia.screens.coop.facility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.coco.celestia.ui.theme.CoopBackground
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.White1
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SaleItem(
    val id: String,
    val item: String,
    val amount: Double,
    val date: String
)

data class OrderItem(
    val status: String,
    val totalActivities: Int
)

@Composable
fun CoopSales(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Orders") }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Orders", "Sales")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Sample Data for Orders and Sales
    val statuses = listOf(
        "Pending", "Confirmed", "To Deliver", "To Receive", "Completed", "Cancelled", "Return/Refund"
    )

    val sampleOrders = remember {
        statuses.map { status ->
            OrderItem(status = status, totalActivities = (0..20).random())
        }
    }

    val sampleSales = remember {
        listOf(
            SaleItem("Sale #001", "Rice", 50.0, "2022-12-01"),
            SaleItem("Sale #002", "Corn", 30.0, "2023-10-01"),
            SaleItem("Sale #003", "Fertilizer", 100.0, "2024-02-02"),
            SaleItem("Sale #004", "Vegetables", 20.0, "2024-01-05")
        )
    }

    // Filtering logic
    val filteredOrders = sampleOrders.filter {
        it.status.contains(searchQuery, ignoreCase = true)
    }

    var sortOption by remember { mutableStateOf("A-Z") }

    val filteredAndSortedSales = sampleSales
        .filter {
            it.item.contains(searchQuery, ignoreCase = true) ||
                    it.id.contains(searchQuery, ignoreCase = true) ||
                    it.date.contains(searchQuery, ignoreCase = true)
        }
        .sortedWith(when (sortOption) {
            "A-Z" -> compareBy { it.item }
            "Z-A" -> compareByDescending { it.item }
            "By Year" -> compareBy { LocalDate.parse(it.date, formatter).year }
            "By Month" -> compareBy { LocalDate.parse(it.date, formatter).withDayOfMonth(1) }
            "By Week" -> compareBy { LocalDate.parse(it.date, formatter).dayOfYear / 7 }
            "By Day" -> compareBy { LocalDate.parse(it.date, formatter) }
            else -> compareBy { it.item }
        })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .height(40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery -> searchQuery = newQuery },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
                    .height(48.dp),
                placeholder = {
                    Text(text = "Search...")},
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
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
                "Orders" -> OrdersContent(filteredOrders)

                "Sales" -> {
                    // Sort Options
                    var expanded by remember { mutableStateOf(false) } // State for dropdown menu

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sort by:", style = MaterialTheme.typography.bodyMedium)

                        Box {
                            Button(
                                onClick = { expanded = !expanded },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green1,
                                    contentColor = White1
                                ),
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(150.dp)
                            ) {
                                Text(text = sortOption)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .background(White1)
                            ) {
                                listOf("A-Z", "Z-A", "By Year", "By Month", "By Week", "By Day").forEach { option ->
                                    DropdownMenuItem(
                                        onClick = {
                                            sortOption = option
                                            expanded = false
                                        },
                                        text = { Text(option) }
                                    )
                                }
                            }
                        }
                    }

                    SalesContent(filteredAndSortedSales)
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { /* Handle navigation to create new entry */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = White1,
            contentColor = Green1
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add"
            )
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
fun SalesContent(filteredSales: List<SaleItem>) {
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
    sale: SaleItem,
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