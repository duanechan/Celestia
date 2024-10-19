package com.coco.celestia.screens.coop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CoopBackground
import com.coco.celestia.ui.theme.DeliveredItem
import com.coco.celestia.ui.theme.GreenBeans
import com.coco.celestia.ui.theme.Kiniing
import com.coco.celestia.ui.theme.Ordered
import com.coco.celestia.ui.theme.Packed
import com.coco.celestia.ui.theme.RawMeat
import com.coco.celestia.ui.theme.RoastedBeans
import com.coco.celestia.ui.theme.Sorted
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.util.calculateMonthlyInventory
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.MonthlyInventory
import com.coco.celestia.viewmodel.model.ProductData

@Composable
fun CoopInventory(navController: NavController, role: String) {
    val productViewModel: ProductViewModel = viewModel()
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(
            filter = "",
            role = role
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .height(860.dp)
            .background(CoopBackground)
            .verticalScroll(rememberScrollState())
    ){
        Spacer(modifier = Modifier.height(100.dp))

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
                if (role == "CoopCoffee") {
                    navController.navigate(Screen.CoopProductInventory.createRoute("Coffee"))
                } else {
                    navController.navigate(Screen.CoopProductInventory.createRoute("Meat"))
                }
            }
        }
    }
}

@Composable
fun ProductTypeInventory(type: String?, userRole: String) {
    val productViewModel: ProductViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val productData by productViewModel.productData.observeAsState(emptyList())
    var monthlyInventory by remember { mutableStateOf<List<MonthlyInventory>>(emptyList()) }
    var selectedTab by remember { mutableStateOf("Current Inventory") }

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
        orderViewModel.fetchAllOrders(
            "",
            userRole
        )
    }
    LaunchedEffect(orderData, productData) {
        monthlyInventory = calculateMonthlyInventory(orderData, productData)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 10.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarCoopInventory(
            onTabSelected = { selectedTab = it }
        )

        Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
            Text(
                text = type.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                color = fontColor
            )

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "$type icon",
                modifier = Modifier
                    .size(35.dp)
                    .padding(top = 5.dp)
            )
        }

        if (selectedTab == "Current Inventory") {
            CoopItemList(productData)
        } else {
            CoopMonthlyItemList(monthlyInventory)
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Ordered
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Ordered",
                    fontSize = 25.sp,
                    modifier = Modifier.padding(10.dp),
                    color = Color.White,
                    fontFamily = mintsansFontFamily,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "kg",
                    fontSize = 35.sp,
                    color = Color.White,
                    fontFamily = mintsansFontFamily,
                    fontWeight = FontWeight.Bold)
            }
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = DeliveredItem
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Delivered",
                    fontSize = 25.sp,
                    modifier = Modifier.padding(10.dp),
                    color = Color.White,
                    fontFamily = mintsansFontFamily,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "kg",
                    fontSize = 35.sp,
                    color = Color.White,
                    fontFamily = mintsansFontFamily,
                    fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun CoopItemList(itemList: List<ProductData>) {
    if (itemList.isNotEmpty()) {
        itemList.forEach { product ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (product.name) {
                        "Green Beans" -> GreenBeans
                        "Roasted Beans" -> RoastedBeans
                        "Packaged Beans" -> Packed
                        "Sorted Beans" -> Sorted
                        "Kiniing" -> Kiniing
                        "Raw Meat" -> RawMeat
                        else -> Color.White // Default color if no match
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${product.quantity}kg",
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
fun CoopMonthlyItemList(itemList: List<MonthlyInventory>) {
    if (itemList.isNotEmpty()) {
        itemList.forEach { product ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (product.productName) {
                        "Green Beans" -> GreenBeans
                        "Roasted Beans" -> RoastedBeans
                        "Packaged Beans" -> Packed
                        "Sorted Beans" -> Sorted
                        "Kiniing" -> Kiniing
                        "Raw Meat" -> RawMeat
                        else -> Color.White // Default color if no match
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = product.productName,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${product.remainingQuantity}kg",
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
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
}

@Composable
fun TopBarCoopInventory(onTabSelected: (String) -> Unit) {
    var selectedOption  by remember { mutableIntStateOf(0) }
    Spacer(modifier = Modifier.height(35.dp))
    Column (
        modifier = Modifier
            .statusBarsPadding()
            .wrapContentHeight()
            .padding(vertical = 20.dp)
    ){
        TabRow(
            selectedTabIndex = selectedOption,
            modifier = Modifier.wrapContentHeight()
        ) {
            val tabTitles = listOf("Current Inventory", "Inventory This Month")
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily,
                        fontSize = 15.sp)},
                    selected = selectedOption == index,
                    onClick = {
                        selectedOption = index
                        onTabSelected(title)
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}








