package com.coco.celestia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun SupplierInventory(){
    Box(modifier = Modifier.fillMaxSize()){
        Column (modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
            Text(text = " Supplier Inventory TEST", fontSize = 30.sp)
        }
    }
}