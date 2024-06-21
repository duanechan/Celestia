package com.coco.celestia

import java.util.Date

data class OrderData(
    val orderId: Long,
    val clientName: String,
    val orderDate: Date,
    val orderType: OrderType,
    val city: String,
    val postalCode: Int,
    val barangay: String,
    val street: String,
    val additionalInfo: String
)