package com.coco.celestia.screens.client

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun BasketScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val checkoutItems = remember { mutableStateListOf<BasketItem>() }

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
            .padding(15.dp)
    ) {
        when (userState) {
            UserState.EMPTY,
            UserState.EMAIL_SENT_SUCCESS,
            is UserState.LOGIN_SUCCESS,
            UserState.REGISTER_SUCCESS,
            is UserState.ERROR -> BasketError(message = (userState as UserState.ERROR).message ?: "Unknown error")
            UserState.LOADING -> BasketLoading()
            UserState.SUCCESS -> {
                if (userData.basket.isNotEmpty()) {
                    Basket(
                        userViewModel = userViewModel,
                        items = userData.basket,
                        checkoutItems = checkoutItems,
                        onCheckout = {
                            if (it.isNotEmpty()) {
                                val items = it.toList()
                                userViewModel.updateCheckoutItems(uid, items)
                                val itemsJson = Uri.encode(Json.encodeToString(items))
                                navController.navigate(Screen.OrderSummary.createRoute(itemsJson))
                            } else {
                                onEvent(
                                    Triple(
                                        ToastStatus.WARNING,
                                        "Mark the items to checkout.",
                                        System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        onRemove = { onEvent(it) }
                    )
                } else {
                    BasketEmpty()
                }
            }
        }
    }
}

@Composable
fun Basket(
    userViewModel: UserViewModel,
    items: List<BasketItem>,
    checkoutItems: SnapshotStateList<BasketItem>,
    onCheckout: (SnapshotStateList<BasketItem>) -> Unit,
    onRemove: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val productViewModel: ProductViewModel = viewModel()
    val productData by productViewModel.productData.observeAsState(emptyList())
    var removingItem by remember { mutableStateOf<BasketItem?>(null) }
    var removingFacility by remember { mutableStateOf<String?>(null) }
    var deleteModal by remember { mutableStateOf(false) }
    var deleteFacilityModal by remember { mutableStateOf(false) }

    val groupedItems = items.groupBy { it.productType }.mapValues { (_, facilityItems) ->
        facilityItems.groupBy { it.productId }.map { (_, productItems) ->
            val totalQuantity = productItems.sumOf { it.quantity }
            val totalPrice = productItems.sumOf { it.price }
            productItems.first().copy(quantity = totalQuantity, price = totalPrice)
        }
    }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(
            filter = items.joinToString(", ") { it.productId },
            role = "Client"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        groupedItems.forEach { (facility, facilityItems) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Green4)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = facility,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var allChecked by remember { mutableStateOf(false) }
                            Checkbox(
                                checked = allChecked,
                                onCheckedChange = { checked ->
                                    allChecked = checked
                                    if (checked) {
                                        facilityItems.forEach { item ->
                                            if (!checkoutItems.any { it.id == item.id }) {
                                                checkoutItems.add(item)
                                            }
                                        }
                                    } else {
                                        checkoutItems.removeAll { checkItem ->
                                            facilityItems.any { it.id == checkItem.id }
                                        }
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Green1)
                            )

                            IconButton(
                                onClick = {
                                    removingFacility = facility
                                    deleteFacilityModal = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Facility Items",
                                    tint = Green1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = MaterialTheme.colorScheme.onSurface,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    facilityItems.forEach { item ->
                        BasketItemCard(
                            product = productData.find { it.productId == item.productId } ?: ProductData(),
                            item = item,
                            isChecked = checkoutItems.any { it.id == item.id },
                            onAdd = { checkoutItems.add(it) },
                            onUpdate = { old, new -> checkoutItems[checkoutItems.indexOf(old)] = new },
                            onRemove = { checkoutItems.remove(it) },
                            onDelete = {
                                removingItem = item
                                deleteModal = true
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        BasketActions(
            totalPrice = checkoutItems.sumOf { it.price },
            onCheckout = { onCheckout(checkoutItems) }
        )
    }

    if (deleteModal && removingItem != null) {
        AlertDialog(
            onDismissRequest = {
                deleteModal = false
                removingItem = null
            },
            title = {
                Text(
                    text = "Removing ${removingItem?.product}",
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            },
            text = {
                Text(
                    text = "Do you want to remove this item from the basket?",
                    fontFamily = mintsansFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        removingItem?.let { item ->
                            userViewModel.clearItems(listOf(item))
                            checkoutItems.remove(item)
                            onRemove(
                                Triple(
                                    ToastStatus.SUCCESSFUL,
                                    "${item.product} removed from the basket.",
                                    System.currentTimeMillis()
                                )
                            )
                        }
                        deleteModal = false
                        removingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(Green4),
                ) {
                    Text(
                        text = "Remove",
                        fontFamily = mintsansFontFamily,
                        color = Green1
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        deleteModal = false
                        removingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(Color.Gray),
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = mintsansFontFamily,
                        color = Color.White
                    )
                }
            }
        )
    }

    if (deleteFacilityModal && removingFacility != null) {
        AlertDialog(
            onDismissRequest = {
                deleteFacilityModal = false
                removingFacility = null
            },
            title = {
                Text(
                    text = "Removing All Items from ${removingFacility}",
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            },
            text = {
                Text(
                    text = "Do you want to remove all items from this facility?",
                    fontFamily = mintsansFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        removingFacility?.let { facility ->
                            val itemsToRemove = items.filter { it.productType == facility }
                            userViewModel.clearItems(itemsToRemove)
                            checkoutItems.removeAll { checkItem ->
                                itemsToRemove.any { it.id == checkItem.id }
                            }
                            onRemove(
                                Triple(
                                    ToastStatus.SUCCESSFUL,
                                    "All items from $facility removed from the basket.",
                                    System.currentTimeMillis()
                                )
                            )
                        }
                        deleteFacilityModal = false
                        removingFacility = null
                    },
                    colors = ButtonDefaults.buttonColors(Green4),
                ) {
                    Text(
                        text = "Remove All",
                        fontFamily = mintsansFontFamily,
                        color = Green1
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        deleteFacilityModal = false
                        removingFacility = null
                    },
                    colors = ButtonDefaults.buttonColors(Color.Gray),
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = mintsansFontFamily,
                        color = Color.White
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BasketItemCard(
    product: ProductData,
    item: BasketItem,
    isChecked: Boolean,
    onAdd: (BasketItem) -> Unit,
    onUpdate: (BasketItem, BasketItem) -> Unit,
    onRemove: (BasketItem) -> Unit,
    onDelete: () -> Unit
) {
    var image by remember { mutableStateOf<Uri?>(null) }
    var checked by remember { mutableStateOf(false) }
    var updatedQuantity by remember { mutableIntStateOf(item.quantity) }

    LaunchedEffect(Unit) {
        try {
            ImageService.fetchProductImage(productId = item.productId) {
                image = it
            }
        } catch(e: Exception) {
            image = null
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .height(175.dp)
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Delete Item",
                    tint = Green1,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 36.dp)
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        checked = it
                        if (checked) {
                            onAdd(
                                item.copy(
                                    quantity = updatedQuantity,
                                    price = product.price * updatedQuantity,
                                )
                            )
                        } else {
                            onRemove(
                                item.copy(
                                    quantity = updatedQuantity,
                                    price = product.price * updatedQuantity,
                                )
                            )
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = Green1)
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(90.dp)
                ) {
                    Image(
                        painter = if (image != null) {
                            rememberImagePainter(image)
                        } else {
                            painterResource(R.drawable.product_icon)
                        },
                        contentDescription = item.product,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.product,
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    Text(
                        text = "Php ${product.price * updatedQuantity}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                if (updatedQuantity > 1) {
                                    val old = item.copy(
                                        quantity = updatedQuantity,
                                        price = updatedQuantity * product.price
                                    )
                                    updatedQuantity--
                                    val new = old.copy(
                                        quantity = updatedQuantity,
                                        price = updatedQuantity * product.price
                                    )
                                    if (checked) {
                                        onUpdate(old, new)
                                    }
                                }
                            }
                        ) {
                            Text(text = "-", fontWeight = FontWeight.Bold, color = Green1)
                        }
                        Text(text = updatedQuantity.toString(), fontWeight = FontWeight.Bold, color = Green1)
                        TextButton(
                            onClick = {
                                if (updatedQuantity < product.quantity) {
                                    val old = item.copy(
                                        quantity = updatedQuantity,
                                        price = updatedQuantity * product.price
                                    )
                                    updatedQuantity++
                                    val new = old.copy(
                                        quantity = updatedQuantity,
                                        price = updatedQuantity * product.price
                                    )
                                    if (checked) {
                                        onUpdate(old, new)
                                    }
                                }
                            }
                        ) {
                            Text(text = "+", fontWeight = FontWeight.Bold, color = Green1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasketActions(
    totalPrice: Double,
    onCheckout: () -> Unit
) {
    //Total
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Green4)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "Total: PHP $totalPrice",
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = { onCheckout() },
                colors = ButtonDefaults.buttonColors(containerColor = White1),
                elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
            ) {
                Text(
                    text = "Checkout",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun BasketLoading() {
    CircularProgressIndicator()
}

@Composable
fun BasketEmpty() {
    Text(text = "Basket is empty.")
}

@Composable
fun BasketError(message: String) {
    Text(text = "Error: $message")
}
