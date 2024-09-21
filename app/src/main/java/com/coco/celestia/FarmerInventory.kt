package com.coco.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush

//Farmer Inventory Interface
@Composable
fun FarmerInventory(navController: NavController) {
    val farmerProductViewModel: ProductViewModel = viewModel()
    val farmerProductData by farmerProductViewModel.productData.observeAsState(emptyList())
    val farmerProductState by farmerProductViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        farmerProductViewModel.fetchProducts(
            filter = "",
            role = "Farmer"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2E3DB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(860.dp)
                .padding(top = 75.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            when (farmerProductState) {
                is ProductState.LOADING -> {
                    Text("Loading products...")
                }
                is ProductState.ERROR -> {
                    Text("Failed to load products: ${(farmerProductState as ProductState.ERROR).message}")
                }
                is ProductState.EMPTY -> {
                    Text("No products available.")
                }
                is ProductState.SUCCESS -> {
                    FarmerProductTypeCards(navController, farmerProductData)
                }
            }
        }
    }
}

@Composable
fun FarmerProductTypeCards(navController: NavController, productData: List<ProductData>) {
    val productsByType = productData.groupBy { it.type }

    productsByType.forEach { (type) ->
        val textColor = when (type) {
            "Vegetable" -> Color(0xFF41644A) // Dark Green
            "Meat" -> Color(0xFFE83333) // Red
            "Coffee" -> Color(0xFFB06520) // Brown
            else -> Color.Gray
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .clickable {
                    navController.navigate(Screen.FarmerProductInventory.createRoute(type))
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(textColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = type,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun FarmerProductTypeInventory(navController: NavController, type: String?) {
    val farmerProductViewModel: ProductViewModel = viewModel()
    val farmerProductData by farmerProductViewModel.productData.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        farmerProductViewModel.fetchProduct(type.toString())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2E3DB)) // Set the background color
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Spacer(modifier = Modifier.height(30.dp))
            Text(text = type.toString(), fontSize = 25.sp, fontWeight = FontWeight.Bold)

            // Display product cards
            farmerProductData.forEach { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF41644A), // Start of gradient
                                        Color(0xFF83CA95)  // End of gradient
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = product.name, fontSize = 18.sp, color = Color.White)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "${product.quantity}kg", fontSize = 18.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}






