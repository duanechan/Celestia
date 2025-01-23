package com.coco.celestia.screens.coop.facility

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.SalesState
import com.coco.celestia.viewmodel.SalesViewModel
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.SalesData
import com.coco.celestia.viewmodel.model.StatusUpdate
import com.coco.celestia.viewmodel.model.TransactionData
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

@Composable
fun CoopSalesDetails(
    navController: NavController,
    userEmail: String,
    salesViewModel: SalesViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val salesNumber = remember {
        navController.currentBackStackEntry?.arguments?.getString("salesNumber") ?: ""
    }

    val orderId = remember {
        navController.currentBackStackEntry?.arguments?.getString("orderId") ?: ""
    }

    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    val salesState by salesViewModel.salesState.observeAsState(SalesState.LOADING)
    val salesData by salesViewModel.salesData.observeAsState(emptyList())

    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val orderData by orderViewModel.orderData.observeAsState(emptyList())

    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)

    val userFacility = facilitiesData.find { facility ->
        facility.emails.contains(userEmail)
    }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    LaunchedEffect(userFacility) {
        userFacility?.let { facility ->
            launch {
                salesViewModel.fetchSales(facility = facility.name)
            }
            launch {
                orderViewModel.fetchAllOrders(filter = "", role = facility.name)
            }
            launch {
                val encodedEmail = encodeEmail(userEmail)
                transactionViewModel.fetchTransactions(encodedEmail, "")
            }
        }
    }

    val currentSale = salesData.find { it.salesNumber == salesNumber }
    val currentOrder = orderData.find { it.orderId == orderId}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            facilityState is FacilityState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green1)
                }
            }
            facilityState is FacilityState.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text((facilityState as FacilityState.ERROR).message)
                }
            }
            userFacility == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No facility found for user")
                }
            }
            else -> {
                when {
                    orderState == OrderState.LOADING ||
                            salesState == SalesState.LOADING -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Green1)
                        }
                    }
                    orderState is OrderState.ERROR -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((orderState as OrderState.ERROR).message)
                        }
                    }
                    salesState is SalesState.ERROR -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((salesState as SalesState.ERROR).message)
                        }
                    }
                    transactionState is TransactionState.ERROR -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((transactionState as TransactionState.ERROR).message)
                        }
                    }
                    else -> {
                        if (currentOrder != null) {
                            OnlineSalesDetails(
                                order = currentOrder,
                                navController = navController,
                                orderViewModel = orderViewModel,
                                transactionViewModel = transactionViewModel
                            )
                        } else if (currentSale != null) {
                            InStoreSalesDetails(
                                sale = currentSale,
                                navController = navController
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Order/Sale not found")
                            }
                        }
                    }
                }
            }
        }
    }
}

//ONLINE
@Composable
fun OnlineSalesDetails(
    order: OrderData,
    navController: NavController,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var currentOrder by remember { mutableStateOf(order) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
            .verticalScroll(rememberScrollState())
    ) {
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
                    text = currentOrder.client,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box {
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Green1)
                    ) {
                        Text("Update Status")
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = "Update Order Status") },
                    text = {
                        UpdateStatusCard(
                            status = currentOrder.status,
                            statusDescription = currentOrder.statusDescription,
                            dateTime = currentOrder.orderDate,
                            statusHistory = currentOrder.statusHistory,
                            onStatusUpdate = { newStatus, newDescription, newHistory ->
                                currentOrder = currentOrder.copy(
                                    status = newStatus,
                                    statusDescription = newDescription,
                                    statusHistory = newHistory
                                )
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // Check if the status is being updated to "Completed"
                                val previousStatus = order.status
                                if (currentOrder.status == "Completed" && previousStatus != "Completed") {
                                    // Record transaction for each product in the order
                                    recordOrderTransaction(currentOrder, transactionViewModel)
                                }

                                orderViewModel.updateOrder(currentOrder)
                                showDialog = false
                            }
                        ) {
                            Text(text = "Save")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                currentOrder = order
                                showDialog = false
                            }
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentOrder.orderId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = currentOrder.orderDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Details Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White1)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Items (${currentOrder.orderData.size})",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Divider(color = Green4, thickness = 1.dp)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentOrder.orderData.forEach { item ->
                        ItemCard(item)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider(color = Green4, thickness = 1.dp)
                    Text(
                        text = "Total: PHP ${currentOrder.orderData.sumOf { it.price }}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        //Collection Method
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collection Method",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Divider(
                color = Green4,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentOrder.collectionMethod,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        //Payment Method
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Divider(
                color = Green4,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentOrder.paymentMethod,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        //Order Status
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

            OrderStatus(
                status = currentOrder.status,
                statusDescription = currentOrder.statusDescription,
                dateTime = currentOrder.orderDate,
                statusHistory = currentOrder.statusHistory
            )
        }
    }
}

@Composable
fun OrderStatus(
    status: String,
    statusDescription: String,
    dateTime: String,
    statusHistory: List<StatusUpdate> = emptyList()
) {
    val allStatuses = listOf(
        "Pending",
        "Confirmed",
        "To Deliver",
        "To Receive",
        "Completed"
    )

    val currentIndex = allStatuses.indexOfFirst { it.equals(status, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        allStatuses.forEachIndexed { index, currentStatus ->
            val isCurrent = index == currentIndex
            val isPast = index < currentIndex

            val statusUpdate = when {
                isCurrent -> {
                    statusHistory.findLast { it.status.equals(currentStatus, ignoreCase = true) }
                        ?: StatusUpdate(
                            status = status,
                            statusDescription = statusDescription,
                            dateTime = dateTime,
                            updatedBy = ""
                        )
                }
                isPast -> statusHistory.findLast {
                    it.status.equals(currentStatus, ignoreCase = true)
                }
                else -> null
            }

            if (isCurrent || isPast) {
                TimelineStep(
                    status = currentStatus,
                    statusDescription = when {
                        isCurrent && statusUpdate?.statusDescription.isNullOrBlank() -> statusDescription
                        else -> statusUpdate?.statusDescription ?: ""
                    },
                    dateTime = statusUpdate?.dateTime ?: dateTime,
                    showInfo = true,
                    isCurrent = isCurrent,
                    isCompleted = isPast,
                    showLine = index < allStatuses.lastIndex
                )
            }
        }
    }
}

@Composable
private fun TimelineStep(
    status: String,
    statusDescription: String,
    dateTime: String,
    showInfo: Boolean,
    isCurrent: Boolean,
    isCompleted: Boolean,
    showLine: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .wrapContentHeight()
                .width(24.dp)
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when {
                            isCurrent || isCompleted -> Color(0xFF28403D)
                            else -> Color.Gray.copy(alpha = 0.3f)
                        },
                        shape = CircleShape
                    )
            ) {
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape)
                            .align(Alignment.Center)
                    )
                }
            }

            // Dynamic vertical connecting line
            if (showLine) {
                Spacer(modifier = Modifier.height(5.dp))
                val lineHeight = if (showInfo) {
                    val additionalHeight = if (statusDescription.isNotBlank() && dateTime.isNotBlank()) {
                        60.dp
                    } else {
                        32.dp
                    }
                    additionalHeight
                } else {
                    24.dp
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(lineHeight)
                        .background(
                            color = if (isCompleted) Color(0xFF28403D)
                            else Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Status details
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isCurrent -> Color(0xFF28403D)
                    isCompleted -> Color(0xFF28403D)
                    else -> Color.Gray
                }
            )

            if (showInfo && statusDescription.isNotBlank()) {
                Text(
                    text = statusDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            if (showInfo && dateTime.isNotBlank()) {
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun UpdateStatusCard(
    status: String,
    statusDescription: String,
    dateTime: String,
    statusHistory: List<StatusUpdate> = emptyList(),
    onStatusUpdate: (String, String, List<StatusUpdate>) -> Unit = { _, _, _ -> }
) {
    var statusValue by remember { mutableStateOf(status) }
    var statusDescriptionValue by remember { mutableStateOf(statusDescription) }
    var expanded by remember { mutableStateOf(false) }
    var isEditingDescription by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
    val currentDateTime = remember { LocalDateTime.now().format(formatter) }

    val statusOptions = mapOf(
        "Pending" to "Your order is pending confirmation.",
        "Confirmed" to "Your order has been confirmed.",
        "To Deliver" to "Your order is to be handed to courier.",
        "To Receive" to "Your order is ready to be picked up/ has been shipped by courier.",
        "Completed" to "Your order has been completed.",
        "Cancelled" to "Your order has been cancelled.",
    )

    val statusOrder = listOf(
        "Pending",
        "Confirmed",
        "To Deliver",
        "To Receive",
        "Completed",
        "Cancelled"
    )

    fun getAvailableStatuses(currentStatus: String): List<String> {
        val currentIndex = statusOrder.indexOf(currentStatus)
        if (currentIndex == -1) return emptyList()

        if (currentStatus == "Cancelled" || currentStatus == "Completed") {
            return emptyList()
        }

        val nextIndex = currentIndex + 1
        if (nextIndex >= statusOrder.size - 1) {
            return listOf("Completed")
        }

        return when (currentStatus) {
            "Pending", "Confirmed" -> listOf(statusOrder[nextIndex], "Cancelled")
            else -> listOf(statusOrder[nextIndex])
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                OutlinedTextField(
                    value = statusValue,
                    onValueChange = {},
                    label = { Text("Status") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.clickable { expanded = true }
                        )
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val availableStatuses = getAvailableStatuses(status)

                    availableStatuses.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                statusValue = option
                                if (!isEditingDescription) {
                                    statusDescriptionValue = statusOptions[option] ?: ""
                                }
                                expanded = false

                                val newUpdate = StatusUpdate(
                                    status = option,
                                    statusDescription = statusDescriptionValue,
                                    dateTime = currentDateTime
                                )

                                val updatedHistory = statusHistory + newUpdate

                                onStatusUpdate(option, statusDescriptionValue, updatedHistory)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = statusDescriptionValue,
                onValueChange = { newValue ->
                    isEditingDescription = true
                    statusDescriptionValue = newValue

                    val newUpdate = StatusUpdate(
                        status = statusValue,
                        statusDescription = newValue,
                        dateTime = currentDateTime
                    )

                    val updatedHistory = statusHistory + newUpdate

                    onStatusUpdate(statusValue, newValue, updatedHistory)
                },
                label = { Text("Description") },
                readOnly = false,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (isEditingDescription) {
                        IconButton(
                            onClick = {
                                isEditingDescription = false
                                statusDescriptionValue = statusOptions[statusValue] ?: ""

                                val newUpdate = StatusUpdate(
                                    status = statusValue,
                                    statusDescription = statusDescriptionValue,
                                    dateTime = currentDateTime
                                )

                                val updatedHistory = statusHistory + newUpdate

                                onStatusUpdate(statusValue, statusDescriptionValue, updatedHistory)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset to default"
                            )
                        }
                    }
                }
            )

            OutlinedTextField(
                value = currentDateTime,
                onValueChange = { },
                readOnly = true,
                label = { Text("Date and Time") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

//INSTORE
@Composable
fun InStoreSalesDetails(sale: SalesData, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
    ) {
        // Header Section
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
                    text = sale.productName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Green1
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sale.salesNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = sale.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Product Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InStorePriceInfoColumn(
                    title = "Selling Price",
                    price = sale.price,
                    weightUnit = sale.weightUnit
                )
                Card(
                    modifier = Modifier.size(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Green4)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+ Add\nImage", textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Details Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White1)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        InStoreDetailsCard(sale = sale)
        NotesCard(sale = sale)
    }
}

@Composable
private fun InStoreDetailsCard(sale: SalesData) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sale",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    Text(
                        text = sale.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(color = Green4, thickness = 1.dp)

                // Item Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "QTY x Price",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${sale.quantity} x PHP${String.format("%.2f", sale.price)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "PHP${String.format("%.2f", sale.quantity * sale.price)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun InStorePriceInfoColumn(
    title: String,
    price: Double,
    weightUnit: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "PHP${String.format("%.2f", price)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "per $weightUnit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotesCard(sale: SalesData){
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Divider(color = Green4, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Display notes here",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

fun encodeEmail(email: String): String {
    return email.replace(".", ",")
}

fun formatDate(dateStr: String): String {
    try {
        val inputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
        val date = LocalDateTime.parse(dateStr, inputFormatter)
        val outputFormatter = DateTimeFormatterBuilder()
            .appendPattern("dd MMM yyyy")
            .toFormatter(Locale.ENGLISH)

        return date.format(outputFormatter)
    } catch (e: Exception) {
        return dateStr
    }
}

fun recordOrderTransaction(
    order: OrderData,
    transactionViewModel: TransactionViewModel
) {
    val completionDate = order.statusHistory
        .findLast { it.status == "Completed" }
        ?.dateTime ?: order.orderDate

    val formattedDate = formatDate(completionDate)

    order.orderData.forEach { product ->
        val transaction = TransactionData(
            transactionId = order.orderId,
            type = "Online Sale",
            date = formattedDate,
            description = "Completed order of ${product.quantity} ${product.weightUnit} of ${product.name}",
            status = "COMPLETED",
            productName = product.name
        )
        val encodedClient = encodeEmail(order.client)
        transactionViewModel.recordTransaction(encodedClient, transaction)
    }
}