package com.coco.celestia.viewmodel.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class UserData(
    val email: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val role: String = "",
    val phoneNumber: String = "",
    val streetNumber: String = "",
    val barangay: String = "",
    var isChecked: Boolean = false
)

data class OrderData(
    val orderId: String = "",
    val orderDate: String = "",
    val status: String = "",
    var orderData: MutableList<ProductData> = mutableListOf(),
    val client: String = "",
    val barangay: String = "",
    val street: String = "",
)

data class ProductData(
    val name: String = "",
    val quantity: Int = 0,
    val type: String = ""
)

data class TransactionData(
    val transactionId: String = "",
    val order: OrderData = OrderData()
)

data class ContactData(
    val name: String = "",
    val contactNumber: Long = 0,
    val email: String = "",
    val facebook: String = "",
    val role: String = "",
)

data class LocationData(
    val street: String = "",
    val barangay: String = ""
)

data class CartData(val items: MutableList<ProductData> = mutableListOf())