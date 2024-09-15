package com.coco.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel

//@Preview
@Composable
fun CoopInventory(navController: NavController) {
    val productViewModel: ProductViewModel = viewModel()
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(860.dp)
            .padding(top = 75.dp)
            .verticalScroll(rememberScrollState())
    ){
        Spacer(modifier = Modifier.height(15.dp))

        when (productState) {
            is ProductState.LOADING -> {
                Text("Loading products...")
            }
            is ProductState.ERROR -> {
                Text("Failed to load products: ${(productState as ProductState.ERROR).message}")
            }
            is ProductState.EMPTY -> {
                Text("No products available.")
            }
            is ProductState.SUCCESS -> {
                ProductTypeCards(navController, productData)
            }
        }
    }
    TopBar()
}

@Composable
fun ProductTypeCards(navController: NavController, productData: List<ProductData>) {
    val productsByType = productData.groupBy { it.type }
    val maxQuantity = 1000f // TODO: There should be a max qty.
    productsByType.forEach { (type, productsOfType) ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .clickable {
                    navController.navigate(Screen.CoopProductInventory.createRoute(type))
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = type, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                productsOfType.forEach { product ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = product.name, fontSize = 18.sp)
                        Spacer(modifier = Modifier.weight(0.9f))
                        LinearProgressIndicator(
                            progress = product.quantity.toFloat() / maxQuantity,
                            trackColor = Color.LightGray
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun ProductTypeInventory(navController: NavController, type: String?) {
    val productViewModel: ProductViewModel = viewModel()
    val productData by productViewModel.productData.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        productViewModel.fetchProduct(type.toString())
    }
    Column {
        Text(text = type.toString(), fontSize = 25.sp, fontWeight = FontWeight.Bold)
        productData.forEach { product ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = product.name, fontSize = 18.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "${product.quantity}kg", fontSize = 18.sp)
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Ordered", fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "kg", fontSize = 18.sp)
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Delivered", fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "kg", fontSize = 18.sp)
            }
        }
    }
}

// Define the gradient brush TODO: Move to a more appropriate folder
val GradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF83CA95),
        Color(0xFF41644A)
    ),
    start = Offset(0f, 0f),
    end = Offset(500f, 0f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .background(GradientBrush)  // Apply the gradient background
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),  // Fills the entire width
                    contentAlignment = Alignment.Center  // Aligns the content to the center
                ) {
                    Text(
                        text = "Inventory",
                        fontFamily = mintsansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent, // Make TopAppBar background transparent
                titleContentColor = Color.White
            ),
            modifier = Modifier.background(Color.Transparent)  // Ensure transparency in TopAppBar
        )
    }
}

@Preview
@Composable
fun TopBarPreview(){
    TopBar()
}
