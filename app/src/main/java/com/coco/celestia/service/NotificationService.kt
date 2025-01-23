package com.coco.celestia.service

import android.widget.GridLayout.Spec
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.DataParser
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor.Order
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
                        .mapNotNull { DataParser.parseNotificationData(it) }
                    onNotificationsChanged(notifications)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error)
                }
            })
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
                    .mapNotNull { DataParser.parseUserData(it) }
                    .filter {
                        when (type) {
                            NotificationType.Notice -> it.firstname.isNotEmpty()
                            NotificationType.ClientOrderPlaced -> {
                                (details as OrderData).orderData.any { product ->
                                    "Coop${product.type}" == it.role
                                }
                            }
                            NotificationType.OrderUpdated -> {
                                "${it.firstname} ${it.lastname}" == (details as OrderData).client
                            }
                            NotificationType.FarmerCalamityAffected,
                            NotificationType.ClientSpecialRequest -> {
                                it.role == "Admin"
                            }

                            NotificationType.CoopSpecialRequestUpdated -> {
                                val description = (details as SpecialRequest).trackRecord
                                    .maxByOrNull { date -> date.dateTime }?.description.toString()
                                when {
                                    description.contains("accepted", ignoreCase = true) -> it.email == details.email
                                    description.contains("assigned", ignoreCase = true) -> {
                                        details.assignedMember.any { member -> member.email == it.email }
                                    }
                                    description.contains("status", ignoreCase = true) -> {
                                        it.role == "Admin" || details.email == it.email
                                    }
                                    else -> false
                                }
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


     fun navigateTo(
        navController: NavController,
        user: UserData,
        role: String,
        notification: Notification
    ) {
        when (notification.type) {
            NotificationType.ClientOrderPlaced -> {
                navController.navigate(
                    Screen.CoopOrderDetails.createRoute(
                        (notification.details as OrderData).orderId
                    )
                )
            }
            NotificationType.CoopSpecialRequestUpdated -> {
                notification.details as SpecialRequest
                when (role) {
                    "Farmer" -> {
                        val farmer = notification.details.assignedMember
                            .find { it.email == user.email }
                        navController.navigate(
                            Screen.FarmerRequestCardDetails.createRoute(
                                notification.details.specialRequestUID,
                                farmer?.email.toString(),
                                farmer?.product.toString()
                            )
                        )
                    }
                    "Client" -> {
                        navController.navigate(
                            Screen.ClientSpecialReqDetails.createRoute(
                                notification.details.specialRequestUID
                            )
                        )
                    }
                    "Admin" -> {
                        navController.navigate(
                            Screen.AdminSpecialRequestsDetails.createRoute(
                                notification.details.specialRequestUID
                            )
                        )
                    }
                    else -> {

                    }
                }
            }
            NotificationType.ClientSpecialRequest -> {
                navController.navigate(
                    Screen.AdminSpecialRequestsDetails.createRoute(
                        (notification.details as SpecialRequest).specialRequestUID
                    )
                )
            }
            NotificationType.FarmerCalamityAffected -> {
                navController.navigate(
                    Screen.AdminSpecialRequestsDetails.createRoute(
                        (notification.details as SpecialRequest).specialRequestUID
                    )
                )
            }

            else -> {}
        }
    }

    fun parseDetails(type: NotificationType, details: Any): String {
        return when (type) {
            NotificationType.OrderUpdated,
            NotificationType.Notice -> "Announcement:"
            NotificationType.ClientOrderPlaced -> {
                var str = "${(details as OrderData).client} ordered ${details.orderData[0].quantity} Kg of ${details.orderData[0].name}"
                if (details.orderData.size > 1) {
                    str += ", and ${details.orderData.size - 1} more..."
                }
                str
            }
            NotificationType.ClientSpecialRequest -> (details as SpecialRequest).description
            NotificationType.CoopSpecialRequestUpdated -> {
                val record = (details as SpecialRequest).trackRecord.maxByOrNull { it.dateTime }
                record?.let {
                    when {
                        it.description.contains("Accepted", ignoreCase = true) -> {
                            "Please wait for further updates."
                        }
                        it.description.contains("Assigned", ignoreCase = true) -> {
                            "View Special Request ${details.specialRequestUID}"
                        }

                        else -> it.description
                    }
                }.toString()
            }
            NotificationType.FarmerCalamityAffected -> {
                "${(details as SpecialRequest).name}'s special request has been affected by a calamity."
            }
        }
//        return when (details) {
//            is OrderData -> {
//                var str = "${details.client} ordered ${details.orderData[0].quantity} Kg of ${details.orderData[0].name}"
//                if (details.orderData.size > 1) {
//                    str += ", and ${details.orderData.size - 1} more..."
//                }
//                str
//            }
//            is SpecialRequest -> {
//                when (type) {
//                    NotificationType.ClientSpecialRequest -> {
//                        "From ${details.name}: ${details.description}"
//                    }
//                    NotificationType.CoopSpecialRequestUpdated -> {
//                        when {
//                            notification.message.contains("accepted", ignoreCase = true) -> {
//                                "Please wait for further updates."
//                            }
//                            notification.message.contains("assigned", ignoreCase = true) -> {
//                                "View Special Request ${details.specialRequestUID}"
//                            }
//                            notification.message.contains("update", ignoreCase = true) -> {
//                                "${(notification.details as SpecialRequest).trackRecord
//                                    .maxByOrNull { it.dateTime }?.description}"
//                            }
//                            else -> "Unknown"
//                        }
//                    }
//                    NotificationType.FarmerCalamityAffected -> {
//                        "${details.name}'s special request has been affected by a calamity."
//                    }
//                    else -> {
//                        "Unknown Special Request Notification Type"
//                    }
//                }
//            }
//            else -> details.toString()
//        }
    }
}