package com.coco.celestia.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object ImageService {
    private val storage = FirebaseStorage.getInstance().getReference()
    private val usersReference = storage.child("images/users")
    private val productReference = storage.child("images/products")
    private val statusReference = storage.child("images/status_updates")
    private val imageCache = mutableMapOf<String, Uri?>()

    fun fetchProfilePicture(uid: String, onComplete: (Uri?) -> Unit) {
        val query = usersReference.child(uid).child("profile-pic.jpg")
        query.downloadUrl
            .addOnSuccessListener {
                onComplete(it)
            }.addOnFailureListener {
                onComplete(null)
            }
    }

    fun uploadProfilePicture(uid: String, imageUri: Uri, onSuccess: (Boolean) -> Unit) {
        val query = usersReference.child(uid).child("profile-pic.jpg")
        query.putFile(imageUri)
            .addOnSuccessListener {
                onSuccess(true)
            }.addOnFailureListener {
                onSuccess(false)
            }
    }

    fun fetchProductImage(productId: String, onComplete: (Uri?) -> Unit) {
        imageCache[productId]?.let {
            onComplete(it)
            return
        }

        val query = productReference.child(productId).child("$productId.jpg")
        query.downloadUrl
            .addOnSuccessListener {
                imageCache[productId] = it
                onComplete(it)
            }.addOnFailureListener {
                imageCache[productId] = null
                onComplete(null)
            }
    }

    fun uploadProductPicture(productId: String, imageUri: Uri, onSuccess: (Boolean) -> Unit) {
        val query = productReference.child(productId).child("$productId.jpg")
        query.putFile(imageUri)
            .addOnSuccessListener {
                onSuccess(true)
            }.addOnFailureListener {
                onSuccess(false)
            }
    }

    fun uploadStatusImage(imageUri: Uri, onComplete: (String?) -> Unit) {
        val timestamp = System.currentTimeMillis()
        val statusId = "$timestamp.jpg"
        val query = statusReference.child(statusId)

        query.putFile(imageUri)
            .addOnSuccessListener {
                query.downloadUrl
                    .addOnSuccessListener { uri ->
                        onComplete("images/status_updates/$statusId")
                    }
                    .addOnFailureListener {
                        onComplete(null)
                    }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun fetchStatusImage(imageUrl: String, onComplete: (Uri?) -> Unit) {
        // Using the relative path stored in TrackRecord
        val query = storage.child(imageUrl)
        query.downloadUrl
            .addOnSuccessListener {
                onComplete(it)
            }.addOnFailureListener {
                onComplete(null)
            }
    }
}
