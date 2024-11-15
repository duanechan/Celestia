package com.coco.celestia.service

import android.util.Log
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object NotificationService {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference()

    fun pushNotifications(
        uid: String,
        onComplete: (MutableList<Notification>) -> Unit,
        onError: (String) -> Unit
    ) {
        val notifications = mutableListOf<Notification>()
        var ordersLoaded = false
        var transactionsLoaded = false

        fun checkAndComplete() {
            if (ordersLoaded && transactionsLoaded) {
                Log.d("notifications", notifications.toString())
                onComplete(notifications)
            }
        }

        listenToPath(
            uid = uid,
            path = "orders",
            onDataChange = {
                notifications.addAll(it)
                ordersLoaded = true
                checkAndComplete()
            },
            onError = {
                onError(it.message)
            }
        )

        listenToPath(
            uid = uid,
            path = "transactions",
            onDataChange = {
                notifications.addAll(it)
                transactionsLoaded = true
                checkAndComplete()
            },
            onError = {
                onError(it.message)
            }
        )
    }

    private fun listenToPath(
        uid: String,
        path: String,
        onDataChange: (List<Notification>) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        database.child(path).child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataList = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(
                        when (path) {
                            "orders" -> OrderData::class.java
                            "transactions" -> TransactionData::class.java
                            else -> Error::class.java
                        }
                    )
                }
                processData(dataList, onProcessed = { onDataChange(it) })
            }


            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    private fun processData(list: List<Any>, onProcessed: (List<Notification>) -> Unit) {
        val processed = list.map { item ->
            when (item) {
                is OrderData -> Notification(
                    timestamp = item.orderDate,
                    message = "Your Order(#${
                        item.orderId.substring(6, 11).uppercase()
                    }) " + when (item.status) {
                        "REJECTED" -> "has been rejected (Reason: ${item.rejectionReason})"
                        "PENDING" -> "is pending. Please wait for further updates."
                        "PARTIALLY_FULFILLED" -> "is partially fulfilled. "
                        "HARVESTING_MEAT" -> "is getting harvested. Please wait for further updates."
                        "PLANTING" -> "is being planting. Please wait for further updates."
                        "HARVESTING" -> "is getting harvested. Please wait for further updates."
                        "ACCEPTED" -> "has been accepted by a farmer."
                        "DELIVERING" -> "is being delivered."
                        "COMPLETED"-> "has been completed. Thank you for ordering!"
                        "RECEIVED" -> "You have received your order. Thank you for ordering!"
                        "CANCELLED" -> "has been cancelled."
                        else -> "UNKNOWN STATUS"
                    } + when {
                        item.fulfilledBy.any{ it.status == "PLANTING"} -> {
                            "Planting the crop ordered. Please wait for further updates."
                        }
                        item.fulfilledBy.any{ it.status == "HARVESTING"} -> {
                            "Harvesting the crop. Please wait for further updates."
                        }
                        item.fulfilledBy.any{ it.status == "HARVESTING_MEAT"} -> {
                            "Harvesting meat. Please wait for further updates."
                        }
                        else -> ""
                    },
                    status = item.status
                )

                is TransactionData -> Notification(
                    timestamp = item.date,
                    message = item.description,
                    status = item.status ?: "UNKNOWN"
                // If TransactionData has status, otherwise set a default
                )

                else -> Notification() // Uses the default status value ("UNKNOWN")
            }
        }
        onProcessed(processed)
    }
}