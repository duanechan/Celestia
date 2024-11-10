@file:OptIn(ExperimentalCoilApi::class)

package com.coco.celestia.screens.admin

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.service.ImageService
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.calculateMonthlyInventory
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.MonthlyInventory

@Composable
fun AdminInventory(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    var query by remember { mutableStateOf("Coffee") }
    var selectedButton by remember { mutableStateOf<String?>("Coffee") }
    var selectedTab by remember { mutableStateOf("Current Inventory") }
    var monthlyInventory by remember { mutableStateOf<List<MonthlyInventory>>(emptyList()) }

    LaunchedEffect(query) {
        productViewModel.fetchProducts(filter = query, role = "Admin")
        orderViewModel.fetchAllOrders("", "Admin")
    }

    LaunchedEffect(orderData, productData) {
        monthlyInventory = calculateMonthlyInventory(orderData, productData)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .background(BlueGradientBrush)
                .verticalScroll(rememberScrollState())
        ) {
            TopBarInventory(
                onTabSelected = { selectedTab = it }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 13.dp)
                    .border(1.dp, Color.White, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .height(48.dp)
                    .background(Color.White)
                    .semantics { testTag = "android:id/ButtonRow" },
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        query = "Coffee"
                        selectedButton = "Coffee"
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedButton == "Coffee") DuskyBlue else Color.White,
                        contentColor = if (selectedButton == "Coffee") Color.White else DarkBlue
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { testTag = "android:id/CoffeeButton" }
                ) {
                    Text(
                        text = "COFFEE",
                        fontFamily = mintsansFontFamily,
                        fontWeight = if (selectedButton == "Coffee") FontWeight.Normal else FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        query = "Meat"
                        selectedButton = "Meat"
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedButton == "Meat") DuskyBlue else Color.White,
                        contentColor = if (selectedButton == "Meat") Color.White else DarkBlue
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { testTag = "android:id/MeatButton" }
                ) {
                    Text(
                        text = "MEAT",
                        fontFamily = mintsansFontFamily,
                        fontWeight = if (selectedButton == "Meat") FontWeight.Normal else FontWeight.Bold
                    )
                }
            }

            when (productState) {
                is ProductState.LOADING -> {
                    Text(
                        "Loading products...",
                        color = Color.White,
                        modifier = Modifier.semantics { testTag = "android:id/LoadingText" }
                    )
                }

                is ProductState.ERROR -> {
                    Text(
                        "Failed to load products: ${(productState as ProductState.ERROR).message}",
                        color = Color.Red,
                        modifier = Modifier.semantics { testTag = "android:id/ErrorText" }
                    )
                }

                is ProductState.EMPTY -> {
                    Text(
                        "No products available.",
                        color = Color.White,
                        modifier = Modifier.semantics { testTag = "android:id/EmptyText" }
                    )
                }

                is ProductState.SUCCESS -> {
                    if (selectedTab == "Current Inventory") {
                        AdminItemList(productData, productViewModel, transactionViewModel)
                    } else {
                        AdminMonthlyInventoryList(monthlyInventory)
                    }
                }
            }
        }
    }
}
@Composable
fun AdminItemList(
    itemList: List<ProductData>,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel
) {
    var selectedProduct by remember { mutableStateOf<ProductData?>(null) }
    if (itemList.isNotEmpty()) {
        itemList.forEach { item ->
            AdminItemCard(
                productName = item.name,
                monthly = 0,
                ordered = 0,
                price = item.priceKg,
                current = item.quantity,
                identifier = "current",
                onEditProductClick = {
                    selectedProduct = item
                },
                modifier = Modifier.semantics { testTag = "android:id/AdminItemCard_${item.name}" }
            )
        }

        selectedProduct?.let { product ->
            EditProduct(
                productViewModel = productViewModel,
                transactionViewModel = transactionViewModel,
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
                monthly = item.remainingQuantity,
                ordered = item.totalOrderedThisMonth,
                price = item.priceKg,
                current = item.currentInv,
                identifier = "monthly",
                onEditProductClick = {},
                modifier = Modifier.semantics { testTag = "android:id/MonthlyInventory_${item.productName}" }
            )
        }
    }
}

@Composable
fun AdminItemCard(
    productName: String,
    monthly: Int,
    ordered: Int,
    price: Double,
    current: Int,
    identifier: String,
    onEditProductClick: (ProductData) -> Unit,
    modifier: Modifier = Modifier // Modifier added for customization
) {
    var productImage by remember { mutableStateOf<Uri?>(null) }
    LaunchedEffect(Unit) {
        if (productName.isNotEmpty()) {
            ImageService.fetchProfilePicture(productName) {
                productImage = it
            }
        }
    }

    Card(modifier = modifier
        .fillMaxWidth()
        .height(if (identifier == "monthly") 220.dp else 180.dp)
        .padding(16.dp)
        .semantics { testTag = "android:id/AdminItemCard_${productName}_${identifier}" },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )) {
        var expanded by remember { mutableStateOf(false) }
        val productData = ProductData(
            name = productName,
            quantity = current,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    painter = rememberImagePainter(data = productImage ?: R.drawable.product_image),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                )

                Column(
                    Modifier
                        .clickable { expanded = !expanded }
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
                                .padding(horizontal = 12.dp)
                                .padding(vertical = 6.dp)
                                .weight(1f)
                                .semantics { testTag = "android:id/ProductName_${productName}" },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box (
                            modifier = Modifier
                                .background(Color.Gray, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .width(70.dp)
                                .height(30.dp)
                                .wrapContentSize()
                        ) {
                            Text(
                                text = "₱ $price",
                                fontSize = 18.sp,
                                color = Gray,
                                modifier = Modifier
                                    .semantics { testTag = "android:id/ProductPrice_${productName}" }
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    AdminItemCardDetails("Inventory", "${current}kg")

                    if (identifier == "monthly") {
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 10f), 0f)

                        AdminItemCardDetails("Ordered", "-${ordered}kg")

                        Canvas(
                            Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {

                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                pathEffect = pathEffect,
                                strokeWidth = 5f,

                            )
                        }

                        AdminItemCardDetails("Total", "${monthly}kg")
                    }

                    if (identifier == "current") {
                        IconButton(
                            onClick = { onEditProductClick(productData) },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(8.dp)
                                .semantics { testTag = "android:id/EditButton_${productName}" }
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
    }
}

@Composable
fun AdminItemCardDetails (label: String, value: String) {
    Row (
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontFamily = mintsansFontFamily,
            color = Gray
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontFamily = mintsansFontFamily,
            color = Gray
        )
    }
}
@Composable
fun TopBarInventory(onTabSelected: (String) -> Unit) {
    var selectedOption by remember { mutableIntStateOf(0) }
    Column (
        modifier = Modifier
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
                    modifier = Modifier
                        .padding(8.dp)
                        .semantics { testTag = "android:id/Tab_$title" }
                )
            }
        }
    }
}