package com.coco.celestia

import java.util.Date

data class UserData(
    val email: String,
    val username: String,
    val firstname: String,
    val lastname: String,
    val password: String
)

data class OrderData(
    var orderDate: String = "",
    val product: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val city: String = "",
    val postalCode: Int = 0,
    val barangay: String = "",
    val street: String = "",
    val additionalInfo: String = ""
)