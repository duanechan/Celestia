@file:OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
                        AdminMonthlyInventoryList(monthlyInventory, productViewModel)
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
                productViewModel = productViewModel,
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
fun AdminMonthlyInventoryList(itemList: List<MonthlyInventory>, productViewModel: ProductViewModel) {
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
                productViewModel = productViewModel,
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
    productViewModel: ProductViewModel,
    modifier: Modifier = Modifier
) {
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (productName.isNotEmpty()) {
            ImageService.fetchProductImage(productName) {
                productImage = it
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (identifier == "monthly") 220.dp else 180.dp)
            .padding(16.dp)
            .semantics { testTag = "android:id/AdminItemCard_${productName}_${identifier}" },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        var expanded by remember { mutableStateOf(false) }
        val productData = ProductData(
            name = productName,
            quantity = current,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { testTag = "android:id/AdminItemBox_${productName}" }
        ) {
            Row(
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
                        .semantics { testTag = "android:id/ProductImage_${productName}" }
                )

                Column(
                    Modifier
                        .clickable { expanded = !expanded }
                        .semantics { testTag = "android:id/ProductInfoColumn_${productName}" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = productName,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .padding(vertical = 6.dp)
                                .weight(1f)
                                .semantics { testTag = "android:id/ProductName_${productName}" },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(
                            modifier = Modifier
                                .background(PaleBlue, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .width(70.dp)
                                .height(30.dp)
                                .wrapContentSize()
                                .semantics { testTag = "android:id/ProductPriceContainer_${productName}" }
                        ) {
                            Text(
                                text = "₱ $price",
                                fontSize = 18.sp,
                                color = DarkBlue,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .semantics { testTag = "android:id/ProductPrice_${productName}" }
                            )
                        }
                    }
                    AdminItemCardDetails(
                        label = "Inventory",
                        value = "${current}kg",
                        modifier = Modifier.semantics { testTag = "android:id/InventoryDetail_${productName}" }
                    )

                    if (identifier == "monthly") {
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 10f), 0f)

                        AdminItemCardDetails(
                            label = "Ordered",
                            value = "-${ordered}kg",
                            modifier = Modifier.semantics { testTag = "android:id/OrderedDetail_${productName}" }
                        )

                        Canvas(
                            Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .semantics { testTag = "android:id/SeparatorLine_${productName}" }
                        ) {
                            drawLine(
                                color = DarkBlue,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                pathEffect = pathEffect,
                                strokeWidth = 5f,
                            )
                        }

                        AdminItemCardDetails(
                            label = "Total",
                            value = "${monthly}kg",
                            modifier = Modifier.semantics { testTag = "android:id/TotalDetail_${productName}" }
                        )
                    }

                    if (identifier == "current") {
                        Row (
                            modifier = Modifier
                                .align(Alignment.End)
                        ) {
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Product",
                                    tint = DarkBlue
                                )
                            }

                            IconButton(
                                onClick = { onEditProductClick(productData) },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .semantics { testTag = "android:id/EditButton_${productName}" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = DarkBlue
                                )
                            }

                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false},
                                    title = { Text("Delete Confirmation") },
                                    text = { Text("Are you sure you want to delete? This action cannot be undone.") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                productViewModel.deleteProduct(productName)
                                                showDeleteDialog = false
                                            }
                                        ) {
                                            Text("Delete")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showDeleteDialog = false }
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminItemCardDetails(label: String, value: String, modifier: Modifier) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .semantics { testTag = "AdminItemCardDetailsRow" },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontFamily = mintsansFontFamily,
            color = DarkBlue,
            modifier = Modifier.semantics { testTag = "AdminItemCardDetailsLabel" }
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontFamily = mintsansFontFamily,
            color = DarkBlue,
            modifier = Modifier.semantics { testTag = "AdminItemCardDetailsValue" }
        )
    }
}

@Composable
fun TopBarInventory(onTabSelected: (String) -> Unit) {
    var selectedOption by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .padding(vertical = 20.dp)
            .semantics { testTag = "TopBarInventoryColumn" }
    ) {
        TabRow(
            selectedTabIndex = selectedOption,
            modifier = Modifier
                .wrapContentHeight()
                .semantics { testTag = "TopBarInventoryTabRow" }
        ) {
            val tabTitles = listOf("Current Inventory", "Inventory This Month")
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            text = title,
                            fontFamily = mintsansFontFamily,
                            fontSize = 13.sp,
                            fontWeight = if (selectedOption == index) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.semantics { testTag = "TabText_$title" }
                        )
                    },
                    selected = selectedOption == index,
                    onClick = {
                        selectedOption = index
                        onTabSelected(title)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .semantics { testTag = "Tab_$title" }
                )
            }
        }
    }
}