package com.coco.celestia.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object ImageService {
    private val storage = FirebaseStorage.getInstance().getReference()
    private val usersReference = storage.child("images/users")

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
}
