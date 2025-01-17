package com.coco.celestia.service

import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.FullFilledBy
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object NotificationService {
    private val usersRef = FirebaseDatabase.getInstance().getReference().child("users")

    fun observeUserNotifications(
        uid: String,
        onNotificationsChanged: (List<Notification>) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        usersRef
            .child(uid)
            .child("notifications")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = snapshot.children
                        .mapNotNull { parseNotificationData(it) }
                    onNotificationsChanged(notifications)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error)
                }
            })
    }

    private fun parseNotificationData(snapshot: DataSnapshot): Notification {
        val type = when (snapshot.child("type").getValue(String::class.java) ?: "") {
            "OrderUpdated" -> NotificationType.OrderUpdated
            "OrderPlaced" -> NotificationType.OrderPlaced
            else -> NotificationType.Notice
        }
        val details: Any = when (type) {
            NotificationType.Notice -> parseUserData(snapshot.child("details"))
            NotificationType.OrderUpdated,
            NotificationType.OrderPlaced -> parseOrderData(snapshot.child("details"))
        }

        return Notification(
            timestamp = snapshot.child("timestamp").getValue(String::class.java) ?: "",
            sender = snapshot.child("sender").getValue(String::class.java) ?: "",
            message = snapshot.child("message").getValue(String::class.java) ?: "",
            details = details,
            type = type,
            hasRead = snapshot.child("hasRead").getValue(Boolean::class.java) ?: false,
        )
    }

    private suspend fun DataSnapshot.findUserByEmailAndName(recipient: UserData): String? = coroutineScope {
        this@findUserByEmailAndName.children
            .firstOrNull { snapshot ->
                val email = snapshot.child("email").getValue(String::class.java)
                val firstname = snapshot.child("firstname").getValue(String::class.java)
                val lastname = snapshot.child("lastname").getValue(String::class.java)

                email == recipient.email &&
                        "$firstname $lastname" == "${recipient.firstname} ${recipient.lastname}"
            }?.key
    }

    private suspend fun resolveDetailsAsync(
        type: NotificationType,
        details: Any
    ): List<UserData> = suspendCoroutine { continuation ->
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    continuation.resume(emptyList())
                    return
                }

                val recipients = snapshot.children
                    .mapNotNull { parseUserData(it) }
                    .filter {
                        when (type) {
                            NotificationType.Notice -> it.firstname.isNotEmpty()
                            NotificationType.OrderPlaced -> {
                                (details as OrderData).orderData.any { product ->
                                    "Coop${product.type}" == it.role
                                }
                            }
                            NotificationType.OrderUpdated -> {
                                "${it.firstname} ${it.lastname}" == (details as OrderData).client
                            }
                        }
                    }
                continuation.resume(recipients)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    suspend fun pushNotifications(
        notification: Notification,
        onComplete: () -> Unit,
        onError: () -> Unit
    ) = coroutineScope {
        try {
            val recipients = resolveDetailsAsync(notification.type, notification.details)

            val usersSnapshot = suspendCoroutine { continuation ->
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }

            val notificationJobs = recipients.map { recipient ->
                async {
                    val userId = usersSnapshot.findUserByEmailAndName(recipient)
                    userId?.let { pushNotificationToUser(it, notification) }
                }
            }

            notificationJobs.awaitAll()
            onComplete()

        } catch (e: Exception) {
            println("Error sending notifications: ${e.message}")
            onError()
        }
    }

    private suspend fun pushNotificationToUser(
        userId: String,
        notification: Notification
    ): Boolean = suspendCoroutine { continuation ->
        usersRef
            .child(userId)
            .child("notifications")
            .push()
            .setValue(notification)
            .addOnSuccessListener { continuation.resume(true) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }

    private fun parseUserData(snapshot: DataSnapshot): UserData {
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

    private fun parseOrderData(snapshot: DataSnapshot): OrderData {
        return OrderData(
            orderId = snapshot.child("orderId").getValue(String::class.java) ?: "",
            orderDate = snapshot.child("orderDate").getValue(String::class.java) ?: "",
            targetDate = snapshot.child("targetDate").getValue(String::class.java) ?: "",
            status = snapshot.child("status").getValue(String::class.java) ?: "",
            orderData = snapshot.child("orderData").children
                .mapNotNull { it.getValue(ProductData::class.java) },
            client = snapshot.child("client").getValue(String::class.java) ?: "",
            barangay = snapshot.child("barangay").getValue(String::class.java) ?: "",
            street = snapshot.child("street").getValue(String::class.java) ?: "",
            rejectionReason = snapshot.child("rejectionReason").getValue(String::class.java) ?: "",
            fulfilledBy = snapshot.child("fulfilledBy").children
                .mapNotNull { it.getValue(FullFilledBy::class.java) },
            partialQuantity = snapshot.child("partialQuantity").getValue(Int::class.java) ?: 0,
            fulfilled = snapshot.child("fulfilled").getValue(Int::class.java) ?: 0,
            collectionMethod = snapshot.child("collectionMethod").getValue(String::class.java) ?: "",
            paymentMethod = snapshot.child("paymentMethod").getValue(String::class.java) ?: ""
        )
    }

    fun markAsRead(
        notification: Notification,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        usersRef
            .child(uid)
            .child("notifications")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notificationKey = snapshot.children.find { snapshot ->
                        snapshot.child("timestamp").getValue(String::class.java) == notification.timestamp &&
                                snapshot.child("sender").getValue(String::class.java) == notification.sender
                    }?.key

                    notificationKey?.let {
                        usersRef
                            .child(uid)
                            .child("notifications")
                            .child(it)
                            .child("hasRead")
                            .setValue(true)
                            .addOnSuccessListener { onComplete() }
                            .addOnFailureListener { e -> onError(e.message.toString()) }
                    }
                }
                override fun onCancelled(error: DatabaseError) { onError(error.message) }
            })
    }
}