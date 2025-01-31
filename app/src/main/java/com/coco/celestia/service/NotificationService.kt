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
    private val ordersRef = FirebaseDatabase.getInstance().getReference().child("orders")
    private val specialRequestsRef = FirebaseDatabase.getInstance().getReference().child("special_requests")

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
        detailsId: String
    ): List<UserData> = suspendCoroutine { continuation ->
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    continuation.resume(emptyList())
                    return
                }

                when (type) {
                    NotificationType.Notice -> {
                        val recipients = snapshot.children
                            .mapNotNull { DataParser.parseUserData(it) }
                            .filter { it.firstname.isNotEmpty() }
                        continuation.resume(recipients)
                    }

                    NotificationType.ClientOrderPlaced -> {
                        ordersRef.child(detailsId).get().addOnSuccessListener { orderSnapshot ->
                            val order = DataParser.parseOrderData(orderSnapshot)
                            val recipients = snapshot.children
                                .mapNotNull { DataParser.parseUserData(it) }
                                .filter { userData ->
                                    order?.orderData?.any { product ->
                                        "Coop${product.type}" == userData.role
                                    } == true
                                }
                            continuation.resume(recipients)
                        }.addOnFailureListener {
                            continuation.resume(emptyList())
                        }
                    }

                    NotificationType.OrderUpdated -> {
                        ordersRef.child(detailsId).get().addOnSuccessListener { orderSnapshot ->
                            val order = DataParser.parseOrderData(orderSnapshot)
                            val recipients = snapshot.children
                                .mapNotNull { DataParser.parseUserData(it) }
                                .filter { userData ->
                                    "${userData.firstname} ${userData.lastname}" == order?.client
                                }
                            continuation.resume(recipients)
                        }.addOnFailureListener {
                            continuation.resume(emptyList())
                        }
                    }

                    NotificationType.FarmerCalamityAffected,
                    NotificationType.ClientSpecialRequest -> {
                        val recipients = snapshot.children
                            .mapNotNull { DataParser.parseUserData(it) }
                            .filter { it.role == "Admin" }
                        continuation.resume(recipients)
                    }

                    NotificationType.CoopSpecialRequestUpdated -> {
                        // Search through all users' special requests
                        specialRequestsRef.get().addOnSuccessListener { usersSnapshot ->
                            var foundSpecialRequest: SpecialRequest? = null

                            // Traverse the correct path: special_requests -> userId -> srKey -> sr
                            for (userSnapshot in usersSnapshot.children) {
                                for (srSnapshot in userSnapshot.children) {
                                    val specialRequest = DataParser.parseSpecialRequest(srSnapshot)
                                    if (specialRequest?.specialRequestUID == detailsId) {
                                        foundSpecialRequest = specialRequest
                                        break
                                    }
                                }
                                if (foundSpecialRequest != null) break
                            }

                            if (foundSpecialRequest == null) {
                                continuation.resume(emptyList())
                                return@addOnSuccessListener
                            }

                            val latestRecord = foundSpecialRequest.trackRecord.maxByOrNull { it.dateTime }
                            val description = latestRecord?.description ?: ""

                            val recipients = snapshot.children
                                .mapNotNull { DataParser.parseUserData(it) }
                                .filter { userData ->
                                    when {
                                        description.contains("accepted", ignoreCase = true) -> {
                                            userData.email == foundSpecialRequest.email
                                        }
                                        description.contains("assigned", ignoreCase = true) -> {
                                            foundSpecialRequest.assignedMember.any { it.email == userData.email }
                                        }
                                        description.contains("status", ignoreCase = true) -> {
                                            userData.role == "Admin" || foundSpecialRequest.email == userData.email
                                        }
                                        else -> false
                                    }
                                }
                            continuation.resume(recipients)
                        }.addOnFailureListener {
                            continuation.resume(emptyList())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    suspend fun parseDetailsMessage(type: NotificationType, detailsId: String): String = suspendCoroutine { continuation ->
        when (type) {
            NotificationType.OrderUpdated,
            NotificationType.Notice -> continuation.resume("Announcement:")

            NotificationType.ClientOrderPlaced -> {
                ordersRef.child(detailsId).get().addOnSuccessListener { snapshot ->
                    val order = DataParser.parseOrderData(snapshot)
                    if (order == null) {
                        continuation.resume("Order details not available")
                        return@addOnSuccessListener
                    }

                    var str = "${order.client} ordered ${order.orderData[0].quantity} Kg of ${order.orderData[0].name}"
                    if (order.orderData.size > 1) {
                        str += ", and ${order.orderData.size - 1} more..."
                    }
                    continuation.resume(str)
                }.addOnFailureListener {
                    continuation.resume("Order details not available")
                }
            }

            NotificationType.ClientSpecialRequest -> {
                specialRequestsRef.get().addOnSuccessListener { usersSnapshot ->
                    var foundRequest: SpecialRequest? = null
                    for (userSnapshot in usersSnapshot.children) {
                        for (srSnapshot in userSnapshot.children) {
                            val request = DataParser.parseSpecialRequest(srSnapshot)
                            if (request?.specialRequestUID == detailsId) {
                                foundRequest = request
                                break
                            }
                        }
                        if (foundRequest != null) break
                    }
                    continuation.resume(foundRequest?.description ?: "Special request details not available")
                }.addOnFailureListener {
                    continuation.resume("Special request details not available")
                }
            }

            NotificationType.FarmerCalamityAffected -> {
                specialRequestsRef.get().addOnSuccessListener { usersSnapshot ->
                    var foundRequest: SpecialRequest? = null
                    for (userSnapshot in usersSnapshot.children) {
                        for (srSnapshot in userSnapshot.children) {
                            val request = DataParser.parseSpecialRequest(srSnapshot)
                            if (request?.specialRequestUID == detailsId) {
                                foundRequest = request
                                break
                            }
                        }
                        if (foundRequest != null) break
                    }
                    continuation.resume(
                        if (foundRequest != null) "${foundRequest!!.name}'s special request has been affected by a calamity."
                        else "Calamity notification details not available"
                    )
                }.addOnFailureListener {
                    continuation.resume("Calamity notification details not available")
                }
            }

            NotificationType.CoopSpecialRequestUpdated -> {
                specialRequestsRef.get().addOnSuccessListener { usersSnapshot ->
                    var foundRequest: SpecialRequest? = null

                    for (userSnapshot in usersSnapshot.children) {
                        for (srSnapshot in userSnapshot.children) {
                            val request = DataParser.parseSpecialRequest(srSnapshot)
                            if (request?.specialRequestUID == detailsId) {
                                foundRequest = request
                                break
                            }
                        }
                        if (foundRequest != null) break
                    }

                    if (foundRequest == null) {
                        continuation.resume("Special request details not available")
                        return@addOnSuccessListener
                    }

                    val record = foundRequest!!.trackRecord.maxByOrNull { it.dateTime }
                    val message = record?.let {
                        when {
                            it.description.contains("Accepted", ignoreCase = true) ->
                                "Please wait for further updates."
                            it.description.contains("Assigned", ignoreCase = true) ->
                                "View Special Request ${foundRequest!!.specialRequestUID}"
                            else -> it.description
                        }
                    } ?: "Status update not available"
                    continuation.resume(message)
                }.addOnFailureListener {
                    continuation.resume("Special request details not available")
                }
            }

        }
    }

    suspend fun pushNotifications(
        notification: Notification,
        onComplete: () -> Unit,
        onError: () -> Unit
    ) = coroutineScope {
        try {
            val recipients = resolveDetailsAsync(notification.type, notification.detailsId)

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

    fun navigateTo(
        navController: NavController,
        user: UserData,
        role: String,
        notification: Notification
    ) {
        when (notification.type) {
            NotificationType.ClientOrderPlaced -> {
                navController.navigate(
                    Screen.CoopOrderDetails.createRoute(notification.detailsId)
                )
            }
            NotificationType.CoopSpecialRequestUpdated -> {
                when (role) {
                    "Farmer" -> {
                        specialRequestsRef.get().addOnSuccessListener { usersSnapshot ->
                            for (userSnapshot in usersSnapshot.children) {
                                for (srSnapshot in userSnapshot.children) {
                                    val specialRequest = DataParser.parseSpecialRequest(srSnapshot)
                                    if (specialRequest?.specialRequestUID == notification.detailsId) {
                                        val assignedProduct = specialRequest.assignedMember
                                            .find { it.email == user.email }
                                            ?.product ?: ""

                                        navController.navigate(
                                            Screen.FarmerRequestCardDetails.createRoute(
                                                notification.detailsId,
                                                user.email,
                                                assignedProduct
                                            )
                                        )
                                        return@addOnSuccessListener
                                    }
                                }
                            }
                        }
                    }
                    "Client" -> {
                        navController.navigate(
                            Screen.ClientSpecialReqDetails.createRoute(notification.detailsId)
                        )
                    }
                    "Admin" -> {
                        navController.navigate(
                            Screen.AdminSpecialRequestsDetails.createRoute(notification.detailsId)
                        )
                    }
                }
            }
            NotificationType.ClientSpecialRequest,
            NotificationType.FarmerCalamityAffected -> {
                navController.navigate(
                    Screen.AdminSpecialRequestsDetails.createRoute(notification.detailsId)
                )
            }
            else -> {}
        }
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