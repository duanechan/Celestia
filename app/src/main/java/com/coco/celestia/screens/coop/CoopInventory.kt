package com.coco.celestia.screens.coop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.screens.admin.DropdownMenuItem
import com.coco.celestia.ui.theme.DarkGreen
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
        productViewModel.fetchProducts(
            filter = "",
            role = "Coop"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(860.dp)
            .verticalScroll(rememberScrollState())
    ){
        TopBar("Inventory")
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
}

@Composable
fun ProductTypeCards(navController: NavController, productData: List<ProductData>) {
    val productsByType = productData.groupBy { it.type }
    val maxQuantity = 1000f // TODO: There should be a max qty.
    productsByType.forEach { (type, productsOfType) ->
        val highestQuantityByType = productsOfType.sortedByDescending { it.quantity }.take(3)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .clickable { navController.navigate(Screen.CoopProductInventory.createRoute(type)) }
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(text = type, fontSize = 25.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Spacer(modifier = Modifier.height(10.dp))
                highestQuantityByType.forEach { product ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = product.name, fontSize = 18.sp, fontFamily = mintsansFontFamily)
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
        productViewModel.fetchProductByType(type.toString())
    }
    Column( modifier = Modifier
        .fillMaxSize()
        .padding(top = 90.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = type.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily,
            modifier = Modifier.padding(bottom = 10.dp))
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
                    Text(text = product.name, fontSize = 18.sp, fontFamily = mintsansFontFamily, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "${product.quantity}kg", fontSize = 35.sp, fontFamily = mintsansFontFamily)
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
    TopBar("Inventory")
}

@Composable
fun AddProductForm(
    productName: String,
    farmerName: String,
    address: String,
    quantity: Int,
    onProductNameChange: (String) -> Unit,
    onFarmerNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Product ✚",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Product Name
        OutlinedTextField(
            value = productName,
            onValueChange = onProductNameChange,
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Farmer's Name
        OutlinedTextField(
            value = farmerName,
            onValueChange = onFarmerNameChange,
            label = { Text("Farmer's Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Address
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )

        // Date of Delivery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DropdownField(label = "Month", items = listOf("Month"))
            DropdownField(label = "Day", items = listOf("Day"))
            DropdownField(label = "Year", items = listOf("Year"))
        }

        // Harvest Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DropdownField(label = "Month", items = listOf("Month"))
            DropdownField(label = "Day", items = listOf("Day"))
            DropdownField(label = "Year", items = listOf("Year"))
        }

        // Shelf Life
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DropdownField(label = "Years", items = listOf("Years"))
            DropdownField(label = "Months", items = listOf("Months"))
            DropdownField(label = "Days", items = listOf("Days"))
        }

        // Quantity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = quantity.toString(),
                onValueChange = onQuantityChange,
                label = { Text("Qty.") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            DropdownField(label = "Kg", items = listOf("Kg"))
        }
    }
}

@Composable
fun DropdownField(label: String, items: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(items.first()) }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .width(150.dp)
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { selectedText = it },
            label = { Text(label) },
            modifier = Modifier.clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        selectedText = item
                        expanded = false
                    }
                ) {
                    Text(text = item)
                }
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
fun TopBar(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .background(GradientBrush)  // Apply the gradient background
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = mintsansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            modifier = Modifier
                .background(Color.Transparent)
        )
    }
}



