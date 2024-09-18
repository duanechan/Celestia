package com.coco.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    TopBar()
    Column( modifier = Modifier.height(795.dp)) {
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

@Composable
fun AddProductForm(navController: NavController, type: String?) {
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
            value = "",
            onValueChange = {},
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Farmer's Name
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Farmer's Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Address
        OutlinedTextField(
            value = "",
            onValueChange = {},
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
                value = "36.8",
                onValueChange = {},
                label = { Text("Qty.") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            DropdownField(label = "Kg", items = listOf("Kg"))
        }
        Box(modifier = Modifier.fillMaxWidth()){
            // Floating Action Button (FAB)
            FloatingActionButton(
                onClick = { val productType = "coffee"
                    navController.navigate(Screen.CoopAddProductInventory.createRoute(type = productType))},
                shape = CircleShape,
                containerColor = DarkGreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp))
            }
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
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
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
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            modifier = Modifier.background(Color.Transparent)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = DarkGreen,
            modifier = Modifier.padding(horizontal = 0.dp) // Adjust padding to create space for the FAB
        ) {
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                label = { Text("Dashboard") },
                selected = currentDestination == Screen.Coop.route,
                onClick = {
                    navController.navigate(Screen.Coop.route) {
                        popUpTo(0)
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Items") },
                label = { Text("Items") },
                selected = currentDestination == Screen.CoopInventory.route,
                onClick = {
                    navController.navigate(Screen.CoopInventory.route) {
                        popUpTo(0)
                    }
                }
            )

            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Orders") },
                label = { Text("Orders") },
                selected = currentDestination == Screen.CoopOrder.route,
                onClick = {
                    navController.navigate(Screen.CoopOrder.route) {
                        popUpTo(0)
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile") },
                label = { Text("Profile") },
                selected = currentDestination == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Floating Action Button (FAB)
        FloatingActionButton(
            onClick = { val productType = "coffee" //Temporary
                navController.navigate(Screen.CoopAddProductInventory.createRoute(type = productType))},
            shape = CircleShape,
            containerColor = DarkGreen,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(24.dp))
        }
    }
}


