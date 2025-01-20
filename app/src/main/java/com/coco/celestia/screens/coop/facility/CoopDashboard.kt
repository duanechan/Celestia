package com.coco.celestia.screens.coop.facility

import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.coco.celestia.R
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CoopDashboard(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    facilityViewModel: FacilityViewModel,
) {
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val currentFacility by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)
    val products by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
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

    val facilityOrders = allOrders.filter { order ->
        order.orderData.any { product -> product.type == facilityName }
    }

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = White2
    ) {
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
                Text(
                    text = currentFacilityName.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                if (facilityState is FacilityState.LOADING ||
                    productState is ProductState.LOADING ||
                    orderState is OrderState.LOADING) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Text(
                text = "Items",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Green1,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ItemCard("Active Products", activeProducts.toString())
            ItemCard("Inactive Products", inactiveProducts.toString())

            Text(
                text = "Stock Alerts",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Green1,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ItemCard(
                label = "Low Stocks",
                count = lowStockProducts.size.toString(),
                onClick = { showLowStockDialog = true }
            )

            if (showLowStockDialog) {
                LowStockDialog(
                    products = lowStockProducts,
                    onDismiss = { showLowStockDialog = false }
                )
            }

            Text(
                text = "Order Statuses",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Green1,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SalesActivityCard("Pending", pendingCount.toString(), R.drawable.review)
            SalesActivityCard("Confirmed", confirmedCount.toString(), R.drawable.progress)
            SalesActivityCard("To Deliver", toDeliverCount.toString(), R.drawable.progress)
            SalesActivityCard("To Receive", toReceiveCount.toString(), R.drawable.progress)
            SalesActivityCard("Completed", completedCount.toString(), R.drawable.progress)
            SalesActivityCard("Cancelled", cancelledCount.toString(), R.drawable.cancelled)
            SalesActivityCard("Return/Refund", returnRefundCount.toString(), R.drawable.cancelled)
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
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Green1
                )
            ) {
                Text(
                    "Close",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        title = {
            Text(
                text = "Low Stock Alert",
                fontWeight = FontWeight.Bold,
                color = Green1
            )
        },
        text = {
            Column {
                if (products.isNotEmpty()) {
                    products.forEach { product ->
                        LowStockCard(
                            label = product.name,
                            reorderPoint = "Reorder Point: ${product.reorderPoint}",
                            currentStock = "Current Stock: ${product.quantity}"
                        )
                    }
                } else {
                    Text(
                        text = "No low stock products available.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        },
        containerColor = Green4,
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
                    color = Color.DarkGray
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
                    color = if (currentStock.contains("0")) Cinnabar else Color.Gray
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
                color = Color.DarkGray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = count,
                    fontSize = 14.sp,
                    color = Color.DarkGray
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
fun SalesActivityCard(label: String, count: String, iconResId: Int) {
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
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = count,
                    fontSize = 14.sp,
                    color = Color.DarkGray
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