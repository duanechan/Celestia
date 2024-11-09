package com.coco.celestia.service

import android.util.Log
import com.coco.celestia.util.UserIdentifier
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.full.memberProperties

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
                    message =
                        "Your Order(#${item.orderId.substring(6,11).uppercase()}) " +
                        when (item.status) {
                            "REJECTED" -> "has been rejected (Reason: ${item.rejectionReason})"
                            "PENDING" -> "is pending. Please wait for further updates."
                            "PREPARING" -> "is being prepared."
                            "DELIVERING" -> "is being delivered."
                            "COMPLETED", "RECEIVED" -> "has been completed. Thank you for ordering!"
                            "INCOMPLETE" -> "has been partially fulfilled."
                            "CANCELLED" -> "has been cancelled."
                            else -> "UNKNOWN STATUS"
                        }
                )
                is TransactionData -> Notification(
                    timestamp = item.date,
                    message = item.description
                )
                else -> Notification()
            }
        }
        onProcessed(processed)
    }
}
