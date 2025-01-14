package com.coco.celestia.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrder(
    navController: NavController,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
) {
    val userData by userViewModel.userData.observeAsState()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val uid = FirebaseAuth.getInstance().currentUser ?.uid.toString()

    LaunchedEffect(Unit) {
        orderViewModel.fetchOrders(
            uid = uid,
            filter = ""
        )
        userViewModel.fetchUser (uid)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Green4)
            ) {
                val filters = listOf(
                    "Pending",
                    "Confirmed",
                    "To Deliver",
                    "To Recieve",
                    "Completed",
                    "Cancelled",
                    "Return/Refund"
                )
                var selectedTabIndex by remember { mutableStateOf(0) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color.White, shape = RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    TextField(
                        value = "", //text
                        onValueChange = {}, //newText -> text = newText
                        placeholder = { Text("Search") },
                        modifier = Modifier
                            .weight(1f),
                        maxLines = 1,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp
                ) {
                    filters.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            modifier = Modifier.background(Green4), // Apply Green4 to all tabs
                            text = {
                                Text(
                                    text = label,
                                    fontFamily = mintsansFontFamily,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = Green1,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
    0            }

                Column {
                    orderData.forEachIndexed { index, order ->
                        OrderCard(order = order, index = "ORDER-$index", navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderData,
    index: String,
    navController: NavController
) { // Accepting `order` as a parameter
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.ClientOrderDetails.createRoute(index)) // Use the order parameter
            },
        colors = CardDefaults.cardColors(containerColor = White1),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
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
                    text = "Order ID: $index", // Use the order parameter for the ID
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = "Jan 11, 2025 13:11", // Placeholder date
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
                    text = "Items: ${order.orderData.size}", // Placeholder for item count
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = order.status, // Placeholder status
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Placeholder Items
            order.orderData.forEach { product ->
                ItemCard(product)
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.collectionMethod,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.paymentMethod, // Placeholder for payment status
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: PHP ${order.orderData.sumOf { it.price }}", // Placeholder total
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun ItemCard(item: ProductData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image Placeholder
        Card(
            modifier = Modifier
                .size(60.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+ Add\nImage",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Product Name, Quantity, and Price Placeholders
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.name, // Placeholder for product name
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${item.quantity} kg", // Placeholder for quantity
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "PHP ${item.price}", // Placeholder for price
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}