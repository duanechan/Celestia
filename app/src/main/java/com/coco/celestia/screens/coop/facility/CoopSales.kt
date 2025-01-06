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

data class SaleItem(
    val id: String,
    val item: String,
    val amount: Double,
    val date: String
)

data class OrderItem(
    val id: String,
    val product: String,
    val quantity: Int,
    val date: String
)

@Composable
fun CoopSales(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Orders") }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Orders", "Sales")

    // Sample Data
    val sampleOrders = remember {
        listOf(
            OrderItem("Order #001", "Tomatoes", 10, "2024-01-01"),
            OrderItem("Order #002", "Potatoes", 5, "2024-01-02"),
            OrderItem("Order #003", "Carrots", 8, "2024-01-03")
        )
    }

    val sampleSales = remember {
        listOf(
            SaleItem("Sale #001", "Rice", 50.0, "2024-01-01"),
            SaleItem("Sale #002", "Corn", 30.0, "2024-01-01"),
            SaleItem("Sale #003", "Fertilizer", 100.0, "2024-01-02")
        )
    }

    // Filtering based on search query
    val filteredOrders = sampleOrders.filter {
        it.product.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.date.contains(searchQuery, ignoreCase = true)
    }

    val filteredSales = sampleSales.filter {
        it.item.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.date.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search...") },
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
                "Sales" -> SalesContent(filteredSales)
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
                    text = order.product,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = "Qty: ${order.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.id,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = order.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    text = "PHP${"%.2f".format(sale.amount)}",
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