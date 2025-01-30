package com.coco.celestia.screens.client

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.coop.admin.EmptyOrders
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.AttachFileService
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.FacilityData
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.StatusUpdate
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun OrderSummary(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    facilityViewModel: FacilityViewModel,
    items: List<BasketItem>,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = LocalDateTime.now().format(formatter).toString()
    val idFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val currentDateTime = LocalDateTime.now().format(idFormatter)
    val userData by userViewModel.userData.observeAsState(UserData())
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())

    val groupedItems = items.groupBy { it.productType }

    val facilityMethods = remember(groupedItems) {
        mutableStateMapOf<String, Pair<String, String>>()
    }

    // Track order data including attachments for each facility
    val facilityOrderData = remember(groupedItems) {
        mutableStateMapOf<String, OrderData>()
    }

    val paymentRequestId = remember {
        "GCASH-PAYMENT-${UUID.randomUUID()}"
    }

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
        facilityViewModel.fetchFacilities()
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
                item {
                    UserDetailsHeader(userData = userData)
                }

                groupedItems.forEach { (facilityName, facilityItems) ->
                    val facilityData = facilitiesData.find {
                        it.name.lowercase() == facilityName.lowercase()
                    }

                    item {
                        FacilityCard(
                            items = facilityItems,
                            facilityName = facilityName
                        )
                    }

                    val tempOrderData = OrderData(
                        orderData = facilityItems.map { item ->
                            ProductData(
                                productId = item.productId,
                                name = item.product,
                                quantity = item.quantity,
                                price = item.price,
                                timestamp = item.timestamp,
                                type = item.productType
                            )
                        },
                        collectionMethod = facilityMethods[facilityName]?.first ?: "",
                        paymentMethod = facilityMethods[facilityName]?.second ?: "",
                        gcashPaymentId = paymentRequestId,
                        attachments = facilityOrderData[facilityName]?.attachments ?: emptyList()
                    )

                    // Update facility order data
                    facilityOrderData[facilityName] = tempOrderData

                    item {
                        ClientCollectionMethod(
                            orderData = tempOrderData,
                            facilityData = facilityData,
                            onUpdate = { method ->
                                facilityMethods[facilityName] = Pair(
                                    method,
                                    facilityMethods[facilityName]?.second ?: ""
                                )
                            }
                        )
                    }

                    item {
                        ClientPaymentMethod(
                            orderData = tempOrderData,
                            orderViewModel = orderViewModel,
                            facilityData = facilityData,
                            onUpdate = { method, attachments ->
                                facilityMethods[facilityName] = Pair(
                                    facilityMethods[facilityName]?.first ?: "",
                                    method
                                )
                                // Update the facilityOrderData with new attachments
                                facilityOrderData[facilityName] = tempOrderData.copy(
                                    attachments = attachments ?: emptyList()
                                )
                            }
                        )
                    }
                }

                item {
                    OrderSummaryActions(
                        enabled = validateMethods(groupedItems.keys, facilityMethods),
                        totalPrice = items.sumOf { it.price },
                        facilityOrderData = facilityOrderData,
                        onPlaceOrder = {
                            val hasInvalidGcashPayment = facilityOrderData.any { (_, orderData) ->
                                orderData.paymentMethod == Constants.PAYMENT_GCASH &&
                                        (orderData.attachments.isNullOrEmpty() || orderData.gcashPaymentId.isNullOrEmpty())
                            }

                            if (hasInvalidGcashPayment) {
                                onEvent(
                                    Triple(
                                        ToastStatus.WARNING,
                                        "Please complete GCash payment upload for all facilities",
                                        System.currentTimeMillis()
                                    )
                                )
                                return@OrderSummaryActions
                            }

                            groupedItems.forEach { (facilityName, facilityItems) ->
                                val methods = facilityMethods[facilityName] ?: Pair("", "")
                                val formattedOrderId = "OID-${facilityName.uppercase()}-$currentDateTime"

                                val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
                                val formattedDisplayDate = LocalDateTime.now().format(formatter)

                                val initialStatus = StatusUpdate(
                                    status = "Pending",
                                    statusDescription = "Your order is being reviewed",
                                    dateTime = formattedDisplayDate
                                )

                                val currentOrderData = facilityOrderData[facilityName]
                                orderViewModel.placeOrder(
                                    uid = uid,
                                    order = OrderData(
                                        orderId = formattedOrderId,
                                        orderDate = formattedDateTime,
                                        status = "Pending",
                                        statusDescription = "Your order is being reviewed",
                                        statusHistory = listOf(initialStatus),
                                        orderData = facilityItems.map {
                                            ProductData(
                                                productId = it.productId,
                                                name = it.product,
                                                quantity = it.quantity,
                                                price = it.price,
                                                timestamp = it.timestamp,
                                                type = it.productType,
                                                committedStock = 0.0
                                            )
                                        },
                                        client = "${userData.firstname} ${userData.lastname}",
                                        collectionMethod = methods.first,
                                        paymentMethod = methods.second,
                                        gcashPaymentId = currentOrderData?.gcashPaymentId ?: paymentRequestId,
                                        attachments = currentOrderData?.attachments ?: emptyList()
                                    )
                                )
                            }
                            navController.navigate(Screen.ClientOrder.route)
                            userViewModel.clearItems(items)
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
    facilityOrderData: Map<String, OrderData>,
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
                    when {
                        !enabled.first -> {
                            onEvent(
                                Triple(
                                    ToastStatus.WARNING,
                                    enabled.second,
                                    System.currentTimeMillis()
                                )
                            )
                        }
                        facilityOrderData.any { (_, orderData) ->
                            orderData.paymentMethod == Constants.PAYMENT_GCASH &&
                                    (orderData.attachments.isNullOrEmpty() || orderData.gcashPaymentId.isNullOrEmpty())
                        } -> {
                            onEvent(
                                Triple(
                                    ToastStatus.WARNING,
                                    "Please upload GCash payment receipt for all facilities using GCash",
                                    System.currentTimeMillis()
                                )
                            )
                        }
                        else -> {
                            onPlaceOrder()
                            onEvent(
                                Triple(
                                    ToastStatus.SUCCESSFUL,
                                    "Order placed successfully!",
                                    System.currentTimeMillis()
                                )
                            )
                        }
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
fun UserDetailsHeader(userData: UserData) {
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
                contentDescription = "User Profile",
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(Green1)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "${userData.firstname} ${userData.lastname}",
                    fontWeight = FontWeight.Bold,
                    color = Green1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${userData.streetNumber}${userData.barangay}",
                    fontSize = 13.sp,
                    color = Green1
                )
            }
        }
    }
}

@Composable
fun FacilityCard(
    items: List<BasketItem>,
    facilityName: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = facilityName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

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
            ) {
                Text(
                    text = "Total: ",
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
fun ClientCollectionMethod(
    orderData: OrderData,
    facilityData: FacilityData?,
    onUpdate: (String) -> Unit
) {
    val enabledMethods = mutableListOf<Pair<String, String>>()
    if (!facilityData?.pickupLocation.isNullOrEmpty()) {
        enabledMethods.add(Constants.COLLECTION_PICKUP to facilityData!!.pickupLocation)
    }
    if (!facilityData?.deliveryDetails.isNullOrEmpty()) {
        enabledMethods.add(Constants.COLLECTION_DELIVERY to facilityData!!.deliveryDetails)
    }

    if (enabledMethods.isEmpty()) return

    LaunchedEffect(enabledMethods) {
        if (enabledMethods.size == 1) {
            onUpdate(enabledMethods[0].first)
        }
    }

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
                    modifier = Modifier.weight(1f)
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
            }

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
                    if (enabledMethods.size == 1) {
                        val (method, description) = enabledMethods[0]
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Green1
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    } else {
                        var selectedMethod by remember { mutableStateOf(orderData.collectionMethod) }
                        enabledMethods.forEach { (method, description) ->
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
                                        color = Color.Gray
                                    )
                                }
                            }
                            if (method != enabledMethods.last().first) {
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
    }
}

@Composable
fun ClientPaymentMethod(
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    facilityData: FacilityData?,
    onUpdate: (String, List<String>?) -> Unit
) {
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var showUploadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasUploadedFiles by remember { mutableStateOf(!orderData.attachments.isNullOrEmpty()) }

    val enabledMethods = mutableListOf<Pair<String, String>>()
    if (!facilityData?.cashInstructions.isNullOrEmpty()) {
        enabledMethods.add(Constants.PAYMENT_CASH to facilityData!!.cashInstructions)
    }
    if (!facilityData?.gcashNumbers.isNullOrEmpty()) {
        enabledMethods.add(Constants.PAYMENT_GCASH to facilityData!!.gcashNumbers)
    }

    if (enabledMethods.isEmpty()) return

    LaunchedEffect(enabledMethods) {
        if (enabledMethods.size == 1) {
            val method = enabledMethods[0].first
            onUpdate(method, if (method == Constants.PAYMENT_GCASH) emptyList() else null)
        }
    }

    LaunchedEffect(orderData.attachments) {
        hasUploadedFiles = !orderData.attachments.isNullOrEmpty()
    }

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
                    modifier = Modifier.weight(1f)
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
            }

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
                    if (enabledMethods.size == 1) {
                        val (method, description) = enabledMethods[0]
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Green1
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (method == Constants.PAYMENT_GCASH) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showUploadDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hasUploadedFiles) Green4 else Green1
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (hasUploadedFiles) "Receipt Uploaded ✓" else "Upload GCash Receipt")
                                }
                            }
                        }
                    } else {
                        var selectedMethod by remember { mutableStateOf(orderData.paymentMethod) }
                        enabledMethods.forEach { (method, description) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedMethod = method
                                            if (method != Constants.PAYMENT_GCASH) {
                                                onUpdate(method, emptyList())
                                                val updatedOrder = orderData.copy(
                                                    attachments = emptyList()
                                                )
                                                orderViewModel.updateOrder(updatedOrder)
                                                hasUploadedFiles = false
                                            } else {
                                                onUpdate(method, emptyList())
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedMethod == method,
                                        onClick = {
                                            selectedMethod = method
                                            if (method != Constants.PAYMENT_GCASH) {
                                                onUpdate(method, emptyList())
                                                val updatedOrder = orderData.copy(
                                                    attachments = emptyList()
                                                )
                                                orderViewModel.updateOrder(updatedOrder)
                                                hasUploadedFiles = false
                                            } else {
                                                onUpdate(method, emptyList())
                                            }
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
                                            color = Color.Gray
                                        )
                                    }
                                }

                                if (method == Constants.PAYMENT_GCASH &&
                                    selectedMethod == method) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showUploadDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (hasUploadedFiles) Green4 else Green1
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (hasUploadedFiles) "Receipt Uploaded ✓" else "Upload GCash Receipt")
                                    }
                                }
                            }
                            if (method != enabledMethods.last().first) {
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
    }

    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload GCash Payment Receipt") },
            text = {
                Column {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let { newUri ->
                            val type = context.contentResolver.getType(newUri)
                            if (type?.startsWith("image/") != true) {
                                Toast.makeText(
                                    context,
                                    "Please select an image file",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@let
                            }

                            val isOversized = context.contentResolver.openInputStream(newUri)?.use {
                                it.available() > 5 * 1024 * 1024 // 5MB
                            } ?: false

                            if (isOversized) {
                                Toast.makeText(
                                    context,
                                    "Image exceeds 5MB limit",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@let
                            }

                            selectedFile = newUri
                        }
                    }

                    if (selectedFile != null) {
                        Spacer(modifier = Modifier.height(8.dp))
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
                                    contentDescription = "Receipt",
                                    tint = Green1,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Payment Receipt",
                                    color = Green1,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        AttachFileService.deleteAllAttachments(
                                            requestId = orderData.gcashPaymentId
                                        ) { success ->
                                            if (success) {
                                                selectedFile = null
                                                onUpdate(Constants.PAYMENT_GCASH, emptyList())
                                                hasUploadedFiles = false
                                                val updatedOrder = orderData.copy(
                                                    attachments = emptyList()
                                                )
                                                orderViewModel.updateOrder(updatedOrder)

                                                Toast.makeText(
                                                    context,
                                                    "Receipt removed successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to remove receipt",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
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

                    if (selectedFile == null) {
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
                                    contentDescription = "Add Receipt"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Receipt")
                            }
                        }
                    }

                    Text(
                        text = "Please upload GCash payment receipt (max 5MB)",
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedFile == null) {
                            Toast.makeText(
                                context,
                                "Please upload payment receipt",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }

                        scope.launch {
                            try {
                                isUploading = true
                                val fileName = AttachFileService.getFileName(selectedFile!!)

                                AttachFileService.uploadMultipleAttachments(
                                    requestId = orderData.gcashPaymentId,
                                    files = listOf(selectedFile!! to fileName),
                                    onProgress = { progress ->
                                        uploadProgress = progress
                                    }
                                ) { success ->
                                    if (success) {
                                        val attachmentNames = listOf(fileName)
                                        val updatedOrder = orderData.copy(
                                            statusDescription = "Payment proof submitted, waiting for confirmation",
                                            statusHistory = orderData.statusHistory + StatusUpdate(
                                                status = "Pending",
                                                statusDescription = "Payment proof submitted, waiting for confirmation",
                                                dateTime = getCurrentDateTime()
                                            ),
                                            attachments = attachmentNames,
                                            gcashPaymentId = orderData.gcashPaymentId
                                        )
                                        orderViewModel.updateOrder(updatedOrder)
                                        onUpdate(Constants.PAYMENT_GCASH, attachmentNames)
                                        hasUploadedFiles = true
                                        isUploading = false
                                        showUploadDialog = false

                                        Toast.makeText(
                                            context,
                                            "Payment proof submitted successfully",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        isUploading = false
                                        Toast.makeText(
                                            context,
                                            "Failed to upload receipt",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                isUploading = false
                                Toast.makeText(
                                    context,
                                    "Error processing files: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    enabled = !isUploading && selectedFile != null
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Green1,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Submit Payment")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            AttachFileService.deleteAllAttachments(
                                requestId = orderData.gcashPaymentId
                            ) { success ->
                                if (success) {
                                    onUpdate(Constants.PAYMENT_GCASH, emptyList())
                                    val updatedOrder = orderData.copy(
                                        attachments = emptyList(),
                                        gcashPaymentId = orderData.gcashPaymentId
                                    )
                                    orderViewModel.updateOrder(updatedOrder)
                                    hasUploadedFiles = false
                                    selectedFile = null
                                    showUploadDialog = false

                                    Toast.makeText(
                                        context,
                                        "Receipt removed successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to remove receipt",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun validateMethods(
    facilityNames: Set<String>,
    facilityMethods: Map<String, Pair<String, String>>
): Pair<Boolean, String> {
    facilityNames.forEach { facility ->
        val methods = facilityMethods[facility]
        if (methods == null || methods.first.isEmpty() || methods.second.isEmpty()) {
            return Pair(
                false,
                if (methods == null || (methods.first.isEmpty() && methods.second.isEmpty())) {
                    "Please select collection and payment methods for $facility"
                } else if (methods.first.isEmpty()) {
                    "Please select collection method for $facility"
                } else {
                    "Please select payment method for $facility"
                }
            )
        }
    }
    return Pair(true, "")
}