package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ProductDetailScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    productId: String,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState()
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        productViewModel.fetchProduct(productId)
    }

    DisposableEffect(productData) {
        if (productData.isNotEmpty()) {
            isLoading = true
            ImageService.fetchProductImage(productId) { uri ->
                productImage = uri
                isLoading = false
            }
        }
        onDispose { }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
    ) {
        when (productState) {
            ProductState.EMPTY -> {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Unknown Product",
                    tint = Green1,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = "Product not found.")
            }
            is ProductState.ERROR -> Text(text = "Error fetching product: ${(productState as ProductState.ERROR).message}")
            ProductState.LOADING -> CircularProgressIndicator()
            ProductState.SUCCESS -> {
                if (productData.isNotEmpty()) {
                    ProductDetails(
                        navController = navController,
                        userViewModel = userViewModel,
                        orderViewModel = orderViewModel,
                        product = productData[0],
                        productImage = productImage,
                        isLoading = isLoading,
                        onEvent = { onEvent(it) },
                    )
                }
            }
            else -> Text(text = "Loading product details...")
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ProductDetails(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    product: ProductData,
    productImage: Uri?,
    isLoading: Boolean,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var selectedQuantity by remember { mutableIntStateOf(0) }
    val currentTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = White1)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading || productImage == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "+ Add\nImage",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
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
        }
        item {
            ProductDetails_StockAndQuantity(
                name = product.name,
                price = product.price,
                selectedQuantity = selectedQuantity,
                maxQuantity = product.quantity,
                onUpdate = { selectedQuantity = it }
            )
        }
        item { Divider(thickness = 1.dp, modifier = Modifier.shadow(elevation = 4.dp)) }
        item { ProductDetails_Breakdown(selectedQuantity = selectedQuantity, price = product.price) }
        item { ProductDetails_Description(description = product.description) }
        item {
            ProductDetails_Action(
                onCheckout = {
                    if (selectedQuantity > 0) {
                        val item = listOf(
                            BasketItem(
                                id = UUID.randomUUID().toString(),
                                productId = product.productId,
                                productType = product.type,
                                product = product.name,
                                price = product.price * selectedQuantity,
                                quantity = selectedQuantity,
                                isRetail = true,
                                timestamp = currentTimestamp
                            )
                        )
                        val itemsJson = Uri.encode(Json.encodeToString(item))
                        navController.navigate(Screen.OrderSummary.createRoute(itemsJson))
                    } else {
                        onEvent(
                            Triple(
                                ToastStatus.WARNING,
                                "Insufficient quantity.",
                                System.currentTimeMillis()
                            )
                        )
                    }
                },
                onAddItem = {
                    if (selectedQuantity > 0) {
                        val item = BasketItem(
                            id = UUID.randomUUID().toString(),
                            productId = product.productId,
                            productType = product.type,
                            product = product.name,
                            price = product.price * selectedQuantity,
                            quantity = selectedQuantity,
                            isRetail = true,
                            timestamp = currentTimestamp
                        )
                        userViewModel.addItem(uid, item)
                        onEvent(
                            Triple(
                                ToastStatus.SUCCESSFUL,
                                "$selectedQuantity ${product.weightUnit.lowercase()} of ${product.name} added to the basket!",
                                System.currentTimeMillis()
                            )
                        )
                        navController.navigate(Screen.Basket.route)
                    } else {
                        onEvent(
                            Triple(
                                ToastStatus.WARNING,
                                "Insufficient quantity.",
                                System.currentTimeMillis()
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ProductDetails_Action(
    onCheckout: () -> Unit,
    onAddItem: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp)
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Green4, contentColor = Green1),
            onClick = { onAddItem() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Add to Basket", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Green4, contentColor = Green1),
            onClick = { onCheckout() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Checkout", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
    }
}

@Composable
fun ProductDetails_Description(description: String) {
    Column(modifier = Modifier.padding(15.dp)) {
        Text(
            text = "Description",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Green1
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(
                text = if (description == "") "No description." else description,
                fontSize = 13.sp,
                textAlign = TextAlign.Justify,
                color = Green1
            )
        }
    }
}

@Composable
fun ProductDetails_Breakdown(selectedQuantity: Int, price: Double) {
    Column(modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = "Price per kilogram", fontSize = 16.sp, color = Green1)
            Text(text = "Php $price", fontSize = 16.sp, color = Green1)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = "Total Quantity", fontSize = 16.sp, color = Green1)
            Text(text = "x $selectedQuantity", fontSize = 16.sp, color = Green1)
        }
        Divider(thickness = 1.dp, modifier = Modifier
            .padding(vertical = 15.dp)
            .shadow(elevation = 4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = "Total Cost", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
            Text(text = "Php ${price * selectedQuantity}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
    }
}

@Composable
fun ProductDetails_Header(name: String, image: Uri?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Image(
            painter = rememberImagePainter(image),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ProductDetails_StockAndQuantity(
    name: String,
    price: Double,
    selectedQuantity: Int,
    maxQuantity: Int,
    onUpdate: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(0) }
    var textFieldValue by remember { mutableStateOf(selectedQuantity.toString()) }

    fun updateQuantityFromText(text: String) {
        if (text.isEmpty()) {
            quantity = 0
            onUpdate(0)
            return
        }

        val newValue = text.toIntOrNull()
        if (newValue != null && newValue <= maxQuantity) {
            quantity = newValue
            onUpdate(newValue)
        }
    }

    Column(modifier = Modifier.padding(15.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green1)
            Text(text = "Php $price", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Quantity", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        if (quantity > 0) {
                            quantity--
                            onUpdate(quantity)
                            textFieldValue = quantity.toString()
                        }
                    },
                    enabled = quantity > 0
                ) {
                    Text(text = "-", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
                }

                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() ||
                            (newValue.all { it.isDigit() } &&
                                    (newValue.toIntOrNull()?.let { it <= maxQuantity } ?: false))
                        ) {
                            textFieldValue = newValue
                            updateQuantityFromText(newValue)
                        }
                    },
                    modifier = Modifier.width(80.dp),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green1,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            updateQuantityFromText(textFieldValue)
                        }
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green1,
                        unfocusedBorderColor = Green1,
                    )
                )

                TextButton(
                    onClick = {
                        if (quantity < maxQuantity) {
                            quantity++
                            onUpdate(quantity)
                            textFieldValue = quantity.toString()
                        }
                    },
                    enabled = quantity < maxQuantity
                ) {
                    Text(text = "+", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
                }
            }
        }
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Available Stocks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
                Text(text = "$maxQuantity Kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
            }
        }
    }
}