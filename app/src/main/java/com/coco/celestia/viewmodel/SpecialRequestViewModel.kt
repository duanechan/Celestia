package com.coco.celestia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.service.ImageService
import com.coco.celestia.service.NotificationService
import com.coco.celestia.viewmodel.model.AssignedMember
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.TrackRecord
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class SpecialReqState {
    data object LOADING : SpecialReqState()
    data object SUCCESS : SpecialReqState()
    data object EMPTY : SpecialReqState()
    data class ERROR(val message: String) : SpecialReqState()
}

class SpecialRequestViewModel : ViewModel() {
    private val database : DatabaseReference = FirebaseDatabase.getInstance().getReference("special_requests")
    private val _specialReqData = MutableLiveData<List<SpecialRequest>>()
    private val _assignedData = MutableLiveData<AssignedMember?>()
    private val _specialReqState = MutableLiveData<SpecialReqState>()
    val specialReqData: LiveData<List<SpecialRequest>> = _specialReqData
    val assignedData: LiveData<AssignedMember?> = _assignedData
    val specialReqState: LiveData<SpecialReqState> = _specialReqState

    fun notify(type: NotificationType, specialReq: SpecialRequest) {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
        val formattedDateTime = LocalDateTime.now().format(formatter)

        val notification = Notification(
            timestamp = formattedDateTime,
            sender = specialReq.name,
            details = specialReq,
            subject = when (type) {
                NotificationType.ClientSpecialRequest -> "Special request from ${specialReq.name}: ${specialReq.subject}"
                NotificationType.CoopSpecialRequestUpdated -> formatSubject(specialReq.trackRecord)
                NotificationType.FarmerCalamityAffected -> "Your special request has been affected by a calamity."
                NotificationType.ClientOrderPlaced,
                NotificationType.OrderUpdated,
                NotificationType.Notice -> "Coco"
            },
            message = NotificationService.parseDetails(type, specialReq),
            type = type
        )

        viewModelScope.launch {
            NotificationService.pushNotifications(
                notification = notification,
                onComplete = { },
                onError = { }
            )
        }
    }

    private fun formatSubject(trackRecord: List<TrackRecord>): String {
        val description = trackRecord.maxByOrNull { it.dateTime }?.description.toString()
        return when {
            description.contains("accepted", ignoreCase = true) -> "Your special request has been accepted!"
            description.contains("assigned", ignoreCase = true) -> "You have been assigned to a special request!"
            description.contains("status", ignoreCase = true) -> "Special Request Update"
            else -> "Unknown"
        }
    }

    fun addSpecialRequest(
        uid: String,
        specialReq: SpecialRequest
    ) {
        viewModelScope.launch {
            _specialReqState.value = SpecialReqState.LOADING
            val query = database.child(uid).push()
            query.setValue(specialReq)
                .addOnCompleteListener {
                    viewModelScope.launch { notify(NotificationType.ClientSpecialRequest, specialReq) }
                    _specialReqState.value = SpecialReqState.SUCCESS
                }
                .addOnFailureListener { exception ->
                    _specialReqState.value = SpecialReqState.ERROR(exception.message ?: "Unknown Error")
                }
        }
    }

    fun fetchSpecialRequests(
        filter: String,
        orderBy: String,
        ascending: Boolean
    ) {
        _specialReqState.value = SpecialReqState.LOADING
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")

                val requests = snapshot.children.flatMap { userSnapshot ->
                    userSnapshot.children.mapNotNull { requestSnapshot ->
                        requestSnapshot.getValue(SpecialRequest::class.java)
                    }
                }.let { unsortedRequests ->
                    if (filter.isNotEmpty()) {
                        unsortedRequests.filter { request ->
                            request.status.equals(filter, ignoreCase = true)
                        }
                    } else {
                        unsortedRequests
                    }
                }.let { filteredRequests ->
                    if (orderBy.isNotEmpty()) {
                        val sortedRequests = filteredRequests.sortedByDescending { request ->
                            try {
                                when (orderBy) {
                                    "Requested" -> request.dateRequested.let { LocalDateTime.parse(it, formatter) }
                                    "Accepted" -> request.dateAccepted.let { LocalDateTime.parse(it, formatter) }
                                    else -> null
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (!ascending) sortedRequests.reversed() else sortedRequests
                    } else {
                        filteredRequests
                    }
                }

                _specialReqData.value = requests
                _specialReqState.value = if (requests.isEmpty()) SpecialReqState.EMPTY else SpecialReqState.SUCCESS
            }

            override fun onCancelled(error: DatabaseError) {
                _specialReqState.value = SpecialReqState.ERROR(error.message)
            }
        })
    }

    fun updateSpecialRequest(specialReq: SpecialRequest) {
        viewModelScope.launch {
            _specialReqState.value = SpecialReqState.LOADING
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (user in snapshot.children) {
                            var found = false
                            val requests = user.children

                            for (request in requests) {
                                val requestUid = request.child("specialRequestUID").getValue(String::class.java)
                                if (requestUid == specialReq.specialRequestUID) {
                                    request.ref.setValue(specialReq)
                                        .addOnSuccessListener {
                                            viewModelScope.launch {
                                                // notification code if needed
                                            }
                                            _specialReqState.value = SpecialReqState.SUCCESS
                                        }
                                        .addOnFailureListener { exception ->
                                            _specialReqState.value = SpecialReqState.ERROR(exception.message ?: "Unknown Error")
                                        }
                                    found = true
                                    break
                                }
                            }

                            if(found) {
                                break
                            }
                        }
                    } else {
                        _specialReqState.value = SpecialReqState.EMPTY
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _specialReqState.value = SpecialReqState.ERROR(error.message)
                }
            })
        }
    }

    fun fetchAssignedProducts(
        farmerEmail: String
    ) {
        viewModelScope.launch {
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val assignedProduct = mutableListOf<SpecialRequest>()
                    for (userSnapshot in snapshot.children) {
                        for (requestedSnapshot in userSnapshot.children) {
                            val assignedMemberSnapshot = requestedSnapshot.child("assignedMember")

                            for (memberSnapshot in assignedMemberSnapshot.children) {
                                val email = memberSnapshot.child("email").getValue(String::class.java)

                                if (email == farmerEmail) {
                                    val assignedMember = requestedSnapshot.getValue(SpecialRequest::class.java)
                                    if (assignedMember != null) {
                                        assignedProduct.add(assignedMember)
                                    }
                                }
                            }
                        }
                    }

                    _specialReqData.value = assignedProduct
                    _specialReqState.value = if (assignedProduct.isEmpty()) SpecialReqState.EMPTY else SpecialReqState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _specialReqState.value = SpecialReqState.ERROR(error.message)
                }
            })
        }
    }

    fun fetchRequestByTrackingID(
        farmerTrackingID: String
    ) {
        viewModelScope.launch {
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var assignedMember: AssignedMember? = null

                    for (userSnapshot in snapshot.children) {
                        for (requestedSnapshot in userSnapshot.children) {
                            val assignedMemberSnapshot = requestedSnapshot.child("assignedMember")

                            for (memberSnapshot in assignedMemberSnapshot.children) {
                                val trackID = memberSnapshot.child("trackingID").getValue(String::class.java)

                                if (trackID == farmerTrackingID) {
                                    assignedMember = memberSnapshot.getValue(AssignedMember::class.java)
                                    break
                                }
                            }

                            if (assignedMember != null) break
                        }

                        if (assignedMember != null) break
                    }

                    _assignedData.value = assignedMember
                    _specialReqState.value = assignedMember?.let { SpecialReqState.SUCCESS } ?: SpecialReqState.EMPTY
                }

                override fun onCancelled(error: DatabaseError) {
                    _specialReqState.value = SpecialReqState.ERROR(error.message)
                }
            })
        }
    }
    fun uploadStatusImage(imageUri: Uri, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            _specialReqState.value = SpecialReqState.LOADING
            ImageService.uploadStatusImage(imageUri) { downloadUrl ->
                if (downloadUrl != null) {
                    _specialReqState.value = SpecialReqState.SUCCESS
                    onComplete(downloadUrl)
                } else {
                    _specialReqState.value = SpecialReqState.ERROR("Failed to upload image")
                    onComplete(null)
                }
            }
        }
    }
}