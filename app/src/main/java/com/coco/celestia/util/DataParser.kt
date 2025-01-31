package com.coco.celestia.util

import com.coco.celestia.viewmodel.model.AssignedMember
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.FullFilledBy
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.ProductReq
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.StatusUpdate
import com.coco.celestia.viewmodel.model.TrackRecord
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.database.DataSnapshot

object DataParser {
    fun parseUserData(snapshot: DataSnapshot): UserData {
        return UserData(
            email = snapshot.child("email").getValue(String::class.java) ?: "",
            firstname = snapshot.child("firstname").getValue(String::class.java) ?: "",
            lastname = snapshot.child("lastname").getValue(String::class.java) ?: "",
            role = snapshot.child("role").getValue(String::class.java) ?: "",
            basket = snapshot.child("basket").children.mapNotNull { item ->
                item.getValue(BasketItem::class.java)
            },
            phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "",
            streetNumber = snapshot.child("streetNumber").getValue(String::class.java) ?: "",
            barangay = snapshot.child("barangay").getValue(String::class.java) ?: "",
            online = snapshot.child("online").getValue(Boolean::class.java) ?: false,
            isChecked = snapshot.child("isChecked").getValue(Boolean::class.java) ?: false,
            registrationDate = snapshot.child("registrationDate").getValue(String::class.java) ?: ""
        )
    }

    fun parseOrderData(snapshot: DataSnapshot): OrderData {
        return OrderData(
            orderId = snapshot.child("orderId").getValue(String::class.java) ?: "",
            orderDate = snapshot.child("orderDate").getValue(String::class.java) ?: "",
            timestamp = snapshot.child("timestamp").getValue(String::class.java) ?: "",
            targetDate = snapshot.child("targetDate").getValue(String::class.java) ?: "",
            status = snapshot.child("status").getValue(String::class.java) ?: "",
            statusDescription = snapshot.child("statusDescription").getValue(String::class.java) ?: "",
            statusHistory = snapshot.child("statusHistory").children
                .mapNotNull { it.getValue(StatusUpdate::class.java) },
            orderData = snapshot.child("orderData").children
                .mapNotNull { it.getValue(ProductData::class.java) },
            client = snapshot.child("client").getValue(String::class.java) ?: "",
            barangay = snapshot.child("barangay").getValue(String::class.java) ?: "",
            street = snapshot.child("street").getValue(String::class.java) ?: "",
            rejectionReason = snapshot.child("rejectionReason").getValue(String::class.java),
            fulfilledBy = snapshot.child("fulfilledBy").children
                .mapNotNull { it.getValue(FullFilledBy::class.java) },
            partialQuantity = snapshot.child("partialQuantity").getValue(Int::class.java) ?: 0,
            fulfilled = snapshot.child("fulfilled").getValue(Int::class.java) ?: 0,
            collectionMethod = snapshot.child("collectionMethod").getValue(String::class.java) ?: "",
            paymentMethod = snapshot.child("paymentMethod").getValue(String::class.java) ?: "",
            attachments = snapshot.child("attachments").children
                .mapNotNull { it.getValue(String::class.java) },
            gcashPaymentId = snapshot.child("gcashPaymentId").getValue(String::class.java) ?: ""
        )
    }
    
    fun parseSpecialRequest(snapshot: DataSnapshot): SpecialRequest {
        return SpecialRequest(
            subject = snapshot.child("subject").getValue(String::class.java) ?: "",
            description = snapshot.child("description").getValue(String::class.java) ?: "",
            products = snapshot.child("products").children
                .mapNotNull { it.getValue(ProductReq::class.java) },
            targetDate = snapshot.child("targetDate").getValue(String::class.java) ?: "",
            collectionMethod = snapshot.child("collectionMethod").getValue(String::class.java) ?: "",
            additionalRequest = snapshot.child("additionalRequest").getValue(String::class.java) ?: "",
            status = snapshot.child("status").getValue(String::class.java) ?: "",
            name = snapshot.child("name").getValue(String::class.java) ?: "",
            email = snapshot.child("email").getValue(String::class.java) ?: "",
            uid = snapshot.child("uid").getValue(String::class.java) ?: "",
            dateRequested = snapshot.child("dateRequested").getValue(String::class.java) ?: "",
            dateAccepted = snapshot.child("dateAccepted").getValue(String::class.java) ?: "",
            dateCompleted = snapshot.child("dateCompleted").getValue(String::class.java) ?: "",
            specialRequestUID = snapshot.child("specialRequestUID").getValue(String::class.java) ?: "",
            assignedMember = snapshot.child("assignedMember").children
                .mapNotNull { it.getValue(AssignedMember::class.java) },
            trackRecord = snapshot.child("trackRecord").children
                .mapNotNull { it.getValue(TrackRecord::class.java) },
            deliveryAddress = snapshot.child("deliveryAddress").getValue(String::class.java) ?: "",
        )
    }

     fun parseNotificationData(snapshot: DataSnapshot): Notification {
        val type = when (snapshot.child("type").getValue(String::class.java) ?: "") {
            "OrderUpdated" -> NotificationType.OrderUpdated
            "ClientOrderPlaced" -> NotificationType.ClientOrderPlaced
            "CoopSpecialRequestUpdated" -> NotificationType.CoopSpecialRequestUpdated
            "ClientSpecialRequest" -> NotificationType.ClientSpecialRequest
            "FarmerCalamityAffected" -> NotificationType.FarmerCalamityAffected
            else -> NotificationType.Notice
        }
        val details: Any = when (type) {
            NotificationType.Notice -> parseUserData(snapshot.child("details"))
            NotificationType.OrderUpdated,
            NotificationType.ClientOrderPlaced -> parseOrderData(snapshot.child("details"))
            NotificationType.CoopSpecialRequestUpdated,
            NotificationType.FarmerCalamityAffected,
            NotificationType.ClientSpecialRequest -> parseSpecialRequest(snapshot.child("details"))

        }

        return Notification(
            timestamp = snapshot.child("timestamp").getValue(String::class.java) ?: "",
            sender = snapshot.child("sender").getValue(String::class.java) ?: "",
            subject = snapshot.child("subject").getValue(String::class.java) ?: "",
            message = snapshot.child("message").getValue(String::class.java) ?: "",
            detailsId = snapshot.child("detailsId").getValue(String::class.java) ?: "",
            type = type,
            hasRead = snapshot.child("hasRead").getValue(Boolean::class.java) ?: false,
        )
    }
}

