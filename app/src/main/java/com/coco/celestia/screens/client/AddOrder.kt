package com.coco.celestia


import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
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
fun ProductCard(product: String, navController: NavController) {
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
fun ProductTypeCard(product: ProductData, navController: NavController) {
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
        onClick = {
            if (productType == "Vegetable") {
                navController.navigate(
                    Screen.OrderConfirmation.createRoute(productType, productName, productQuantity)
                )
            } else {
                expanded = !expanded
            }
        },
        modifier = Modifier
            .padding(vertical = 16.dp)
            .animateContentSize()
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp)
    ) {

        Box(
            modifier = Modifier
                .background(brush = gradientBrush)
                .fillMaxWidth()
                .clickable {
                    if (productType == "Vegetable") {
                        navController.navigate(
                            Screen.OrderConfirmation.createRoute(
                                productName,
                                productType,
                                productQuantity
                            )
                        )
                    } else {
                        expanded = !expanded
                    }
                }
                .padding(16.dp) // padding inside the gradient Box
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = productName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
                if (productType != "Vegetable") {
                    Text(
                        text = "${productQuantity}kg",
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                AnimatedVisibility(expanded) {
                    if (productType != "Vegetable") {
                        QuantitySelector(
                            navController = navController,
                            productType = productType,
                            productName = productName,
                            maxQuantity = productQuantity
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(15.dp))
}

@Composable
fun OrderDetailsPanel(navController: NavController, type: String?, productViewModel: ProductViewModel) {
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
                            ProductTypeCard(product, navController)
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
    productType: String?,
    productName: String?,
    maxQuantity: Int
) {
    var quantity by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (quantity > 0)
                        quantity--
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "-",
                    color = Color.Black,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            TextField( //align with circle size
                value = quantity.toString(),
                onValueChange = { newValue ->
                    quantity = newValue.toIntOrNull() ?: 0
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(100.dp),
                textStyle = TextStyle(fontSize = 25.sp, textAlign = TextAlign.Center),
                singleLine = true
            )
            Button(
                onClick = {
                    if (quantity < maxQuantity)
                        quantity++
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "+",
                    color = Color.Black,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        Button(
            onClick = {
                navController.navigate(Screen.OrderConfirmation.createRoute(productType.toString(), productName.toString(), quantity))
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add Order", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
    type: String?,
    name: String?,
    quantity: Int?,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel
) {
    val userData by userViewModel.userData.observeAsState()
    val orderState by orderViewModel.orderState.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val barangay = userData?.barangay ?: ""
    val streetNumber = userData?.streetNumber ?: ""

    LaunchedEffect(barangay, streetNumber) {
        if (barangay.isEmpty() && streetNumber.isEmpty()) {
            Toast.makeText(navController.context, "Please complete your address details.", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.OrderConfirmation.route) { inclusive = true }
            }
        } else {
            val order = OrderData(
                "ORDR{${UUID.randomUUID()}}",
                Date.from(Instant.now()).toString(),
                "PENDING",
                ProductData(
                    name.toString(),
                    quantity!!,
                    type.toString()
                ),
                barangay,
                streetNumber
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
            Toast.makeText(navController.context, "Placing order...", Toast.LENGTH_SHORT).show()
        }
        is OrderState.ERROR -> {
            Toast.makeText(navController.context, "Error: ${(orderState as OrderState.ERROR).message}", Toast.LENGTH_SHORT).show()
        }
        is OrderState.SUCCESS -> {
            Toast.makeText(navController.context, "Order placed.", Toast.LENGTH_SHORT).show()
            userData?.let {
                navController.navigate(Screen.Client.route) {
                    popUpTo(Screen.Splash.route)
                }
            }
        }
        else -> {}
    }
}
