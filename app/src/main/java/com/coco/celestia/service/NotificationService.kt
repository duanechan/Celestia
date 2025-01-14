package com.coco.celestia.service

import android.util.Log
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationStatus
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NotificationService {
    private val usersRef = FirebaseDatabase.getInstance().getReference().child("users")

    fun pushNotifications(
        sender: String,
        recipient: String,
        message: String
    ) {
        val query = usersRef.child(recipient).child("notifications").push()

        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
        val formattedDateTime = LocalDateTime.now().format(formatter)

        query.setValue(
            Notification(
                sender = sender,
                recipient = recipient,
                timestamp = formattedDateTime,
                message = message,
                status = NotificationStatus.New
            )
        )
    }
}