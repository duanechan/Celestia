@file:OptIn(ExperimentalMaterial3Api::class)

package com.coco.celestia.screens.client

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.convertMillisToDate
import com.coco.celestia.util.formatDate
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun AddOrderPanel(navController: NavController) {
    BackHandler {
        navController.navigateUp()
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .semantics { testTag = "android:id/AddOrderPanel" }
    ) {
        Text(text = "Add Order", fontSize = 25.sp)
        Spacer(modifier = Modifier.height(150.dp))
        ProductCard("Coffee", navController)
        ProductCard("Meat", navController)
        ProductCard("Vegetable", navController)
    }
}

@Composable
fun ProductCard(
    product: String,
    navController: NavController,
) {
    val gradient = when (product) {
        "Meat" -> Brush.linearGradient(
            colors = listOf(Color(0xFFFF5151), Color(0xFFB06520))
        )
        "Coffee" -> Brush.linearGradient(
            colors = listOf(Color(0xFFB06520), Color(0xFF5D4037))
        )
        "Vegetable" -> Brush.linearGradient(
            colors = listOf(Color(0xFF42654A), Color(0xFF3B8D46))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color.Gray, Color.LightGray)
        )
    }

    // Apply gradient inside the card
    Card(
        modifier = Modifier
            .height(150.dp)
            .clickable {
                navController.navigate(Screen.OrderDetails.createRoute(product))
            }
            .semantics { testTag = "android:id/ProductCard_$product" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.elevatedCardElevation(5.dp) // adjust shadow effect here
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient) // gradient background here
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = product,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(15.dp))
}

@Composable
fun OrderDetailsPanel(
    navController: NavController,
    type: String?,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onOrder: (OrderData) -> Unit
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .semantics { testTag = "android:id/OrderDetailsPanel" }
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        Text(
            text = type ?: "Unknown Product",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .semantics { testTag = "android:id/ProductTypeTitle" }
        )
        type?.let {
            LaunchedEffect(type) {
                productViewModel.fetchProductByType(type)
            }
            when (productState) {
                is ProductState.EMPTY -> Text(
                    text = "No products available.",
                    modifier = Modifier.semantics { testTag = "android:id/ProductStateEmpty" }
                )
                is ProductState.ERROR -> Text(
                    text = "Error: ${(productState as ProductState.ERROR).message}",
                    modifier = Modifier.semantics { testTag = "android:id/ProductStateError" }
                )
                is ProductState.LOADING -> Text(
                    text = "Loading products...",
                    modifier = Modifier.semantics { testTag = "android:id/ProductStateLoading" }
                )
                is ProductState.SUCCESS -> {
                    LazyColumn(modifier = Modifier.semantics { testTag = "android:id/ProductList" }) {
                        items(productData) { product ->
                            ProductTypeCard(
                                product,
                                navController,
                                userViewModel = userViewModel,
                                onAddToCartEvent = { onAddToCartEvent(it) },
                                onOrder = { onOrder(it) }
                            )
                        }
                    }
                }
                null -> Text(
                    text = "Unknown state",
                    modifier = Modifier.semantics { testTag = "android:id/ProductStateUnknown" }
                )
            }
        }
    }
}

@Composable
fun ProductTypeCard(
    product: ProductData,
    navController: NavController,
    userViewModel: UserViewModel,
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onOrder: (OrderData) -> Unit
) {
    val productName = product.name
    val productType = product.type
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val gradientBrush = when (productType.lowercase()) {
        "coffee" -> Brush.linearGradient(
            colors = listOf(Color(0xFFB79276), Color(0xFF91684A))
        )
        "meat" -> Brush.linearGradient(
            colors = listOf(Color(0xFFD45C5C), Color(0xFFAA3333))
        )
        "vegetable" -> Brush.linearGradient(
            colors = listOf(Color(0xFF4CB05C), Color(0xFF4F8A45))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color.Gray, Color.LightGray)
        )
    }

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .animateContentSize()
            .fillMaxWidth()
            .semantics { testTag = "android:id/ProductTypeCard_${product.name}" },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp)
    ) {
        Box(
            modifier = Modifier
                .background(brush = gradientBrush)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = productName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 8.dp)
                            .clickable {
                                //expanded = !expanded
                                isSheetOpen = true
                            }
                            .semantics { testTag = "android:id/ShoppingCartIcon" }
                    )
                }
                if (isSheetOpen) {
                    LaunchedEffect(Unit) {
                        sheetState.expand()
                    }
                    ModalBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = {
                            isSheetOpen = false
                        }
                    ) {
                        AddOrderForm(
                            navController = navController,
                            userViewModel = userViewModel,
                            productType = productType,
                            productName = productName,
                            onAddToCartEvent = { onAddToCartEvent(it) },
                            onOrder = { onOrder(it) }
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(15.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderForm(
    navController: NavController,
    userViewModel: UserViewModel,
    productType: String?,
    productName: String?,
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onOrder: (OrderData) -> Unit
) {
    val userData by userViewModel.userData.observeAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var targetDateDialog by remember { mutableStateOf(true) }
    val datePickerState = rememberDatePickerState()
    var selectedDate by remember { mutableStateOf("") } // Track the selected date
    var quantity by remember { mutableIntStateOf(0) }
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()

    val order = OrderData(
        orderId = "Order-${UUID.randomUUID()}",
        orderDate = formattedDateTime,
        targetDate = selectedDate,
        status = "PENDING",
        orderData = ProductData(productName.toString(), quantity, productType.toString()),
        client = "${userData?.firstname} ${userData?.lastname}",
        barangay = userData?.barangay.toString(),
        street = userData?.streetNumber.toString()
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .semantics { testTag = "android:id/DatePickerDialogContent" }
    ) {
        Text(text = "Do you want to place this order?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "ID: ${order.orderId.substring(6, 10).uppercase()}", modifier = Modifier.semantics { testTag = "android:id/OrderIdText" })
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Product: ${productName.toString()}", modifier = Modifier.semantics { testTag = "android:id/ProductNameText" })
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Order Date: ${order.orderDate}", modifier = Modifier.semantics { testTag = "android:id/OrderDateText" })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = if (quantity == 0) "" else quantity.toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null) {
                    quantity = intValue
                } else if (newValue.isEmpty()) {
                    quantity = 0
                }
            },
            label = { Text("Enter weight (kg)") },
            placeholder = { Text("e.g. 10.5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/QuantityInput" }
        )

        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Target Date") },
            placeholder = { Text(selectedDate) },
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = { showDatePicker = !showDatePicker },
                    modifier = Modifier.semantics { testTag = "android:id/DateIconButton" }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = ""
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/TargetDateInput" }
        )
        AnimatedVisibility(visible = showDatePicker) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .semantics { testTag = "android:id/DatePickerBox" }
            ) {
                BoxWithConstraints {
                    val scale = remember(this.maxWidth) { if (this.maxWidth > 360.dp) 1f else (this.maxWidth / 360.dp) }
                    Box(modifier = Modifier.requiredWidthIn(min = 360.dp)) {
                        DatePicker(
                            modifier = Modifier.scale(scale).fillMaxSize(),
                            state = datePickerState,
                            showModeToggle = false,
                            dateValidator = { it >= System.currentTimeMillis() }
                        )
                    }
                }
            }
        }
        // Inside your LaunchedEffect or wherever you are using selectedDateMillis
        LaunchedEffect(datePickerState.selectedDateMillis) {
            datePickerState.selectedDateMillis?.let { millis ->
                selectedDate = convertMillisToDate(millis)
                showDatePicker = false
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = {
            targetDateDialog = false
        }) {
            Text("Cancel")
        }
        TextButton(
            onClick = {
                onAddToCartEvent(Triple(ToastStatus.SUCCESSFUL, "Added order.", System.currentTimeMillis()))
                onOrder(order)
                navController.navigate(Screen.OrderConfirmation.route)
                targetDateDialog = false
            },
            enabled = quantity != 0 && selectedDate.isNotEmpty(),
            modifier = Modifier.semantics { testTag = "ConfirmOrderButton" }
        ) {
            Text("Confirm Order")
        }
    }
}

@Composable
fun ConfirmOrderRequestPanel(
    navController: NavController,
    order: OrderData,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel,
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val userData by userViewModel.userData.observeAsState()
    val orderState by orderViewModel.orderState.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val barangay = userData?.barangay ?: ""
    val streetNumber = userData?.streetNumber ?: ""

    LaunchedEffect(barangay, streetNumber) {
        if (barangay.isEmpty() && streetNumber.isEmpty()) {
            onAddToCartEvent(Triple(ToastStatus.WARNING, "Please complete your address details.", System.currentTimeMillis()))
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.OrderConfirmation.route) { inclusive = true }
            }
        } else {
            val transaction = TransactionData(
                transactionId = "Transaction-${UUID.randomUUID()}",
                type = "OrderPlaced",
                date = order.orderDate,
                description = "Ordered ${order.orderData.quantity}kg of ${order.orderData.name} due in ${order.targetDate}"
            )
            orderViewModel.placeOrder(uid, order)
            transactionViewModel.recordTransaction(uid, transaction)
        }
    }
    when (orderState) {
        is OrderState.LOADING -> {
            onAddToCartEvent(
                Triple(
                    ToastStatus.INFO,
                    "Loading...",
                    System.currentTimeMillis()
                )
            )
            Text(
                text = "Loading...",
                modifier = Modifier.semantics { testTag = "android:id/OrderStateLoading" }
            )
        }
        is OrderState.ERROR -> {
            onAddToCartEvent(
                Triple(
                    ToastStatus.FAILED,
                    "Error: ${(orderState as OrderState.ERROR).message}",
                    System.currentTimeMillis()
                )
            )
            Text(
                text = "Error: ${(orderState as OrderState.ERROR).message}",
                modifier = Modifier.semantics { testTag = "android:id/OrderStateError" }
            )
        }
        is OrderState.SUCCESS -> {
            onAddToCartEvent(
                Triple(
                    ToastStatus.SUCCESSFUL,
                    "Order placed.",
                    System.currentTimeMillis()
                )
            )
            userData?.let {
                navController.navigate(Screen.Client.route) {
                    popUpTo(Screen.Splash.route)
                }
            }
            Text(
                text = "Order placed successfully.",
                modifier = Modifier.semantics { testTag = "android:id/OrderStateSuccess" }
            )
        }
        else -> {
            Text(
                text = "Order state is unknown.",
                modifier = Modifier.semantics { testTag = "android:id/OrderStateUnknown" }
            )
        }
    }
}
