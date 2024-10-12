package com.coco.celestia.screens.client

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.CartViewModel
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
import java.time.Instant
import java.util.Date
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
            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTypeCard(
    product: ProductData,
    navController: NavController,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onOrderVegetable: (SnapshotStateList<ProductData>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var expanded by remember { mutableStateOf(false) }
    val productName = product.name
    val productType = product.type
    val productQuantity = product.quantity
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
            .padding(vertical = 16.dp)
            .animateContentSize()
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
                    modifier = Modifier.fillMaxWidth()
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
                        Text(
                            text = "${productQuantity}kg",
                            fontSize = 20.sp,
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
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    QuantitySelector(
                        navController = navController,
                        cartViewModel = cartViewModel,
                        productType = productType,
                        productName = productName,
                        maxQuantity = productQuantity,
                        onAddToCartEvent = { onAddToCartEvent(it) },
                        onOrderVegetable = { onOrderVegetable(it) }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(15.dp))
}

@Composable
fun OrderDetailsPanel(
    navController: NavController,
    type: String?,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onOrderVegetable: (SnapshotStateList<ProductData>) -> Unit
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = type ?: "Unknown Product",
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.height(150.dp))
        type?.let {
            LaunchedEffect(type) {
                productViewModel.fetchProductByType(type)
            }
            when (productState) {
                is ProductState.EMPTY -> Text("No products available.")
                is ProductState.ERROR -> Text("Error: ${(productState as ProductState.ERROR).message}")
                is ProductState.LOADING -> Text("Loading products...")
                is ProductState.SUCCESS -> {
                    LazyColumn {
                        items(productData) { product ->
                                ProductTypeCard(
                                    product,
                                    navController,
                                    cartViewModel,
                                    orderViewModel,
                                    onAddToCartEvent = { onAddToCartEvent(it) },
                                    onOrderVegetable = { onOrderVegetable(it) }
                                )
                            }
                        }
                    }
                null -> Text("Unknown state")
            }
        }
    }
}

@Composable
fun QuantitySelector(
    navController: NavController,
    cartViewModel: CartViewModel,
    productType: String?,
    productName: String?,
    maxQuantity: Int,
    onAddToCartEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onOrderVegetable: (SnapshotStateList<ProductData>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var quantity by remember { mutableIntStateOf(1) }
    val vegetableProduct = remember { mutableStateListOf<ProductData>() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Slider(
            value = quantity.toFloat(),
            onValueChange = { quantity = it.toInt().coerceIn(1, maxQuantity) },
            valueRange = 1f..maxQuantity.toFloat(),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { quantity -= 1 },
                enabled = quantity > 1,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "Decrement")
            }
            OutlinedTextField( //align with circle size
                value = quantity.toString(),
                onValueChange = { quantity = it.toIntOrNull()?.coerceIn(1, maxQuantity) ?: quantity },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
//                suffix = { Text("kg", fontSize = 15.sp) },
                modifier = Modifier.width(150.dp).padding(8.dp),
                singleLine = true
            )
            Button(
                onClick = { quantity += 1 },
                enabled = quantity < maxQuantity,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = "Increment")
            }
        }

        Button(
            onClick = {
                if(productType.toString() == "Vegetable") {
                    onAddToCartEvent(Triple(ToastStatus.SUCCESSFUL, "Added order.", System.currentTimeMillis()))
                    onOrderVegetable(vegetableProduct)
                    navController.navigate(Screen.Cart.route)
                } else {
                    cartViewModel.addToCart(
                        uid,
                        ProductData(
                            productName.toString(),
                            quantity,
                            productType.toString()
                        )
                    )
                    onAddToCartEvent(Triple(ToastStatus.SUCCESSFUL, "Added to cart.", System.currentTimeMillis()))
                    navController.navigate(Screen.Cart.route)
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(if (productType.toString() == "Vegetable") "Add Order" else "Add to Cart", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Qty of Order",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ConfirmOrderRequestPanel(
    navController: NavController,
    checkoutItems: SnapshotStateList<ProductData>,
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
            val order = OrderData(
                orderId = "ORDR{${UUID.randomUUID()}}",
                orderDate = Date.from(Instant.now()).toString(),
                status = "PENDING",
                orderData = checkoutItems,
                client = "${userData?.firstname} ${userData?.lastname}",
                barangay = barangay,
                street = streetNumber
            )
            val transaction = TransactionData(
                "TRNSCTN{${UUID.randomUUID()}}",
                order
            )
            orderViewModel.placeOrder(uid, order)
            transactionViewModel.recordTransaction(uid, transaction)
        }
    }
    when (orderState) {
        is OrderState.LOADING -> {
            onAddToCartEvent(Triple(ToastStatus.INFO, "Loading...", System.currentTimeMillis()))
        }
        is OrderState.ERROR -> {
            onAddToCartEvent(Triple(ToastStatus.FAILED, "Error: ${(orderState as OrderState.ERROR).message}", System.currentTimeMillis()))
        }
        is OrderState.SUCCESS -> {
            onAddToCartEvent(Triple(ToastStatus.SUCCESSFUL, "Order placed.", System.currentTimeMillis()))
            userData?.let {
                navController.navigate(Screen.Client.route) {
                    popUpTo(Screen.Splash.route)
                }
            }
        }
        else -> {}
    }
}
