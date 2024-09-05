package com.coco.celestia

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                ) {
//                    val navController = rememberNavController()
//                    NavGraph(navController = navController)
                }
            }
        }
    }
}

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
    Card(
        modifier = Modifier
            .height(150.dp)
            .clickable {
                navController.navigate(Screen.OrderDetails.createRoute(product))
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = product, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(20.dp))
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
            .fillMaxWidth()
            .clickable {
                if (product.toString() == "Vegetable") {
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
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = productName,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = if (productType != "Vegetable") "${productQuantity}kg" else "",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )

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
    Spacer(modifier = Modifier.height(15.dp))
}

@Composable
fun OrderDetailsPanel(navController: NavController, product: String?) {
    val productViewModel: ProductViewModel = viewModel()
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
            text = product ?: "Unknown Product",
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.height(150.dp))
        product?.let {
            LaunchedEffect(product) {
                productViewModel.fetchProduct(product)
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
                else -> {}
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
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxWidth()
            ) {
                Text("-", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }

            TextField(
                value = quantity.toString(),
                onValueChange = {
                    quantity = it.toInt()
                },
                modifier = Modifier
                    .width(225.dp)
            )

            Button(
                onClick = {
                    if (quantity < maxQuantity)
                        quantity++
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxWidth()
            ) {
                Text("+", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = {
                navController.navigate(Screen.OrderConfirmation.createRoute(productType.toString(), productName.toString(), quantity))
            }
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
fun ConfirmOrderRequestPanel(navController: NavController, type: String?, name: String?, quantity: Int?) {
    val orderViewModel: OrderViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val orderState by orderViewModel.orderState.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var barangay by remember { mutableStateOf("") }
    var streetAndNumber by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }

    when (orderState) {
        is OrderState.LOADING -> {
            Toast.makeText(navController.context, "Placing order..", Toast.LENGTH_SHORT).show()
        }
        is OrderState.ERROR -> {
            Toast.makeText(navController.context, "Error: ${(orderState as OrderState.ERROR).message}", Toast.LENGTH_SHORT).show()
        }
        is OrderState.EMPTY -> {
            Toast.makeText(navController.context, "Error: ${(orderState as OrderState.ERROR).message}", Toast.LENGTH_SHORT).show()
        }
        is OrderState.SUCCESS -> {
            Toast.makeText(navController.context, "Order placed.", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.AddOrder.route)
        }

        else -> {}
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Delivery Address", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = city,
            onValueChange = {
                if (it.length <= 15) {
                    city = it
                }
            },
            label = { Text(text = "City") },
            singleLine = true,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = postalCode,
            onValueChange = {
                if (it.length <= 4) {
                    postalCode = it
                }
            },
            label = { Text(text = "Postal Code") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = barangay,
            onValueChange = {
                if (it.length <= 15) {
                    barangay = it
                }
            },
            label = { Text(text = "Barangay") },
            singleLine = true,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = streetAndNumber,
            onValueChange = {
                if (it.length <= 50) {
                    streetAndNumber = it
                }
            },
            label = { Text(text = "Street and Number") },
            singleLine = true,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = additionalInfo,
            onValueChange = {
                if (it.length <= 50) {
                    additionalInfo = it
                }
            },
            label = { Text(text = "Additional Information") },
            singleLine = true,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = {
                if (
                    city.isNotEmpty()
                    && postalCode.isNotEmpty()
                    && barangay.isNotEmpty()
                    && streetAndNumber.isNotEmpty()
                    && additionalInfo.isNotEmpty()
                ) {
                    val order = OrderData(
                        "ORDR{${UUID.randomUUID()}}",
                        Date.from(Instant.now()).toString(),
                        "PENDING",
                        ProductData(
                            name.toString(),
                            quantity!!,
                            type.toString()
                        ),
                        city,
                        postalCode.toInt(),
                        barangay,
                        streetAndNumber,
                        additionalInfo
                    )
                    orderViewModel.placeOrder(uid, order)
                    val transaction = TransactionData(
                        "TRNSCTN{${UUID.randomUUID()}}",
                        order
                    )
                    transactionViewModel.recordTransaction(uid, transaction)
                } else {
                    Toast.makeText(
                        navController.context,
                        "All fields must be filled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .width(285.dp)
                .height(50.dp)) {
            Text(text = "Submit")
        }
    }
}
