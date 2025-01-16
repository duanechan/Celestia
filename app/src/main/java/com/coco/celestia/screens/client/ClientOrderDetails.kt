package com.coco.celestia.screens.client

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.coop.facility.OnlineItemCard
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.FacilityData
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ClientOrderDetails(
    navController: NavController,
    orderId: String,
    viewModel: OrderViewModel
) {
    val orderState by viewModel.orderState.observeAsState()
    val orderData by viewModel.orderData.observeAsState(emptyList())
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    LaunchedEffect(orderId) {
        viewModel.fetchOrders(uid, "")
    }

    when (orderState) {
        is OrderState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is OrderState.ERROR -> {
            val errorMessage = (orderState as OrderState.ERROR).message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is OrderState.EMPTY -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No order found",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        is OrderState.SUCCESS -> {
            val currentOrder = orderData.find { it.orderId == orderId }

            if (currentOrder == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Order not found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                return
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                // Order details with product list
                OrderDetailsSection(
                    orderData = currentOrder,
                    facilityData = FacilityData()
                )

                // Collection method section
                ClientDetailsCollectionMethod(
                    orderData = currentOrder,
                    facilityData = FacilityData()
                )

                // Payment method section
                ClientDetailsPaymentMethod(
                    orderData = currentOrder,
                    facilityData = FacilityData()
                )

                // Order status tracking
                TrackOrderSection(
                    orderData = currentOrder
                )
            }
        }

        null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun OrderDetailsSection(
    orderData: OrderData,
    facilityData: FacilityData
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = White1)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrderHeader(orderData)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Items (${orderData.orderData.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
            }

            // Scrollable section for products
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp) // Fixed height for exactly 2 items
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(orderData.orderData) { product ->
                        ClientItemCard(product)
                    }
                }
            }

            // Total Amount
            val totalAmount = orderData.orderData.sumOf { it.price * it.quantity }
            Text(
                text = "Total: PHP ${String.format("%.2f", totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}




@Composable
private fun OrderHeader(orderData: OrderData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Order ID and Order Date Layout
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Order ID:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = orderData.orderId,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = orderData.orderDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ClientItemCard(product: ProductData) {
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = White2)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                                Image(
                                    painter = painterResource(R.drawable.product_image),
                                    contentDescription = "Loading",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${product.quantity} ${product.weightUnit.lowercase()} x PHP ${String.format("%.2f", product.price)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "PHP ${String.format("%.2f", product.price * product.quantity)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClientDetailsCollectionMethod(
    orderData: OrderData,
    facilityData: FacilityData
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Collection Method",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = orderData.collectionMethod,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = when (orderData.collectionMethod) {
                    Constants.COLLECTION_PICKUP -> facilityData.pickupLocation
                    Constants.COLLECTION_DELIVERY -> facilityData.deliveryDetails
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun ClientDetailsPaymentMethod(orderData: OrderData, facilityData: FacilityData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = orderData.paymentMethod,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = when (orderData.paymentMethod) {
                    Constants.PAYMENT_CASH -> facilityData.cashInstructions
                    Constants.PAYMENT_GCASH -> facilityData.gcashNumbers
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun TrackOrderSection(orderData: OrderData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White1)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Track Order",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        ClientOrderStatus(
            status = orderData.status,
            statusDescription = orderData.statusDescription,
            dateTime = orderData.orderDate
        )
    }
}

@Composable
fun ClientOrderStatus(
    status: String,
    statusDescription: String,
    dateTime: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}