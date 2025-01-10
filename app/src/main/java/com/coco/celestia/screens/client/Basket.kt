package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.builtins.ListSerializer
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
                        productViewModel = productViewModel,
                        items = userData.basket,
                        checkoutItems = checkoutItems,
                        onCheckout = {
                            if (it.isNotEmpty()) {
                                userViewModel.updateCheckoutItems(uid, it)
                                val itemsJson = Uri.encode(Json.encodeToString(it.toList()))
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
                        }
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
    productViewModel: ProductViewModel,
    items: List<BasketItem>,
    checkoutItems: SnapshotStateList<BasketItem>,
    onCheckout: (SnapshotStateList<BasketItem>) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items) { _, item ->
            val productData by productViewModel.productData.observeAsState(emptyList())

            LaunchedEffect(item.product) {
                productViewModel.fetchProduct(item.product)
            }

            BasketItemCard(
                product = if (productData.isNotEmpty()) productData[0] else ProductData(),
                item = item,
                isChecked = checkoutItems.any { it.id == item.id },
                onAdd = { checkoutItems.add(it) },
                onUpdate = { old, new -> checkoutItems[checkoutItems.indexOf(old)] = new },
                onRemove = { checkoutItems.remove(it) }
            )
        }
        item {
            BasketActions(onCheckout = { onCheckout(checkoutItems) })
        }
    }
}

@Composable
fun BasketActions(
    onCheckout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = { onCheckout() },
            colors = ButtonDefaults.buttonColors(containerColor = Green4),
            elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Checkout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
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

//    LaunchedEffect(Unit) {
//        ImageService.fetchProductImage(productName = item.product) {
//            image = it
//        }
//    }

    Card(
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .height(175.dp)
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Green4)
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
                Box(modifier = Modifier.padding(12.dp)) {
                    Image(
                        painter = rememberImagePainter(image),
                        contentDescription = item.product,
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(White1)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, end = 12.dp)
                    ) {
                        Text(text = item.product, fontWeight = FontWeight.Bold, color = Green1)
                        Text(text = "Php ${product.price * updatedQuantity}", fontWeight = FontWeight.Bold, color = Green1)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        TextButton(
                            // TODO: Need to handle minimum order kg
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
