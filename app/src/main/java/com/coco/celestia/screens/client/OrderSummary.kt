package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.coop.admin.EmptyOrders
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun OrderSummary(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    items: List<BasketItem>,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = LocalDateTime.now().format(formatter).toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    var collection by remember { mutableStateOf("") }
    var payment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 16.dp)
    ) {
        if (items.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { UserDetailsHeader() }
                item { FacilityCard(items) }
                item { ClientCollectionMethod(onUpdate = { collection = it }) }
                item { ClientPaymentMethod(onUpdate = { payment = it }) }
//                items(items) { ItemSummaryCard(it) }
                item {
                    OrderSummaryActions(
                        enabled = Pair(
                            first = collection != "" && payment != "",
                            second = if (collection == "" && payment == "") {
                                "Please select a collection and payment method."
                            } else if (collection == "") {
                                "Please select a collection method."
                            } else {
                                "Please select a payment method."
                            }
                        ),
                        totalPrice = items.sumOf { it.price },
                        onPlaceOrder = {
                            // Place the order
                            orderViewModel.placeOrder(
                                uid = uid,
                                order = OrderData(
                                    orderId = "Order-${UUID.randomUUID()}",
                                    orderDate = formattedDateTime,
                                    status = "Pending",
                                    orderData = items.mapNotNull {
                                        ProductData(
                                            productId = it.productId,
                                            name = it.product,
                                            quantity = it.quantity,
                                            price = it.price,
                                            timestamp = it.timestamp,
                                            type = it.productType
                                        )
                                    },
                                    client = userData.firstname + " " + userData.lastname,
                                    collectionMethod = collection,
                                    paymentMethod = payment
                                )
                            )
                            // Navigate the orders screen
                            navController.navigate(Screen.ClientOrder.route)
                            // Clear checkout items from basket
                            userViewModel.clearCheckoutItems(items)
                        },
                        onEvent = { onEvent(it) }
                    )
                }
            }
        } else {
            EmptyOrders()
        }
    }
}

@Composable
fun OrderSummaryActions(
    enabled: Pair<Boolean, String>,
    totalPrice: Double,
    onPlaceOrder: () -> Unit,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total: PHP $totalPrice",
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = {
                    if (enabled.first) {
                        onPlaceOrder()
                        onEvent(
                            Triple(
                                ToastStatus.SUCCESSFUL,
                                "Order placed successfully!",
                                System.currentTimeMillis()
                            )
                        )
                    } else {
                        onEvent(
                            Triple(
                                ToastStatus.WARNING,
                                enabled.second,
                                System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = White1),
                elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
            ) {
                Text(
                    text = "Place Order",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun UserDetailsHeader() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(Green1)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "Diwata Pares", fontWeight = FontWeight.Bold, color = Green1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "#12 Paliparan 3, Cavite",fontSize = 13.sp, color = Green1)
            }
        }
    }
}

@Composable
fun FacilityCard(items: List<BasketItem>){
    Card(
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Facility header information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Facility Name",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = "Order ID",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // List of ItemSummaryCards
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    ItemSummaryCard(item = item)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Total: ", //total ng checked items
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "PHP ${items.sumOf { it.price }}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ItemSummaryCard(item: BasketItem) {
    var image by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(item) {
        try {
            ImageService.fetchProductImage(productId = item.productId) {
                image = it
            }
        } catch(_: Exception) {
            image = null
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .fillMaxSize()
//            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ){
            Row(
                modifier = Modifier.align(Alignment.TopStart)
            ){
                Box(
                    modifier = Modifier
                        .size(90.dp)
                ){
                    Image(
                        painter = if (image != null) {
                            rememberImagePainter(image)
                        } else {
                            painterResource(R.drawable.product_icon)
                        },
                        contentDescription = item.product,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ){
                    Text(
                        text = item.product,
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    Text(
                        text = "${item.quantity}kg x Php ${item.price / item.quantity}", // price per item
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green1
                    )
                    Text(
                        text = "Php ${item.price}", // total price
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                }
            }
        }

    }
}

@Composable
fun ClientCollectionMethod(onUpdate: (String) -> Unit) {
    var selectedMethod by remember { mutableStateOf("") } // State to track selected method

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f) // Allocates space for this group
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Green1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collection Method",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Available In",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Card containing collection method options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = White1),
                elevation = CardDefaults.elevatedCardElevation(4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    listOf(
                        Constants.COLLECTION_PICKUP to "Pick up location here",
                        Constants.COLLECTION_DELIVERY to "Couriers here or etc"
                    ).forEach { (method, description) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMethod = method
                                    onUpdate(selectedMethod)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = {
                                    selectedMethod = method
                                    onUpdate(selectedMethod)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Green1)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray // Optional for better visual distinction
                                )
                            }
                        }
                        Divider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClientPaymentMethod(onUpdate: (String) -> Unit) {
    var selectedMethod by remember { mutableStateOf("") } // State to track selected method

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f) // Allocates space for this group
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Green1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Available In",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Card containing payment options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = White1),
                elevation = CardDefaults.elevatedCardElevation(4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    listOf(
                        Constants.PAYMENT_CASH to "Pay using cash upon delivery.",
                        Constants.PAYMENT_GCASH to "G-Cash number here."
                    ).forEach { (method, description) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMethod = method
                                    onUpdate(selectedMethod)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = {
                                    selectedMethod = method
                                    onUpdate(selectedMethod)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Green1)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray // Optional for better visual distinction
                                )
                            }
                        }
                        Divider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}