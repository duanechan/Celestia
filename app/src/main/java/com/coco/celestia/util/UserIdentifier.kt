package com.coco.celestia.util

import androidx.lifecycle.ViewModel
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object UserIdentifier {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val userCache = mutableMapOf<String, UserData>()

    suspend fun getUserData(uid: String, onComplete: (UserData) -> Unit) {
        userCache[uid]?.let {
            onComplete(it)
            return
        }

        try {
            val snapshot = database.child(uid).get().await()
            if (snapshot.exists()) {
                val userData = snapshot.getValue(UserData::class.java)
                userData?.let {
                    userCache[uid] = it
                    onComplete(it)
                } ?: onComplete(UserData())
            } else {
                onComplete(UserData())
            }
        } catch (e: Exception) {
            onComplete(UserData())
        }
    }

    fun clearCache() {
        userCache.clear()
    }
}