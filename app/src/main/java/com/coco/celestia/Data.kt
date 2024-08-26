package com.coco.celestia

data class UserData(
    val email: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val password: String = "",
    val role: String = ""
)

data class OrderData(
    val orderId: String = "",
    val orderDate: String = "",
    val status: String = "",
    val product: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val city: String = "",
    val postalCode: Int = 0,
    val barangay: String = "",
    val street: String = "",
    val additionalInfo: String = ""
)

data class ProductData(
    val name: String = "",
    val quantity: Int = 0,
    val type: String = ""
)