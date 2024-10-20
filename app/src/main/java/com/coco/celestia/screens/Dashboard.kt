package com.coco.celestia.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun Dashboard(role: String) {
    /**
     * Display composable/s based on the role parameter.
     *
     * ex:
     * If role is "Coop", display coffee/meat bar.
     * If role is "Client", display pending, in progress, and completed orders.
     * etc...
     */
    Column {
        coffeeBar()
        meatBar()
        orderSummaryBar()
    }


}

@Composable
fun coffeeBar(){
    Row {
        Text(text = "Coffee")
    }
    Row {
        Text(text = "Meat")
    }
}

fun meatBar(){

}

fun orderSummaryBar(){

}