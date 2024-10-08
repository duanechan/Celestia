package com.coco.celestia.screens.coop

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.screens.admin.DropdownMenuItem
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
            .fillMaxSize()
            .height(860.dp)
            .background(Color(0xFFEFF0EF))
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

        val cardColor = when (type) {
            "Coffee" -> Color(0xFFDDC8B3)
            else -> Color(0xFFEDCFC0)
        }

        val fontColor = when (type) {
            "Coffee" -> Color(0xFFB06520)
            else -> Color(0xFFE86A33)
        }

        val iconRes = when (type) {
            "Coffee" -> R.drawable.coffeeicon
            else -> R.drawable.meaticon
        }

        val arrowIconRes = when (type){
            "Coffee" -> R.drawable.arrowsicon
            else -> R.drawable.orangearrowsicon
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(8.dp)
                .clickable { navController.navigate(Screen.CoopProductInventory.createRoute(type)) },
            colors = CardDefaults.cardColors(containerColor = cardColor)

        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(35.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = type,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = fontColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "$type icon",
                        modifier = Modifier.size(35.dp)
                    )
                    Spacer(modifier = Modifier.width(170.dp))
                    Image(
                        painter = painterResource(id = arrowIconRes),
                        contentDescription = "$type icon",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(35.dp))
                highestQuantityByType.forEach { product ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = product.name, fontSize = 18.sp, color = fontColor, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        LinearProgressIndicator(
                            progress = product.quantity.toFloat() / maxQuantity,
                            color = fontColor,
                            trackColor = Color.White
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

    val fontColor = when (type) {
        "Coffee" -> Color(0xFFB06520)
        else -> Color(0xFFE86A33)
    }

    val iconRes = when (type) {
        "Coffee" -> R.drawable.coffeeicon
        else -> R.drawable.meaticon
    }

    LaunchedEffect(Unit) {
        productViewModel.fetchProductByType(type.toString())
    }
    Column( modifier = Modifier
        .fillMaxSize()
        .padding(top = 90.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)){
            Text(text = type.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                color = fontColor)

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "$type icon",
                modifier = Modifier
                    .size(35.dp)
                    .padding(top = 5.dp)
            )
        }

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
                    Text(text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "${product.quantity}kg", fontSize = 35.sp, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductForm(
    productViewModel: ProductViewModel,
    quantity: Int,
    onProductNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val productData by productViewModel.productData.observeAsState()
    val productName by productViewModel.productName.observeAsState("")
    val from by productViewModel.from.observeAsState("")

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts("", "Coop")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Product",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // From
        OutlinedTextField(
            value = from,
            onValueChange = {},
            label = { Text("From") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // Product Name
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                readOnly = true,
                value = productName,
                onValueChange = {},
                placeholder = { Text("Select Product") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                productData?.forEach { productItem ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(productItem.name) },
                        onClick = {
                            onProductNameChange(productItem.name)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Quantity
        OutlinedTextField(
            value = if (quantity == 0) "" else quantity.toString(),
            onValueChange = onQuantityChange,
            label = { Text("Quantity (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
    TopBar("Inventory")
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



