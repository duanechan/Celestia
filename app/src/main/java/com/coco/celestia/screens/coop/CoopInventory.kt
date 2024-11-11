package com.coco.celestia.screens.coop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
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
            .semantics { testTag = "android:id/CoopInventoryColumn" }
    ){
        when (productState) {
            is ProductState.LOADING -> {
                Text("Loading products...")
                Modifier.semantics { testTag = "android:id/LoadingText" }
            }
            is ProductState.ERROR -> {
                Text("Failed to load products: ${(productState as ProductState.ERROR).message}")
                Modifier.semantics { testTag = "android:id/ErrorText" }
            }
            is ProductState.EMPTY -> {
                Text("No products available.")
                Modifier.semantics { testTag = "android:id/EmptyText" }
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

    val orderedQuantityAmount = orderData.filter { order ->
        order.status in listOf("PENDING", "PREPARING", "DELIVERING")
    }.fold(0) { accumulator, order ->
        accumulator + order.orderData.quantity
    }

    val deliveredQuantityAmount = orderData.filter { order ->
        order.status == "COMPLETED"
    }.fold(0) { accumulator, order ->
        accumulator + order.orderData.quantity
    }

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
        .verticalScroll(rememberScrollState())
        .semantics { testTag = "android:id/ProductTypeInventoryColumn" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarCoopInventory(
            onTabSelected = { selectedTab = it },
//            Modifier.semantics { testTag = "android:id/TopBarCoopInventory" }
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
                    .semantics { testTag = "android:id/${type}Icon" }
            )
        }

        if (selectedTab == "Current Inventory") {
            CoopItemList(productData)
            Modifier.semantics { testTag = "android:id/CurrentInventory" }
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
                .semantics { testTag = "android:id/OrderedCard" }
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
                Text(text = "${orderedQuantityAmount}kg",
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
                .semantics { testTag = "android:id/DeliveredCard" }
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
                Text(text = "${deliveredQuantityAmount}kg",
                    fontSize = 35.sp,
                    color = Color.White,
                    fontFamily = mintsansFontFamily,
                    fontWeight = FontWeight.Bold)
            }
        }
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
                        "Pork" -> Pork
                        else -> Color.Gray
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(8.dp)
                    .semantics { testTag = "android:id/ProductCard_${product.name}" }
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "₱ ${product.priceKg}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    )
                }

                Row (
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Inventory",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Color.White
                    )
                    Text(
                        text = "${product.quantity}kg",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Color.White
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
                        "Pork" -> Pork
                        else -> Color.Gray
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(8.dp)
                    .semantics { testTag = "android:id/MonthlyInventoryCard_${product.productName}" }
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Text(
                        text = product.productName,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "₱ ${product.priceKg}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    )
                }

                Row (
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Inventory",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Color.White
                    )
                    Text(
                        text = "${product.remainingQuantity}kg",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Color.White
                    )
                }

                Row (
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ordered",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Color.White
                    )
                    Text(
                        text = "-${product.totalOrderedThisMonth}kg",
                        fontSize = 20.sp,
                        fontFamily = mintsansFontFamily,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TopBarCoopInventory(onTabSelected: (String) -> Unit) {
    var selectedOption by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .semantics { testTag = "android:id/TopBarCoopInventoryColumn" }
    ) {
        TabRow(
            selectedTabIndex = selectedOption,
            modifier = Modifier.wrapContentHeight(),
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedOption])
                        .height(4.dp)
                        .background(DeliveringStatus)
                )
            }
        ) {
            val tabTitles = listOf("Current Inventory", "Inventory This Month")
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily,
                            fontSize = 15.sp,
                            color = if (selectedOption == index) DeliveringStatus else PendingStatus // Change text color based on selection
                        )
                    },
                    selected = selectedOption == index,
                    onClick = {
                        selectedOption = index
                        onTabSelected(title)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .semantics { testTag = "android:id/Tab_${title.replace(" ", "")}" }
                )
            }
        }
    }
}