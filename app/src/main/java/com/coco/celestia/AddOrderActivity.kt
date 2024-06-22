package com.coco.celestia

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.CelestiaTheme

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
                    navController.navigate(Screen.OrderDetails.createRoute(ProductType.COFFEE))
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
                    navController.navigate(Screen.OrderDetails.createRoute(ProductType.MEAT))
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
                    navController.navigate(Screen.OrderDetails.createRoute(ProductType.VEGETABLE))
                }
        ) {
            Text(text = "Vegetable", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(20.dp))
        }
    }
}

@Composable
fun OrderDetailsPanel(navController: NavController, productType: ProductType?) {
    var productQty by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text =
            when (productType) {
                ProductType.COFFEE -> "Coffee"
                ProductType.MEAT -> "Meat"
                else -> "Vegetable"
            },
            fontSize = 25.sp)
        Spacer(modifier = Modifier.height(150.dp))
        when (productType) {
            ProductType.COFFEE -> {
                Card(
                    modifier = Modifier
                        .size(width = 300.dp, height = 150.dp)
                        .clickable {
                            navController.navigate(Screen.OrderConfirmation.createRoute(productType, 1))
                        }
                ) {
                    Text(text = "Green Beans", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(20.dp))
                }
                Spacer(modifier = Modifier.height(15.dp))
                Card(
                    modifier = Modifier
                        .size(width = 300.dp, height = 150.dp)
                        .clickable {
                            navController.navigate(Screen.OrderConfirmation.createRoute(productType, 2))
                        }
                ) {
                    Text(text = "Parchment", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(20.dp))
                }
                Spacer(modifier = Modifier.height(15.dp))
                Card(
                    modifier = Modifier
                        .size(width = 300.dp, height = 150.dp)
                        .clickable {
                            navController.navigate(Screen.OrderConfirmation.createRoute(productType, 3))
                        }
                ) {
                    Text(text = "Packed Coffee", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(20.dp))
                }
            }
            ProductType.MEAT -> {
                Text(text = "Meat Panel", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(20.dp))
            }
            else -> {
                Text(text = "Vegetable Panel", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(20.dp))
            }
        }

    }
}

@Composable
fun ConfirmOrderRequestPanel(navController: NavController, orderType: Int?) {
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
                    Toast.makeText(
                        navController.context,
                        "Order Success",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Database function goes here
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