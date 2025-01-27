package com.coco.celestia.screens.client

import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.service.AttachFileService
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.FacilityData
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.StatusUpdate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

                SupportCenter(
                    orderData = currentOrder,
                    orderViewModel = viewModel
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Order status tracking
                TrackOrderSection(
                    orderData = currentOrder,
                    orderViewModel = viewModel
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

@Composable
fun SupportCenter(
    orderData: OrderData,
    orderViewModel: OrderViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showCancelOrderDialog by remember { mutableStateOf(false) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    // File upload states
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var cancelReason by remember { mutableStateOf("") }
    var refundReason by remember { mutableStateOf("") }

    fun handleOrderCancellation(reason: String) {
        val updatedOrder = orderData.copy(
            status = "Cancelled",
            statusDescription = reason
        )
        orderViewModel.updateOrder(updatedOrder)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.cardElevation(25.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Support Center",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(1.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    if (orderData.status == "Pending" || orderData.status == "Confirmed") {
                        SupportCenterItemsCard(
                            title = "Cancel Order",
                            onClick = { showCancelOrderDialog = true }
                        )
                    }

                    if (orderData.status == "Completed") {
                        SupportCenterItemsCard(
                            title = "Request for Return/Refund",
                            onClick = { showRefundDialog = true }
                        )
                    }

                    SupportCenterItemsCard(
                        title = "Contact BCFAC",
                        onClick = { showContactDialog = true },
                        showDivider = false
                    )
                }
            }
        }
    }

    // Cancel Order Dialog
    if (showCancelOrderDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancelOrderDialog = false
                cancelReason = ""
            },
            title = { Text(text = "Cancel Order") },
            text = {
                Column {
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(fontFamily = mintsansFontFamily)
                    Text(text = "Are you sure you want to cancel your current order?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation") },
                        placeholder = { Text("Enter your reason here") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (cancelReason.isNotBlank()) {
                            handleOrderCancellation(cancelReason)
                            showCancelOrderDialog = false
                            cancelReason = ""
                        }
                    },
                    enabled = cancelReason.isNotBlank()
                ) {
                    Text(
                        text = "Submit",
                        fontFamily = mintsansFontFamily,
                        color = if (cancelReason.isNotBlank()) Green1 else Green1.copy(alpha = 0.5f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCancelOrderDialog = false
                    cancelReason = ""
                }) {
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

    // Refund/Return Dialog
    if (showRefundDialog) {
        AlertDialog(
            onDismissRequest = {
                showRefundDialog = false
                refundReason = ""
                selectedFiles = emptyList()
                isUploading = false
            },
            title = { Text(text = "Request Refund/Return") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Would you like to request a refund or return for your order?",
                        fontFamily = mintsansFontFamily,
                        color = Green1
                    )

                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text("Reason for refund/return") },
                        placeholder = { Text("Enter your reason here") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Upload Proof for Refund (Images only)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (refundReason.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please provide a reason for the refund",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }

                        isUploading = true
                        scope.launch {
                            handleRefundRequest(
                                orderData = orderData,
                                reason = refundReason,
                                selectedFiles = selectedFiles,
                                orderViewModel = orderViewModel,
                                onProgress = { progress ->
                                    uploadProgress = progress
                                },
                                onSuccess = {
                                    isUploading = false
                                    showRefundDialog = false
                                    selectedFiles = emptyList()
                                    refundReason = ""
                                },
                                onError = { error ->
                                    isUploading = false
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    enabled = !isUploading && refundReason.isNotBlank()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Green1,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Submit Request",
                            fontFamily = mintsansFontFamily,
                            color = if (refundReason.isNotBlank()) Green1 else Green1.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRefundDialog = false
                        refundReason = ""
                        selectedFiles = emptyList()
                        isUploading = false
                    },
                    enabled = !isUploading
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

    // Dialog for See BCFAC Contact Details
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text(text = "BCFAC Contact Details")
                LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(fontFamily = mintsansFontFamily)},
            text = {
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(fontFamily = mintsansFontFamily)
                Column {
                    Text(text = "Phone: +63 912 345 6789")
                    Text(text = "Email: support@bcfac.com")
                    Text(text = "Address: BCFAC Office, Baguio City")
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text(text = "Close",
                        fontFamily = mintsansFontFamily,
                        color = Green1)
                }
            },
            containerColor = White1
        )
    }
}

@Composable
fun SupportCenterItemsCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                color = Green1
            )
        }
        if (showDivider) {
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp
            )
        }
    }
}

fun handleRefundRequest(
    orderData: OrderData,
    reason: String,
    selectedFiles: List<Uri>,
    orderViewModel: OrderViewModel,
    onProgress: (Float) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        if (selectedFiles.isNotEmpty()) {
            val fileList = selectedFiles.map { uri ->
                uri to AttachFileService.getFileName(uri)
            }

            val success = AttachFileService.uploadMultipleAttachments(
                requestId = "${orderData.orderId}_refund",
                files = fileList,
                onProgress = onProgress
            ) { success ->
                if (success) {
                    val updatedOrder = orderData.copy(
                        status = "Refund Requested",
                        statusDescription = reason,
                        attachments = fileList.map { it.second },
                        statusHistory = orderData.statusHistory + StatusUpdate(
                            status = "Refund Requested",
                            statusDescription = reason,
                            dateTime = getCurrentDateTime()
                        )
                    )
                    orderViewModel.updateOrder(updatedOrder)
                    onSuccess()
                } else {
                    onError("Failed to upload attachments")
                }
            }
        } else {
            val updatedOrder = orderData.copy(
                status = "Refund Requested",
                statusDescription = reason,
                statusHistory = orderData.statusHistory + StatusUpdate(
                    status = "Refund Requested",
                    statusDescription = reason,
                    dateTime = getCurrentDateTime()
                )
            )
            orderViewModel.updateOrder(updatedOrder)
            onSuccess()
        }
    } catch (e: Exception) {
        onError(e.message ?: "An error occurred while processing your refund request")
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun OrderDetailsSection(
    orderData: OrderData,
    facilityData: FacilityData
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Separate card for OrderHeader
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            OrderHeader(orderData)
        }

        // Card for order details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Items (${orderData.orderData.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily,
                        modifier = Modifier.padding(top = 16.dp),
                        color = Green1
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
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

                val totalAmount = orderData.orderData.sumOf { it.price * it.quantity }
                Text(
                    text = "Total: PHP ${String.format("%.2f", totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    fontFamily = mintsansFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Green1
                )
            }
        }
    }
}

@Composable
private fun OrderHeader(orderData: OrderData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val containerColor = when (orderData.status.lowercase()) {
            "cancelled" -> Cinnabar
            "completed" -> Green2
            else -> Green1
        }

        Text(
            text = orderData.status,
            style = MaterialTheme.typography.titleMedium,
            color = White1,
            fontFamily = mintsansFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = orderData.orderId,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = mintsansFontFamily,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Text(
            text = orderData.orderDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = mintsansFontFamily,
            modifier = Modifier.padding(top = 4.dp)
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
                .padding(16.dp),
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily,
                            color = Green1
                        )
                        Text(
                            text = "${product.quantity} ${product.weightUnit.lowercase()} x PHP ${String.format("%.2f", product.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily,
                            color = Green1
                        )
                        Text(
                            text = "PHP ${String.format("%.2f", product.price * product.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = mintsansFontFamily,
                            color = Green1
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Collection Method",
                color = Green1,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = when (orderData.collectionMethod) {
                        Constants.COLLECTION_PICKUP -> painterResource(id = R.drawable.pickup)
                        Constants.COLLECTION_DELIVERY -> painterResource(id = R.drawable.delivery)
                        else -> painterResource(id = R.drawable.warning)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Green1)
                )

                Text(
                    text = orderData.collectionMethod,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    fontFamily = mintsansFontFamily
                )
            }

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
fun ClientDetailsPaymentMethod(
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Payment Method",
                color = Green1,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = when (orderData.paymentMethod) {
                        Constants.PAYMENT_CASH -> painterResource(id = R.drawable.cash)
                        Constants.PAYMENT_GCASH -> painterResource(id = R.drawable.gcash)
                        else -> painterResource(id = R.drawable.warning)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Green1)
                )

                Text(
                    text = orderData.paymentMethod,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    fontFamily = mintsansFontFamily
                )
            }

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
fun TrackOrderSection(
    orderData: OrderData,
    orderViewModel: OrderViewModel
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp))
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
                color = Green1,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily
            )

            if (orderData.status == "To Receive") {
                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green1
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Received Order",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ClientOrderStatus(
            status = orderData.status,
            statusDescription = orderData.statusDescription,
            dateTime = orderData.orderDate,
            statusHistory = orderData.statusHistory,
            collectionMethod = orderData.collectionMethod
        )

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmDialog = false
                    selectedFiles = emptyList()
                },
                title = {
                    Text(
                        text = "Confirm Order Received"
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (orderData.paymentMethod == "CASH")
                                "Please confirm that you have paid this order."
                            else
                                "Please confirm that you have received your order."
                        )

                        if (orderData.collectionMethod == "PICKUP") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Upload Proof of Pickup",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
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
                            if (orderData.collectionMethod == "PICKUP" && selectedFiles.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Please attach proof of receipt",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TextButton
                            }

                            val completionMessage = if (orderData.paymentMethod == "CASH")
                                "Order received and paid in cash"
                            else
                                "Order received"

                            if (selectedFiles.isNotEmpty()) {
                                isUploading = true
                                val fileList = selectedFiles.map { uri ->
                                    uri to AttachFileService.getFileName(uri)
                                }

                                scope.launch {
                                    AttachFileService.uploadMultipleAttachments(
                                        requestId = "${orderData.orderId}_pickup",
                                        files = fileList,
                                        onProgress = { progress ->
                                            uploadProgress = progress
                                        }
                                    ) { success ->
                                        isUploading = false
                                        if (success) {
                                            val updatedOrder = orderData.copy(
                                                status = "Completed",
                                                statusDescription = completionMessage,
                                                attachments = fileList.map { it.second },
                                                statusHistory = orderData.statusHistory + StatusUpdate(
                                                    status = "Completed",
                                                    statusDescription = completionMessage,
                                                    dateTime = getCurrentDateTime()
                                                )
                                            )
                                            orderViewModel.updateOrder(updatedOrder)
                                            showConfirmDialog = false
                                            selectedFiles = emptyList()
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
                                val updatedOrder = orderData.copy(
                                    status = "Completed",
                                    statusDescription = completionMessage,
                                    statusHistory = orderData.statusHistory + StatusUpdate(
                                        status = "Completed",
                                        statusDescription = completionMessage,
                                        dateTime = getCurrentDateTime()
                                    )
                                )
                                orderViewModel.updateOrder(updatedOrder)
                                showConfirmDialog = false
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
                            Text(
                                text = if (orderData.paymentMethod == "CASH")
                                    "Confirm Payment"
                                else
                                    "Confirm Receipt"
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            selectedFiles = emptyList()
                        },
                        enabled = !isUploading
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun ClientOrderStatus(
    status: String,
    statusDescription: String,
    dateTime: String,
    statusHistory: List<StatusUpdate> = emptyList(),
    collectionMethod: String // Add collection method parameter
) {
    val allStatuses = if (collectionMethod == "PICKUP") {
        listOf(
            "Pending",
            "Confirmed",
            "To Receive",
            "Completed"
        )
    } else {
        listOf(
            "Pending",
            "Confirmed",
            "To Deliver",
            "To Receive",
            "Completed"
        )
    }

    val currentIndex = allStatuses.indexOf(status)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        allStatuses.forEachIndexed { index, currentStatus ->
            val isCurrent = index == currentIndex
            val isPast = index < currentIndex
            val showInfo = isCurrent || isPast

            if (isCurrent || isPast) {
                val statusUpdate = if (isCurrent) {
                    StatusUpdate(status, statusDescription, dateTime)
                } else {
                    statusHistory.find { it.status == currentStatus }
                }

                ClientTimelineStep(
                    status = currentStatus,
                    statusDescription = statusUpdate?.statusDescription ?: "",
                    dateTime = statusUpdate?.dateTime ?: "",
                    showInfo = showInfo,
                    isCurrent = isCurrent,
                    isCompleted = isPast,
                    showLine = index < allStatuses.lastIndex
                )
            }
        }
    }
}

@Composable
private fun ClientTimelineStep(
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
                            isCurrent || isCompleted -> Green2
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
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
            Spacer(modifier = Modifier.height(5.dp))

            if (showLine) {
                Spacer(modifier = Modifier.height(2.dp))
                val lineHeight = if (showInfo) {
                    val additionalHeight = if (statusDescription.isNotEmpty() && dateTime.isNotEmpty()) {
                        70.dp // Assume more content adds additional height
                    } else {
                        35.dp // Default height for minimal content
                    }
                    additionalHeight
                } else {
                    20.dp // Minimal height for no content
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(lineHeight)
                        .background(
                            color = if (isCompleted) Green1
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
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
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                color = Green1
            )

            if (showInfo && statusDescription.isNotEmpty()) {
                Text(
                    text = statusDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showInfo && dateTime.isNotEmpty()) {
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}
