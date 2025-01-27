package com.coco.celestia.screens.coop.facility

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.coco.celestia.R
import com.coco.celestia.screens.client.FileAttachment
import com.coco.celestia.screens.coop.admin.DisplayAttachments
import com.coco.celestia.screens.coop.facility.forms.SalesAddForm
import com.coco.celestia.service.AttachFileService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
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
    transactionViewModel: TransactionViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
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
    val currentOrder = orderData.find { it.orderId == orderId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            facilityState is FacilityState.LOADING -> {
                LoadingIndicator()
            }
            facilityState is FacilityState.ERROR -> {
                ErrorMessage(message = (facilityState as FacilityState.ERROR).message)
            }
            userFacility == null -> {
                ErrorMessage(message = "No facility found for user")
            }
            else -> {
                when {
                    orderState == OrderState.LOADING ||
                            salesState == SalesState.LOADING -> {
                        LoadingIndicator()
                    }
                    orderState is OrderState.ERROR -> {
                        ErrorMessage(message = (orderState as OrderState.ERROR).message)
                    }
                    salesState is SalesState.ERROR -> {
                        ErrorMessage(message = (salesState as SalesState.ERROR).message)
                    }
                    transactionState is TransactionState.ERROR -> {
                        ErrorMessage(message = (transactionState as TransactionState.ERROR).message)
                    }
                    else -> {
                        if (currentOrder != null) {
                            OnlineSalesDetails(
                                order = currentOrder,
                                facilityName = userFacility.name,
                                navController = navController,
                                orderViewModel = orderViewModel,
                                transactionViewModel = transactionViewModel
                            )
                        } else if (currentSale != null) {
                            InStoreSalesDetails(
                                sale = currentSale,
                                navController = navController,
                                viewModel = salesViewModel,
                                productViewModel = productViewModel,
                                transactionViewModel = transactionViewModel,
                                facilityName = userFacility.name,
                                userRole = userFacility.name
                            )
                        } else {
                            ErrorMessage(message = "Order/Sale not found")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Green1)
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = mintsansFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

//ONLINE
@Composable
fun OnlineSalesDetails(
    order: OrderData,
    navController: NavController,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel,
    facilityName: String
) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var currentOrder by remember { mutableStateOf(order) }
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Keep track of which states should prevent further updates
    val isStatusFinal = currentOrder.status == "Rejected" ||
            currentOrder.status == "Cancelled" ||
            currentOrder.status == "Completed"

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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }

            if (showDialog && !isStatusFinal) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = "Update Order Status") },
                    text = {
                        Column {
                            UpdateStatusCard(
                                status = currentOrder.status,
                                statusDescription = currentOrder.statusDescription,
                                dateTime = currentOrder.orderDate,
                                collectionMethod = currentOrder.collectionMethod,
                                statusHistory = currentOrder.statusHistory,
                                onStatusUpdate = { newStatus, newDescription, newHistory ->
                                    currentOrder = currentOrder.copy(
                                        status = newStatus,
                                        statusDescription = newDescription,
                                        statusHistory = newHistory
                                    )
                                }
                            )

                            if (currentOrder.status == "To Receive" &&
                                order.status == "To Deliver" &&
                                currentOrder.collectionMethod == "DELIVERY"
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Upload Proof of Delivery (Images only)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val launcher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.GetMultipleContents()
                                ) { uris: List<Uri>? ->
                                    uris?.let { newUris ->
                                        if (newUris.size + selectedFiles.size > 5) {
                                            Toast.makeText(
                                                context,
                                                "Maximum 5 images allowed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@let
                                        }

                                        val imageUris = newUris.filter { uri ->
                                            val type = context.contentResolver.getType(uri)
                                            type?.startsWith("image/") == true
                                        }

                                        if (imageUris.size < newUris.size) {
                                            Toast.makeText(
                                                context,
                                                "Only image files are allowed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        if (imageUris.isNotEmpty()) {
                                            val oversizedFiles = imageUris.filter { uri ->
                                                context.contentResolver.openInputStream(uri)?.use {
                                                    it.available() > 5 * 1024 * 1024 // 5MB
                                                } ?: false
                                            }

                                            if (oversizedFiles.isNotEmpty()) {
                                                Toast.makeText(
                                                    context,
                                                    "Some images exceed 5MB limit",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@let
                                            }

                                            selectedFiles = (selectedFiles + imageUris).distinct()
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    selectedFiles.forEach { uri ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Green1.copy(alpha = 0.1f))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.image),
                                                    contentDescription = "Image",
                                                    tint = Green1,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = uri.lastPathSegment ?: "Image",
                                                    color = Green1,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    selectedFiles = selectedFiles.filter { it != uri }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = Green1
                                                )
                                            }
                                        }
                                    }

                                    if (selectedFiles.size < 5) {
                                        Button(
                                            onClick = { launcher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Green4,
                                                contentColor = Green1
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add Image"
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Add Image")
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Max 5 images, 5MB each",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (isUploading) {
                                        LinearProgressIndicator(
                                            progress = uploadProgress,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (currentOrder.status == "To Receive" &&
                                    order.status == "To Deliver" &&
                                    currentOrder.collectionMethod == "DELIVERY" &&
                                    selectedFiles.isEmpty()
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Please attach proof of delivery",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@TextButton
                                }

                                val previousStatus = order.status
                                if (currentOrder.status == "Completed" && previousStatus != "Completed") {
                                    recordOrderTransaction(currentOrder, facilityName, transactionViewModel)
                                }

                                if (selectedFiles.isNotEmpty()) {
                                    isUploading = true
                                    val fileList = selectedFiles.map { uri ->
                                        uri to AttachFileService.getFileName(uri)
                                    }

                                    scope.launch {
                                        AttachFileService.uploadMultipleAttachments(
                                            requestId = currentOrder.orderId,
                                            files = fileList,
                                            onProgress = { progress ->
                                                uploadProgress = progress
                                            }
                                        ) { success ->
                                            isUploading = false
                                            if (success) {
                                                // Create list of attachment filenames
                                                val newAttachments = fileList.map { it.second }

                                                // Update the order with new attachments
                                                val updatedOrder = currentOrder.copy(
                                                    attachments = currentOrder.attachments + newAttachments  // Append new attachments to existing ones
                                                )
                                                Log.d("OnlineSalesDetails", "Updating order with attachments: ${updatedOrder.attachments.joinToString()}")
                                                orderViewModel.updateOrder(updatedOrder)
                                                showDialog = false
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to upload images",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    orderViewModel.updateOrder(currentOrder)
                                    showDialog = false
                                }
                            },
                            enabled = !isUploading
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    color = Green1,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(text = "Save")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                currentOrder = order
                                selectedFiles = emptyList()
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = mintsansFontFamily
                )

                Text(
                    text = currentOrder.orderDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = mintsansFontFamily
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
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
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
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
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }
        }

        //Order Status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White1)
                .clip(RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp))
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Box {
                    Button(
                        onClick = {
                            if (!isStatusFinal) {
                                showDialog = true
                            }
                        },
                        enabled = !isStatusFinal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green1,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = "Update Status",
                            color = if (isStatusFinal) Color.LightGray else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }

            OrderStatus(
                status = currentOrder.status,
                statusDescription = currentOrder.statusDescription,
                dateTime = currentOrder.orderDate,
                statusHistory = currentOrder.statusHistory,
                orderId = currentOrder.orderId,
                paymentMethod = currentOrder.paymentMethod,
                gcashPaymentId = currentOrder.gcashPaymentId,
                collectionMethod = currentOrder.collectionMethod,
                attachments = currentOrder.attachments,
                onRefundAction = { isApproved, newStatus ->
                    val updatedOrder = order.copy(
                        status = newStatus,
                        statusDescription = if (isApproved)
                            "Refund request has been approved"
                        else
                            "Refund request has been rejected"
                    )
                    orderViewModel.updateOrder(updatedOrder)
                }
            )
        }
    }
}

@Composable
fun OrderStatus(
    status: String,
    statusDescription: String,
    dateTime: String,
    statusHistory: List<StatusUpdate> = emptyList(),
    orderId: String,
    paymentMethod: String,
    gcashPaymentId: String,
    collectionMethod: String = "",
    attachments: List<String> = emptyList(),
    onRefundAction: (Boolean, String) -> Unit = { _, _ -> }
) {
    val allStatuses = listOf(
        "Pending",
        "Confirmed",
        "To Deliver",
        "To Receive",
        "Completed",
        "Refund Requested",
        "Refund Approved",
        "Refund Rejected"
    )

    val actualPath = buildList {
        add("Pending")
        statusHistory.forEach { update ->
            if (!contains(update.status) && allStatuses.contains(update.status)) {
                add(update.status)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        actualPath.forEachIndexed { index, currentStatus ->
            val isCurrent = currentStatus == status
            val isPast = actualPath.indexOf(currentStatus) < actualPath.indexOf(status)

            val statusUpdate = statusHistory.findLast { it.status.equals(currentStatus, ignoreCase = true) }
                ?: if (isCurrent) {
                    StatusUpdate(
                        status = status,
                        statusDescription = statusDescription,
                        dateTime = dateTime,
                        updatedBy = ""
                    )
                } else null

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
                showLine = index < actualPath.lastIndex,
                orderId = orderId,
                paymentMethod = paymentMethod,
                gcashPaymentId = gcashPaymentId,
                collectionMethod = collectionMethod,
                attachments = attachments,
                onRefundAction = onRefundAction
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun TimelineStep(
    status: String,
    statusDescription: String,
    dateTime: String,
    showInfo: Boolean,
    isCurrent: Boolean,
    isCompleted: Boolean,
    showLine: Boolean,
    orderId: String,
    paymentMethod: String,
    gcashPaymentId: String,
    collectionMethod: String = "",
    attachments: List<String> = emptyList(),
    onRefundAction: (Boolean, String) -> Unit = { _, _ -> }
) {
    var selectedImageUrl by remember { mutableStateOf<Uri?>(null) }
    var showRefundActionDialog by remember { mutableStateOf(false) }
    var refundActionReason by remember { mutableStateOf("") }
    var isApproving by remember { mutableStateOf(false) }

    // Image preview dialog
    if (selectedImageUrl != null) {
        Dialog(
            onDismissRequest = { selectedImageUrl = null },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = selectedImageUrl,
                        builder = {
                            crossfade(true)
                            scale(Scale.FIT)
                        }
                    ),
                    contentDescription = "Full size image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { selectedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Refund Action Dialog
    if (showRefundActionDialog) {
        AlertDialog(
            onDismissRequest = { showRefundActionDialog = false },
            title = {
                Text(
                    text = if (isApproving) "Approve Refund" else "Reject Refund",
                    fontFamily = mintsansFontFamily
                )
            },
            text = {
                Text(
                    text = if (isApproving)
                        "Are you sure you want to approve this refund request?"
                    else
                        "Are you sure you want to reject this refund request?",
                    fontFamily = mintsansFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newStatus = if (isApproving) "Refund Approved" else "Refund Rejected"
                        onRefundAction(isApproving, newStatus)
                        showRefundActionDialog = false
                    }
                ) {
                    Text(
                        text = "Confirm",
                        fontFamily = mintsansFontFamily,
                        color = Green1
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRefundActionDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = mintsansFontFamily,
                        color = Green1
                    )
                }
            },
            containerColor = White1
        )
    }

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
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when {
                            isCurrent || isCompleted -> Green1
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

            if (showLine) {
                Spacer(modifier = Modifier.height(5.dp))
                val lineHeight = if (showInfo) {
                    when {
                        status == "Pending" && paymentMethod == "GCASH" -> 160.dp
                        status == "To Receive" -> 160.dp
                        status == "Refund Requested" -> 200.dp
                        status == "Completed" && attachments.isNotEmpty() -> 160.dp
                        statusDescription.isNotBlank() && dateTime.isNotBlank() -> 60.dp
                        else -> 32.dp
                    }
                } else {
                    24.dp
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(lineHeight)
                        .background(
                            color = if (isCompleted) Green1
                            else Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Status text
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isCurrent -> Green1
                    isCompleted -> Green1
                    else -> Color.Gray
                },
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily
            )

            if (showInfo) {
                if (statusDescription.isNotBlank()) {
                    Text(
                        text = statusDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontFamily = mintsansFontFamily
                    )
                }

                if (dateTime.isNotBlank()) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontFamily = mintsansFontFamily
                    )
                }

                when (status) {
                    "Pending" -> {
                        if (paymentMethod == "GCASH") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "GCash Payment Receipt:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontFamily = mintsansFontFamily
                            )
                            DisplayAttachments(
                                requestId = gcashPaymentId,
                                modifier = Modifier.padding(vertical = 8.dp),
                                showTitle = false
                            )
                        }
                    }

                    "To Receive" -> {
                        if (collectionMethod == "DELIVERY" || collectionMethod == "PICKUP") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (collectionMethod == "DELIVERY") "Proof of Delivery:" else "Proof of Pickup:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontFamily = mintsansFontFamily
                            )
                            DisplayAttachments(
                                requestId = orderId,
                                modifier = Modifier.padding(vertical = 8.dp),
                                showTitle = false,
                                attachmentType = if (collectionMethod == "PICKUP") "pickup" else "general"
                            )
                        }
                    }

                    "Refund Requested" -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (attachments.isNotEmpty()) {
                            Text(
                                text = "Attached Images:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontFamily = mintsansFontFamily
                            )
                            DisplayAttachments(
                                requestId = orderId,
                                modifier = Modifier.padding(vertical = 8.dp),
                                showTitle = false,
                                attachmentType = "refund"
                            )
                        }

                        if (isCurrent) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isApproving = true
                                        showRefundActionDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green1
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Approve Refund",
                                        fontFamily = mintsansFontFamily
                                    )
                                }
                                Button(
                                    onClick = {
                                        isApproving = false
                                        showRefundActionDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Cinnabar
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Reject Refund",
                                        fontFamily = mintsansFontFamily
                                    )
                                }
                            }
                        }
                    }

                    "Refund Approved", "Refund Rejected" -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (status == "Refund Approved")
                                "Refund has been approved"
                            else
                                "Refund has been rejected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (status == "Refund Approved") Green1 else Cinnabar,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateStatusCard(
    status: String,
    statusDescription: String,
    dateTime: String,
    collectionMethod: String,
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
        "Rejected" to "Your order has been rejected.",
        "To Deliver" to "Your order is to be handed to courier.",
        "To Receive" to "Your order is ready to be picked up/ has been shipped by courier.",
        "Cancelled" to "Your order has been cancelled.",
        "Completed" to "Your order has been completed."
    )

    fun getAvailableStatuses(currentStatus: String): List<String> {
        return when (currentStatus) {
            "Pending" -> listOf("Confirmed", "Rejected")
            "Confirmed" -> {
                if (collectionMethod.equals("PICKUP", ignoreCase = true)) {
                    listOf("To Receive", "Cancelled")
                } else {
                    listOf("To Deliver", "Cancelled")
                }
            }
            "To Deliver" -> listOf("To Receive")
            "To Receive", "Completed", "Cancelled", "Rejected" -> emptyList()
            else -> emptyList()
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
                    enabled = true,  // Enable the status field
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.clickable { expanded = true }  // Enable clicking
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
                            },
                            enabled = true
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
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentDateTime,
                onValueChange = { },
                readOnly = true,
                enabled = false,
                label = { Text("Date and Time") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

//INSTORE
@Composable
fun InStoreSalesDetails(
    sale: SalesData,
    navController: NavController,
    viewModel: SalesViewModel,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
    facilityName: String,
    userRole: String
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditForm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showEditForm) {
        SalesAddForm(
            viewModel = viewModel,
            productViewModel = productViewModel,
            transactionViewModel = transactionViewModel,
            facilityName = facilityName,
            userRole = userRole,
            onSuccess = {
                showEditForm = false
                navController.popBackStack()
            },
            onCancel = { showEditForm = false },
            salesNumber = sale.salesNumber
        )
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Sale Record",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = mintsansFontFamily
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this sale record? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isLoading = true
                        viewModel.deleteSale(
                            salesId = sale.salesNumber,
                            onSuccess = {
                                isLoading = false
                                showDeleteDialog = false
                                navController.popBackStack()
                            },
                            onError = { error ->
                                isLoading = false
                                showDeleteDialog = false
                                errorMessage = error
                            }
                        )
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Green1,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Delete",
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = mintsansFontFamily
                    )
                }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = mintsansFontFamily
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text(
                        text = "OK",
                        fontFamily = mintsansFontFamily
                    )
                }
            }
        )
    }

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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Green1
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit",
                                    fontFamily = mintsansFontFamily
                                )
                            },
                            onClick = {
                                showMenu = false
                                showEditForm = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit sale",
                                    tint = Green1
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete",
                                    color = Green1,
                                    fontFamily = mintsansFontFamily
                                )
                            },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete sale",
                                    tint = Green1
                                )
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sale.salesNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = mintsansFontFamily
                )

                Text(
                    text = sale.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Product Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                InStorePriceInfoColumn(
                    title = "Selling Price",
                    price = sale.price,
                    weightUnit = sale.weightUnit
                )
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily
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
                        color = Green1,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily
                    )
                    Text(
                        text = sale.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = mintsansFontFamily
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
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                        Text(
                            text = "${sale.quantity} x PHP${String.format("%.2f", sale.price)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    }
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily
                        )
                        Text(
                            text = "PHP${String.format("%.2f", sale.quantity * sale.price)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = "PHP${String.format("%.2f", price)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = "per $weightUnit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = mintsansFontFamily
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
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Divider(color = Green4, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Display notes here",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
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
    facilityName: String,
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
            productName = product.name,
            productId = product.productId,
            facilityName = facilityName
        )
        val encodedClient = encodeEmail(order.client)
        transactionViewModel.recordTransaction(encodedClient, transaction)
    }
}