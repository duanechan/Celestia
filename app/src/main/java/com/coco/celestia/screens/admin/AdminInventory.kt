package com.coco.celestia.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.BrownCoffee
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.RedMeat
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.util.calculateMonthlyInventory
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.MonthlyInventory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminInventory(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    navController: NavController
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    var query by remember { mutableStateOf("Coffee") }
    var selectedButton by remember { mutableStateOf<String?>("Coffee") } // Row for aligned buttons
    var selectedTab by remember { mutableStateOf("Current Inventory") }
    var monthlyInventory by remember { mutableStateOf<List<MonthlyInventory>>(emptyList()) }

    LaunchedEffect(query) {
        productViewModel.fetchProducts(
            filter = query,
            role = "Admin"
        )
        orderViewModel.fetchAllOrders(
            "",
            "Admin"
        )
    }
    LaunchedEffect(orderData, productData) {
        monthlyInventory = calculateMonthlyInventory(orderData, productData)
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBlue)
                .padding(top = 7.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBarInventory(
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .padding(horizontal = 13.dp)
                    .offset(y = ((-50).dp))
                    .border(1.dp, Color.White, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .height(48.dp)
                    .background(color = Color.White)
                    .semantics { testTag = "ButtonRow" },
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        query = "Coffee"
                        selectedButton = "Coffee"
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =  if (selectedButton == "Coffee") BrownCoffee else Color.White,
                        contentColor = if (selectedButton == "Coffee") Color.White else Color.Black
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { testTag = "CoffeeButton" }
                ) {
                    Text(text = "COFFEE",
                        fontFamily = mintsansFontFamily,
                        fontWeight = if (selectedButton == "Coffee") FontWeight.Normal else FontWeight.Bold)
                }

                Button(
                    onClick = {
                        query = "Meat"
                        selectedButton = "Meat"
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =  if (selectedButton == "Meat") RedMeat else Color.White,
                        contentColor = if (selectedButton == "Meat") Color.White else Color.Black
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { testTag = "MeatButton" }
                ) {
                    Text(text = "MEAT",
                        fontFamily = mintsansFontFamily,
                        fontWeight = if (selectedButton == "Meat") FontWeight.Normal else FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            when (productState) {
                is ProductState.LOADING -> {
                    Text("Loading products...", color = Color.White, modifier = Modifier.semantics { testTag = "LoadingText" }) // Test tag for loading text
                }

                is ProductState.ERROR -> {
                    Text(
                        "Failed to load products: ${(productState as ProductState.ERROR).message}",
                        color = Color.Red,
                        modifier = Modifier.semantics { testTag = "ErrorText" }
                    )
                }

                is ProductState.EMPTY -> {
                    Text("No products available.", color = Color.White, modifier = Modifier.semantics { testTag = "EmptyText" }) // Test tag for empty text
                }

                is ProductState.SUCCESS -> {
                    if (selectedTab == "Current Inventory") {
                        AdminItemList(productData, productViewModel)
                    } else {
                        AdminMonthlyInventoryList(monthlyInventory)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminItemList(itemList: List<ProductData>, productViewModel: ProductViewModel) {
    var selectedProduct by remember { mutableStateOf<ProductData?>(null) }
    if (itemList.isNotEmpty()) {
        itemList.forEach { item ->
            AdminItemCard(
                item.name,
                item.quantity,
                0,
                item.priceKg,
                "current",
                onEditProductClick = {
                    selectedProduct = item
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(50.dp))

        selectedProduct?.let { product ->
            EditProduct(
                productViewModel = productViewModel,
                productData = product,
                onDismiss = {
                    selectedProduct = null
                }
            )
        }
    }
}

@Composable
fun AdminMonthlyInventoryList(itemList: List<MonthlyInventory>) {
    if (itemList.isNotEmpty()) {
        itemList.forEach { item ->
            AdminItemCard(
                productName = item.productName,
                quantity = item.remainingQuantity,
                ordered = item.totalOrderedThisMonth,
                price = item.priceKg,
                identifier = "monthly",
                onEditProductClick = {},
                modifier = Modifier.testTag("MonthlyInventory_${item.productName}")
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun AdminItemCard(
    productName: String,
    quantity: Int,
    ordered: Int,
    price: Double,
    identifier: String,
    onEditProductClick: (ProductData) -> Unit,
    modifier: Modifier = Modifier // Modifier added for customization
) {
    Card(modifier = modifier
        .width(500.dp)
        .height(200.dp)
        .offset(x = (-16).dp, y = (-50).dp)
        .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )) {
        var expanded by remember { mutableStateOf(false) }
        val productData = ProductData(
            name = productName,
            quantity = quantity,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = productName,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray,
                        modifier = Modifier
                            .padding(top = 15.dp, start = 10.dp)
                            .alignBy(LastBaseline)
                    )

                    Text(
                        text = "₱ $price",
                        fontSize = 20.sp,
                        color = Gray,
                        modifier = Modifier
                            .padding(top = 15.dp, start = 10.dp)
                            .alignBy(LastBaseline)
                    )
                }
                Row (
                    modifier = Modifier
                        .padding(top = 15.dp, start = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Inventory",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Gray
                    )
                    Text(
                        text = "${quantity}kg",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Gray
                    )
                }
                if (identifier == "monthly") {
                    Row (
                        modifier = Modifier
                            .padding(top = 15.dp, start = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ordered",
                            fontSize = 20.sp,
                            fontFamily = mintsansFontFamily,
                            color = Gray
                        )
                        Text(
                            text = "-${ordered}kg",
                            fontSize = 20.sp,
                            fontFamily = mintsansFontFamily,
                            color = Gray
                        )
                    }
                }
            }
            if (identifier == "current") {
                IconButton(
                    onClick = { onEditProductClick(productData) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
        }
    }
}

@Composable
fun TopBarInventory(onTabSelected: (String) -> Unit) {
    var selectedOption by remember { mutableIntStateOf(0) }
    Spacer(modifier = Modifier.height(35.dp))
    Column (
        modifier = Modifier
            .statusBarsPadding()
            .wrapContentHeight()
            .padding(vertical = 20.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedOption,
            modifier = Modifier.wrapContentHeight()
        ) {
            val tabTitles = listOf("Current Inventory", "Inventory This Month")
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            text = title,
                            fontFamily = mintsansFontFamily,
                            fontSize = 13.sp,
                            fontWeight = if (selectedOption == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedOption == index,
                    onClick = {
                        selectedOption = index
                        onTabSelected(title)
                    },
                    modifier = Modifier.padding(8.dp).testTag("Tab_$title")
                )
            }
        }
    }
}