package com.coco.celestia.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object AttachFileService {
    private val storage = FirebaseStorage.getInstance().getReference()
    private val attachmentsReference = storage.child("attachments/special-requests")
    private val fileCache = mutableMapOf<String, List<Uri>>()

    fun uploadAttachment(requestId: String, fileUri: Uri, fileName: String, onSuccess: (Boolean) -> Unit) {
        val query = attachmentsReference.child(requestId).child(fileName)
        query.putFile(fileUri)
            .addOnSuccessListener {
                onSuccess(true)
            }.addOnFailureListener {
                onSuccess(false)
            }
    }

    fun uploadMultipleAttachments(
        requestId: String,
        files: List<Pair<Uri, String>>,
        onProgress: (Float) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        var uploadedCount = 0
        var failedUploads = 0

        files.forEach { (fileUri, fileName) ->
            val query = attachmentsReference.child(requestId).child(fileName)
            query.putFile(fileUri)
                .addOnSuccessListener {
                    uploadedCount++
                    onProgress(uploadedCount.toFloat() / files.size)

                    if (uploadedCount + failedUploads == files.size) {
                        onComplete(failedUploads == 0)
                    }
                }
                .addOnFailureListener {
                    failedUploads++
                    if (uploadedCount + failedUploads == files.size) {
                        onComplete(false)
                    }
                }
        }
    }

    fun fetchAttachments(requestId: String, onComplete: (List<Uri>) -> Unit) {
        fileCache[requestId]?.let {
            onComplete(it)
            return
        }

        attachmentsReference.child(requestId).listAll()
            .addOnSuccessListener { result ->
                if (result.items.isEmpty()) {
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                val downloadUrls = mutableListOf<Uri>()
                var completedTasks = 0

                result.items.forEach { item ->
                    item.downloadUrl
                        .addOnSuccessListener { uri ->
                            downloadUrls.add(uri)
                            completedTasks++

                            if (completedTasks == result.items.size) {
                                fileCache[requestId] = downloadUrls
                                onComplete(downloadUrls)
                            }
                        }
                        .addOnFailureListener {
                            completedTasks++
                            if (completedTasks == result.items.size) {
                                onComplete(downloadUrls)
                            }
                        }
                }
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    fun deleteAttachment(requestId: String, fileName: String, onComplete: (Boolean) -> Unit) {
        val query = attachmentsReference.child(requestId).child(fileName)
        query.delete()
            .addOnSuccessListener {
                fileCache.remove(requestId)
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun deleteAllAttachments(requestId: String, onComplete: (Boolean) -> Unit) {
        attachmentsReference.child(requestId).listAll()
            .addOnSuccessListener { result ->
                if (result.items.isEmpty()) {
                    onComplete(true)
                    return@addOnSuccessListener
                }

                var deletedCount = 0
                var failedDeletions = 0

                result.items.forEach { item ->
                    item.delete()
                        .addOnSuccessListener {
                            deletedCount++
                            if (deletedCount + failedDeletions == result.items.size) {
                                fileCache.remove(requestId)
                                onComplete(failedDeletions == 0)
                            }
                        }
                        .addOnFailureListener {
                            failedDeletions++
                            if (deletedCount + failedDeletions == result.items.size) {
                                onComplete(false)
                            }
                        }
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: "${System.currentTimeMillis()}"
    }
}