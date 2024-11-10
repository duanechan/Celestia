package com.coco.celestia.viewmodel.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

data class UserData(
    val email: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val role: String = "",
    val phoneNumber: String = "",
    val streetNumber: String = "",
    val barangay: String = "",
    val online: Boolean = false,
    var isChecked: Boolean = false
)

data class OrderData(
    val orderId: String = "",
    val orderDate: String = "",
    val targetDate: String = "",
    val status: String = "",
    var orderData: ProductData = ProductData(),
    val client: String = "",
    val barangay: String = "",
    val street: String = "",
    val rejectionReason: String? = null,
    val fulfilledBy: List<String> = emptyList(),
    val partialQuantity: Int? = null
)

data class ProductData(
    val name: String = "",
    val quantity: Int = 0,
    val type: String = "",
    val priceKg: Double = 0.0,
    val startSeason: String = "",
    val endSeason: String = "",
    var plantingDate: String = "",
    var duration: Int = 0,
    var durationUnit: CustomDurationUnit = CustomDurationUnit.DAYS,
    var plantingQuantity: Int = 0
)

enum class CustomDurationUnit {
    DAYS,
    WEEKS,
    MONTHS
}

data class TransactionData(
    val transactionId: String = "",
    val type: String = "",
    val date: String = "",
    val description: String = "",
    val status: String = "",
    val productName: String = ""
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

data class ItemData(
    val name: String = "",
    val farmerName: String = "",
    val items: MutableList<ProductData> = mutableListOf(),
)

data class Notification(
    val timestamp: String = "",
    val message: String = "",
)

data class CalendarUIState(
    val yearMonth: YearMonth,
    val dates: List<Date>
) {
    companion object {
        val Init = CalendarUIState (
            yearMonth = YearMonth.now(),
            dates = emptyList()
        )
    }
    data class Date(
        val fullDate: LocalDate,
        val dayOfMonth: String,
        val isSelected: Boolean
    ) {
        companion object {
            val Empty = Date(LocalDate.MIN, "", false)
        }
    }
}

data class MonthlyInventory(
    val productName: String,
    var remainingQuantity: Int,
    val totalOrderedThisMonth: Int,
    val priceKg: Double,
    val currentInv: Int
)

data class MostOrdered (
    val name: String,
    val quantity: Int,
    val type: String
)
