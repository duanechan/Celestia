package com.coco.celestia.viewmodel.model

import com.coco.celestia.R
import java.time.LocalDate
import java.time.YearMonth
import kotlin.reflect.KProperty1

data class UserData(
    val email: String = "",
    val firstname: String = "",
    val lastname: String = "",
    var role: String = "",
    val basket: List<BasketItem> = emptyList(),
    val phoneNumber: String = "",
    val streetNumber: String = "",
    val barangay: String = "",
    val online: Boolean = false,
    var isChecked: Boolean = false,
    val registrationDate: String = ""
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

data class BasketItem(
    val id: String = "",
    val product: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val isRetail: Boolean = false,
)

inline fun <reified BasketItem> BasketItem.toMap(): Map<String, Any?> =
    BasketItem::class.members
        .filterIsInstance<KProperty1<BasketItem, *>>()
        .associate { prop -> prop.name to prop.get(this) }

data class SpecialRequest(
    val subject: String = "",
    val description: String = "",
    val products: List<ProductReq> = emptyList(),
    val targetDate: String = "",
    val collectionMethod: String = "",
    val additionalRequest: String = "",
    val status: String = "",
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val dateRequested: String = "",
    val dateAccepted: String = "",
    val dateCompleted: String = "",
    val specialRequestUID: String = "",
    val assignedMember: List<AssignedMember> = emptyList()
)

data class ProductReq(
    val name: String = "",
    val quantity: Int = 0
)

data class AssignedMember(
    val email: String = "",
    val name: String = "",
    val product: String = "",
    val quantity: Int = 0,
    val status: String = ""
)

data class ProductReqValidation(
    val name: Boolean = false,
    val quantity: Boolean = false
)

data class FullFilledBy (
    val farmerName: String = "",
    val quantityFulfilled: Int = 0,
    val status: String = ""
)

data class ProductData(
    val name: String = "",
    val description: String = "",
    val quantity: Int = 0,
    val type: String = "",
    val price: Double = 0.0,
    val shelfLife: Int = 0,
    val origin: String = "",
    val vendor: String = "",
    val purchasingCost: Double = 0.0,
    val openingStock: Double = 0.0,
    val reorderPoint: Double = 0.0,
    val weightUnit: String = Constants.WEIGHT_GRAMS,
    val isInStore: Boolean = true,
    val isActive: Boolean = true,
    val dateAdded: String = "",
    val collectionMethod: String = Constants.COLLECTION_PICKUP,
    val paymentMethod: String = Constants.PAYMENT_CASH
) {
    constructor() : this(
        "", "", 0, "", 0.0, 0, "", "", 0.0,
        0.0, 0.0, Constants.WEIGHT_GRAMS, true, true,
        Constants.COLLECTION_PICKUP, Constants.PAYMENT_CASH
    )
}

object Constants {
    // Weight Units
    const val WEIGHT_GRAMS = "GRAMS"
    const val WEIGHT_KILOGRAMS = "KILOGRAMS"
    const val WEIGHT_POUNDS = "POUNDS"

    // Collection Methods
    const val COLLECTION_PICKUP = "PICKUP"
    const val COLLECTION_DELIVERY = "DELIVERY"

    // Payment Methods
    const val PAYMENT_CASH = "CASH"
    const val PAYMENT_GCASH = "GCASH"
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
    val facility: String = "",
    val isActive: Boolean = true
)

data class PurchaseOrder(
    val id: String = "",
    val vendor: String = "",
    val purchaseNumber: String = "",
    val referenceNumber: String = "",
    val dateAdded: String = "",
    val dateOfPurchase: String = "",
    val shipmentPreference: String = "",
    val customerNotes: String = "",
    val termsAndConditions: String = "",
    val facility: String = "",
    val status: String = "",
    val savedAsDraft: Boolean = false,
    val items: List<PurchaseOrderItem> = emptyList()
)

data class PurchaseOrderItem(
    val itemName: String = "",
    val description: String = "",
    val account: String = "",
    val quantity: Int = 0,
    val rate: Double = 0.0
)

data class SalesData(
    val salesNumber: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val date: String = "",
    val weightUnit: String = Constants.WEIGHT_GRAMS,
    val notes: String = "",
    val facility: String = ""
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
