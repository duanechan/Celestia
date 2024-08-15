package com.coco.celestia

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.CelestiaTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.time.Instant
import java.util.Date
import java.util.Locale

class AddOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB)) // Hex color))
                ) {
                    val navController = rememberNavController()
                    AddOrderNav(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AddOrderPanel(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add Order", fontSize = 25.sp)
        Spacer(modifier = Modifier.height(150.dp))
        Card(
            modifier = Modifier
                .size(width = 300.dp, height = 150.dp)
                .clickable {
                    navController.navigate(Screen.OrderDetails.createRoute("Coffee"))
                }
        ) {
            Text(text = "Coffee", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(20.dp))
        }
        Spacer(modifier = Modifier.height(15.dp))
        Card(
            modifier = Modifier
                .size(width = 300.dp, height = 150.dp)
                .clickable {
                    navController.navigate(Screen.OrderDetails.createRoute("Meat"))
                }
        ) {
            Text(text = "Meat", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(20.dp))
        }
        Spacer(modifier = Modifier.height(15.dp))
        Card(
            modifier = Modifier
                .size(width = 300.dp, height = 150.dp)
                .clickable {
                    navController.navigate(Screen.OrderDetails.createRoute("Vegetable"))
                }
        ) {
            Text(text = "Vegetable", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(20.dp))
        }
    }
}

@Composable
fun OrderDetailsPanel(navController: NavController, product: String?) {
    var productList by remember { mutableStateOf(mapOf<String, Int>()) }
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("products")

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = product.toString(),
            fontSize = 25.sp)
        Spacer(modifier = Modifier.height(150.dp))
        OrderDetails(navController, product, productList, databaseReference) {
            productList = it
        }
    }
}

private fun fetchProductList(
    databaseReference: DatabaseReference,
    product: String?,
    onProductsFetched: (Map<String, Int>) -> Unit
) {
    databaseReference.orderByChild("type").equalTo(product).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val productList = snapshot.children.mapNotNull {
                it.key?.let { key -> key to it.child("quantity").getValue(Int::class.java) }
            }
                .filter { it.second != null }.associate { it.first to it.second!! }
            onProductsFetched(productList)
        }

        override fun onCancelled(error: DatabaseError) {

        }
    })
}


@Composable
fun OrderDetails(navController: NavController, product: String?, productList: Map<String, Int>,
                 databaseReference: DatabaseReference, onProductsFetched: (Map<String, Int>) -> Unit) {
    when (product) {
        "Coffee" -> {
            LaunchedEffect(Unit) {
                fetchProductList(databaseReference, "coffee", onProductsFetched)
            }
        }
        "Meat" -> {
            LaunchedEffect(Unit) {
                fetchProductList(databaseReference, "meat", onProductsFetched)
            }
        }
        else -> {
            LaunchedEffect(Unit) {
                fetchProductList(databaseReference, "vegetable", onProductsFetched)
            }
        }
    }
    DisplayProductList(navController, productList, product)
}

@Composable
fun DisplayProductList(navController: NavController, productList: Map<String, Int>, product: String?) {
    val quantities = remember { mutableStateMapOf<String, Int>().apply { putAll(productList) } }

    if (productList.isNotEmpty()) {
        productList.forEach { (type, quantity) ->
            val productType = type.replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            Card(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .animateContentSize()
            ) {
                var expanded by remember { mutableStateOf(false) }
                Column(
                    Modifier
                        .clickable { expanded = !expanded }
                        .padding(16.dp)
                ) {
                    Text(
                        text = productType,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "${quantity}kg",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                    AnimatedVisibility(expanded) {
                        QuantitySelector(
                            onQuantityChange = { newQty ->
                                quantities[type] = newQty
                            },
                            navController = navController,
                            product = product,
                            productType = productType
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(
    navController: NavController,
    onQuantityChange: (Int) -> Unit,
    product: String?,
    productType: String?
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
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(
                onClick = {
                    if (quantity > 0)
                        quantity--
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                modifier = Modifier.size(48.dp)
            ) {
                Text("-", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$quantity kg",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Button(
                onClick = {
                    if (quantity < 500)
                        quantity++
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                modifier = Modifier.size(48.dp)
            ) {
                Text("+", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = {
                navController.navigate(Screen.OrderConfirmation.createRoute(product.toString(), productType.toString(), quantity))
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

    LaunchedEffect(quantity) {
        onQuantityChange(quantity)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfirmOrderRequestPanel(navController: NavController, product: String?, type: String?, quantity: Int?) {
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var barangay by remember { mutableStateOf("") }
    var streetAndNumber by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }

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
                        Date.from(Instant.now()).toString(),
                        product.toString(),
                        type.toString(),
                        quantity!!,
                        city,
                        postalCode.toInt(),
                        barangay,
                        streetAndNumber,
                        additionalInfo
                    )
                    placeOrder(navController, order)
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

private fun placeOrder(navController: NavController, order: OrderData) {
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
    databaseReference.push().setValue(order)
        .addOnCompleteListener{
            Toast.makeText(navController.context, "Order Placed", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.AddOrder.route)
        }
        .addOnFailureListener{
            Toast.makeText(navController.context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}
