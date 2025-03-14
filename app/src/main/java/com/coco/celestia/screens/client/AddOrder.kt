@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class, ExperimentalFoundationApi::class, ExperimentalCoilApi::class,
    ExperimentalFoundationApi::class
)

package com.coco.celestia.screens.client

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.ClientBG
import com.coco.celestia.ui.theme.LGContainer
import com.coco.celestia.util.convertMillisToDate
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ItemData
import com.coco.celestia.viewmodel.model.MostOrdered
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Composable
fun AddOrderPanel(
    navController: NavController,
    productType: String,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val mostOrdered by orderViewModel.mostOrderedData.observeAsState(emptyList())
    val products by productViewModel.productData.observeAsState(emptyList())
    val currentMonth = LocalDate.now().month
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("All", "Coffee", "Meat", "Vegetable")

    LaunchedEffect(Unit) {
        orderViewModel.fetchMostOrderedItems()
        productViewModel.fetchProducts(
            filter = "",
            role = "Farmer"
        )
    }

//    val inSeasonProducts = products.filter { product ->
//        val sanitizedStartSeason = product.startSeason.trim().uppercase(Locale.ROOT)
//        val sanitizedEndSeason = product.endSeason.trim().uppercase(Locale.ROOT)
//
//        val startMonth = try {
//            Month.valueOf(sanitizedStartSeason)
//        } catch (e: IllegalArgumentException) { return@filter false }
//
//        val endMonth = try {
//            Month.valueOf(sanitizedEndSeason)
//        } catch (e: IllegalArgumentException) { return@filter false }
//
//        when {
//            startMonth.value <= endMonth.value -> {
//                currentMonth.value in startMonth.value..endMonth.value
//            }
//            else -> {
//                currentMonth.value >= startMonth.value || currentMonth.value <= endMonth.value
//            }
//        }
//    }

    BackHandler {
        navController.navigateUp()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClientBG)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .semantics { testTag = "android:id/AddOrderPanel" }
    ) {
        Row {
            SearchBar(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .offset(y = (-40).dp),
                query = text,
                onQueryChange = {
                    text = it
                },
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = {
                    Text("Search Product")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (text != "") {
                        Icon(
                            modifier = Modifier
                                .clickable {
                                    text = ""
                                },
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Icon"
                        )
                    }
                },
                content = {}
            )

            Box {
                Button(
                    onClick = { expanded = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(LGContainer)
                            .padding(17.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = " Filter Icon",
                            tint = Color.White
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(text = type) },
                            onClick = {
                                selectedType = type
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (text != "" || selectedType != "All") {
            DisplaySearchedProduct(
                text,
                selectedType,
                productViewModel,
                navController,
                userViewModel
            )
        } else {
            DisplayMostOrdered(
                mostOrdered,
                navController,
                userViewModel
            )
//            DisplayInSeason(
//                inSeasonProducts,
//                navController,
//                userViewModel
//            )
            DisplayProducts(
                productViewModel,
                navController,
                userViewModel
            )
        }
    }
}

@Composable
fun DisplaySearchedProduct(
    keyword: String,
    selectedType: String,
    productViewModel: ProductViewModel,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val products by productViewModel.productData.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(
            filter = "",
            role = "Client"
        )
    }
    val filteredProducts = products.filter { product ->
        (product.name.contains(keyword, ignoreCase = true) &&
                (selectedType == "All" || product.type.equals(selectedType, ignoreCase = true)))
    }

    Column {
        filteredProducts.chunked(3).forEach { chunk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunk.forEach { product ->
                    ProductCard(
                        product = product,
                        navController = navController,
                        userViewModel = userViewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayMostOrdered(
    products: List<MostOrdered>,
    navController: NavController,
    userViewModel: UserViewModel
) {
    Text(
        text = "Most Ordered Products",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                navController = navController,
                userViewModel = userViewModel,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun DisplayInSeason(
    products: List<ProductData>,
    navController: NavController,
    userViewModel: UserViewModel
) {
    Text(
        text = "In Season Vegetables",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                navController = navController,
                userViewModel = userViewModel,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun DisplayProducts(
    productViewModel: ProductViewModel,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val products by productViewModel.productData.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(
            filter = "",
            role = "Client"
        )
    }

    Text(
        text = "Products",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Column {
        products.chunked(3).forEach { chunk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunk.forEach { product ->
                    ProductCard(
                        product = product,
                        navController = navController,
                        userViewModel = userViewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun <T> ProductCard(
    product: T,
    navController: NavController,
    userViewModel: UserViewModel,
    modifier: Modifier,
) {
    var orderData by remember { mutableStateOf(OrderData()) }
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var orderConfirmed by remember { mutableStateOf(false) }
    var toastEvent by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }

    val productName = when (product) {
        is MostOrdered -> product.name
        is ProductData -> product.name
        is ItemData -> product.name
        else -> ""
    }

    val productType = when (product) {
        is MostOrdered -> product.type
        is ProductData -> product.type
        else -> ""
    }

    val productPrice = when (product) {
        is MostOrdered -> product.priceKg
//        is ProductData -> product.priceKg
        else -> 0.0
    }

    LaunchedEffect(product) {
        if (productName.isNotEmpty()) {
            ImageService.fetchProductImage(productName) {
                productImage = it
            }
        }
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = 16.dp * 2
    val horizontalSpacing = 8.dp * 2
    val itemWidth = (screenWidth - horizontalPadding - horizontalSpacing) / 3

    Column(
        modifier = modifier
            .fillMaxWidth()
            .width(itemWidth)
            .clickable { isSheetOpen = true },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(2)
                )
        ) {
            Image(
                painter = rememberImagePainter(data = productImage ?: R.drawable.product_image),
                contentScale = ContentScale.Crop,
                contentDescription = "Product Image",
                modifier = Modifier.size(100.dp)
            )
        }

        Text(
            text = productName,
            modifier = Modifier
                .height(55.dp)
                .padding(5.dp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false },
        ) {
            Column {
                TopAppBar(
                    title = { Text(text = "Order Summary") }
                )
                AddOrderForm(
                    userViewModel = userViewModel,
                    productPrice = productPrice,
                    productType = productType,
                    productName = productName,
                    onOrder = {
                        orderData = it
                    },
                    onSheetChanged = { isSheetOpen = it },
                    onOrderConfirmed = { orderConfirmed = it }
                )
            }
        }
    }

    if (orderConfirmed) {
        ConfirmOrderRequestPanel(
            navController = navController,
            order = orderData,
            userViewModel = userViewModel,
            onAddToCartEvent = { toastEvent = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderForm(
    userViewModel: UserViewModel,
    productPrice: Double,
    productType: String?,
    productName: String?,
    onOrder: (OrderData) -> Unit,
    onSheetChanged: (Boolean) -> Unit,
    onOrderConfirmed: (Boolean) -> Unit
) {
    val userData by userViewModel.userData.observeAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showAvailableFarmersDialog by remember { mutableStateOf(false) }
    var targetDateDialog by remember { mutableStateOf(true) }
    val datePickerState = rememberDatePickerState()
    var selectedDate by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(0) }
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val order = OrderData(
        orderId = "Order-${UUID.randomUUID()}",
        orderDate = formattedDateTime,
        targetDate = selectedDate,
        status = "PENDING",
//        orderData = ProductData(productName.toString(), quantity, productType.toString(), productPrice.toDouble()),
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
        SummaryDetails("Name", order.client)
        SummaryDetails("Phone Number", userData?.phoneNumber.toString())
        SummaryDetails("Email", userData?.email.toString())
        SummaryDetails("Address", "${order.barangay}, ${order.street}")
        SummaryDetails("Date Ordered", order.orderDate)
        SummaryDetails("Product", productName.toString())
//        SummaryDetails("Price", "₱${order.orderData.priceKg}")

        Spacer(modifier = Modifier.height(25.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            if (productType == "Vegetable") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAvailableFarmersDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.farmer),
                        contentDescription = "Show available farmers",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show Farmers"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

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
                label = { Text("Weight (kg)") },
                placeholder = { Text("e.g. 10.5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/QuantityInputField" }
            )
        }

        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Target Date") },
            placeholder = { Text(selectedDate) },
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        showDatePicker = !showDatePicker
                        if (showDatePicker) {
                            coroutineScope.launch {
                                delay(300)
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
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
                    .bringIntoViewRequester(bringIntoViewRequester)
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

        LaunchedEffect(datePickerState.selectedDateMillis) {
            datePickerState.selectedDateMillis?.let { millis ->
                selectedDate = convertMillisToDate(millis)
                showDatePicker = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = {
            targetDateDialog = false
            onSheetChanged(false)
        }) {
            Text(
                text = "Cancel",
                fontSize = 16.sp,
                modifier = Modifier.semantics { testTag = "CancelButton" }
            )
        }
        TextButton(
            onClick = {
                onOrder(order)
                targetDateDialog = false
                onSheetChanged(false)
                onOrderConfirmed(true)
            },
            enabled = selectedDate.isNotEmpty() && quantity != 0,
            modifier = Modifier.semantics { testTag = "ConfirmOrderButton" }
        ) {
            Text(
                text = "Confirm Order",
                fontSize = 16.sp
            )
        }
    }

    if (showAvailableFarmersDialog) {
        ShowAvailableFarmers(
            productName = productName ?: "",
            onDismissRequest = { showAvailableFarmersDialog = false }
        )
    }
}

@Composable
fun SummaryDetails (label: String, value: String) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.semantics { testTag = "android:id/$label" }
        )
        Text(value)
    }
    Divider(
        modifier = Modifier
            .padding(vertical = 5.dp),
        thickness = 2.dp
    )
}

@Composable
fun ConfirmOrderRequestPanel(
    navController: NavController,
    order: OrderData,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val userData by userViewModel.userData.observeAsState()
    val orderState by orderViewModel.orderState.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val barangay = userData?.barangay ?: ""
    val streetNumber = userData?.streetNumber ?: ""
    val orderSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isOrderSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(barangay, streetNumber) {
        if (barangay.isEmpty() && streetNumber.isEmpty()) {
            onAddToCartEvent(Triple(ToastStatus.WARNING, "Please complete your address details.", System.currentTimeMillis()))
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.AddOrder.route) { inclusive = true }
            }
        } else {
            val transaction = TransactionData(
                transactionId = "Transaction-${UUID.randomUUID()}",
                type = "OrderPlaced",
                date = order.orderDate,
                description = "Order placed due in ${order.targetDate}"
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
        }
        is OrderState.ERROR -> {
            onAddToCartEvent(
                Triple(
                    ToastStatus.FAILED,
                    "Error: ${(orderState as OrderState.ERROR).message}",
                    System.currentTimeMillis()
                )
            )
        }
        is OrderState.SUCCESS -> {
            userData?.let {
                isOrderSheetOpen = true
            }
        }
        else -> {}
    }

    if (isOrderSheetOpen) {
        LaunchedEffect(Unit) {
            orderSheetState.expand()
        }

        ModalBottomSheet(
            sheetState = orderSheetState,
            onDismissRequest = {
                isOrderSheetOpen = false
                navController.navigate(Screen.Client.route) {
                    popUpTo(Screen.Splash.route)
                }
            },
        ) {
            Column {
                Box (
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        TopAppBar(
                            title = {
                                Text(text = "Order Confirmation")
                            }
                        )

                        Column (
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Thank you for your order!",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                            Text(
                                text = "We’ve received your order and are preparing it for you. We hope you enjoy your purchase!",
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                            Text(
                                text = "Feel free to order again anytime. \uD83D\uDE0A",
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(56.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
//                            navController.navigate(Screen.ClientOrderDetails.createRoute(order.orderId))
                        }
                    ) {
                        Text(
                            text = "Track Order",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
