package com.coco.celestia.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object ImageService {
    private val storage = FirebaseStorage.getInstance().getReference()
    private val usersReference = storage.child("images/users")
    private val productReference = storage.child("images/products")
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

    fun fetchProductImage (productName: String, onComplete: (Uri?) -> Unit) {
        imageCache[productName]?.let {
            onComplete(it)
            return
        }

        val query = productReference.child(productName).child("product_image.jpg")
        query.downloadUrl
            .addOnSuccessListener {
                imageCache[productName] = it
                onComplete(it)
            }.addOnFailureListener {
                imageCache[productName] = null
                onComplete(null)
            }
    }

    fun uploadProductPicture (productName: String, imageUri: Uri, onSuccess: (Boolean) -> Unit) {
        val query = productReference.child(productName).child("product_image.jpg")
        query.putFile(imageUri)
            .addOnSuccessListener {
                onSuccess(true)
            }.addOnFailureListener {
                onSuccess(false)
            }
    }
}
