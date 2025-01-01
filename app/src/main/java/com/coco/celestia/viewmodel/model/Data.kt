package com.coco.celestia.viewmodel.model

import androidx.annotation.Keep
import com.coco.celestia.R
import java.time.LocalDate
import java.time.YearMonth

data class UserData(
    val email: String = "",
    val firstname: String = "",
    val lastname: String = "",
    var role: String = "",
    val phoneNumber: String = "",
    val streetNumber: String = "",
    val barangay: String = "",
    val online: Boolean = false,
    var isChecked: Boolean = false
)

data class FacilityData(
    val icon: Int = R.drawable.facility,
    val name: String = "",
    val emails: MutableList<String> = mutableListOf()
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
    val fulfilledBy: List<FullFilledBy> = emptyList(),
    val partialQuantity: Int = 0,
    val fulfilled: Int = 0
)

data class SpecialRequest(
    val subject: String = "",
    val description: String = "",
    val products: List<Pair<String, Int>> = emptyList(),
    val targetDate: String = "",
    val collectionMethod: String = "",
    val additionalRequest: String = ""
)

data class FullFilledBy (
    val farmerName: String = "",
    val quantityFulfilled: Int = 0,
    val status: String = ""
)

data class ProductData(
    val name: String = "",
    val quantity: Int = 0,
    val type: String = "",
    val price: Double = 0.0,
    val weightUnit: WeightUnit = WeightUnit.GRAMS,
    val isInStore: Boolean = true,
    val dateAdded: String = ""
) {
    constructor() : this("", 0, "", 0.0, WeightUnit.GRAMS, true, "")
}

@Keep
enum class WeightUnit {
    GRAMS, KILOGRAMS, POUNDS
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

data class VendorData(
    val firstName: String = "",
    val lastName: String = "",
    val companyName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val remarks: String = "",
    val isActive: Boolean = true
)

data class Notification(
    val timestamp: String = "",
    val message: String = "",
    val status: String = "UNKNOWN"
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
    val currentInv: Int,
    val type: String
)

data class MostOrdered (
    val name: String,
    val quantity: Int,
    val type: String,
    val priceKg: Double
)
