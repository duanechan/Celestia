package com.coco.celestia.screens.client

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
    var removingItem by remember { mutableStateOf(BasketItem()) }
    var deleteModal by remember { mutableStateOf(false) }

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
        for (item in items) {
            //facility card added
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Green4)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // First Row: Order ID and Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.productType,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Item",
                            tint = Green1,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .clickable {
                                    removingItem = item
                                    deleteModal = true
                                }
                        )

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Divider(
                        color = MaterialTheme.colorScheme.onSurface,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    BasketItemCard(
                        product = productData.find { it.productId == item.productId } ?: ProductData(),
                        item = item,
                        isChecked = checkoutItems.any { it.id == item.id },
                        onAdd = { checkoutItems.add(it) },
                        onUpdate = { old, new -> checkoutItems[checkoutItems.indexOf(old)] = new },
                        onRemove = { checkoutItems.remove(it) }
                    )

                }
            }
        }
        BasketActions(totalPrice = checkoutItems.sumOf { it.price }, onCheckout = { onCheckout(checkoutItems) })
    }
    if (deleteModal) {
        AlertDialog(
            onDismissRequest = { deleteModal = false },
            title = {
                Text(text = "Removing ${removingItem.product}", fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
            },
            text = {
                Text(text = "Do you want to remove this item from the basket?", fontFamily = mintsansFontFamily)
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.clearItems(listOf(removingItem))
                        deleteModal = false
                        onRemove(
                            Triple(
                                ToastStatus.SUCCESSFUL,
                                "${removingItem.product} removed from the basket.",
                                System.currentTimeMillis()
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(Green4),
                ) {
                    Text(text = "Remove", fontFamily = mintsansFontFamily, color = Green1)
                }
            },
            dismissButton = {
                Button(
                    onClick = { deleteModal = false },
                    colors = ButtonDefaults.buttonColors(Color.Gray),
                ) {
                    Text(text = "Cancel", fontFamily = mintsansFontFamily, color = Color.White)
                }
            },
        )
    }
}


@Composable
fun BasketItemCard(
    product: ProductData,
    item: BasketItem,
    isChecked: Boolean,
    onAdd: (BasketItem) -> Unit,
    onUpdate: (BasketItem, BasketItem) -> Unit,
    onRemove: (BasketItem) -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
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
//                            .width(100.dp)
//                            .fillMaxHeight()
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
                        text = "Php ${product.price * updatedQuantity}", //price is per unit
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
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
