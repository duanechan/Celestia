package com.coco.celestia.screens.client

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.JadeGreen
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.CartState
import com.coco.celestia.viewmodel.CartViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Cart(
    navController: NavController,
    cartViewModel: CartViewModel,
    onTitleChange: (String) -> Unit,
    onCheckoutEvent: (SnapshotStateList<ProductData>) -> Unit,
    onCheckoutErrorEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val context = LocalContext.current
    val cartState by cartViewModel.cartState.observeAsState(CartState.LOADING)
    val cartData by cartViewModel.cartData.observeAsState()
    val checkoutItems = remember { mutableStateListOf<ProductData>() }

    LaunchedEffect(Unit) {
        cartViewModel.getCart(uid = uid)
    }

    if(checkoutItems.size != 0) {
        onTitleChange("${checkoutItems.size} Items for Checkout")
    } else {
        onTitleChange("Your Cart")
    }

    // Checkout panel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 150.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = {
                    if(checkoutItems.size != 0) {
                        onCheckoutEvent(checkoutItems)
                        navController.navigate(Screen.OrderConfirmation.route)
                    } else {
                        onCheckoutErrorEvent(Triple(ToastStatus.WARNING, "Select items to checkout.", System.currentTimeMillis()))
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = LightOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(70.dp)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = if(checkoutItems.size == 0) {
                        Icons.Default.ShoppingCart
                    } else {
                        Icons.Default.CheckCircle
                    },
                    contentDescription = "Cart Icon",
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(text = "Check Order", fontFamily = mintsansFontFamily)
        }
    }

    // Orange background with the clipped corners
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(650.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LightOrange),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 125.dp, 0.dp, 100.dp)
        ) {
            when (cartState) {
                is CartState.ERROR -> { Text(text = "Error: ${(cartState as CartState.ERROR).message}") }
                is CartState.EMPTY -> { Text(text = "Cart is empty.") }
                is CartState.LOADING -> CircularProgressIndicator()
                is CartState.SUCCESS -> {
                    cartData?.let {
                        LazyColumn {
                            items(it.items) { item ->
                                CartItem(
                                    item = item,
                                    onSelect = { checkoutItems.add(it) },
                                    onUnselect = { checkoutItems.remove(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = "Add more items to the cart",
            tint = Color.White,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 30.dp)
        )
    }

}

@Composable
fun CartItem(
    item: ProductData,
    onSelect: (ProductData) -> Unit,
    onUnselect: (ProductData) -> Unit
) {
    var selected by remember { mutableStateOf(false) }
    val name = item.name
    val quantity = item.quantity
    val type = item.type // TODO: Handle type card colors. (green = vegetables, etc.)

    LaunchedEffect(selected) {
        if(selected) {
            onSelect(item)
        } else {
            onUnselect(item)
        }
    }

    Card(
        modifier = Modifier
            .clickable { selected = !selected }
            .padding(0.dp, 10.dp)
            .height(190.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFF9900))
                .padding(25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                )
            }
            Text(
                text = "${quantity}kg",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { selected = !selected },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = JadeGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(70.dp)
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Cart Icon",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}