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
    val orderId: Long,
    val clientName: String,
    val orderDate: Date,
    val productType: ProductType,
    val city: String,
    val postalCode: Int,
    val barangay: String,
    val street: String,
    val additionalInfo: String
)