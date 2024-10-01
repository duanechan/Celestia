package com.coco.celestia.screens.coop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel

@Composable
fun ProcessOrderPanel(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel
) {
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val orderedProduct = orderData[0].orderData.name
    val orderedAmount = orderData[0].orderData.quantity

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when (productState) {
            ProductState.LOADING -> {
                CircularProgressIndicator()
            }
            ProductState.EMPTY -> {
                Text(text = "Idk what product that is")
            }
            is ProductState.ERROR -> {
                Text(text = "Error fetching product: ${(productState as ProductState.ERROR).message}")
            }
            ProductState.SUCCESS -> {
                Text(text = "Requesting ${orderedAmount}kg of $orderedProduct")
            }
        }
    }
}