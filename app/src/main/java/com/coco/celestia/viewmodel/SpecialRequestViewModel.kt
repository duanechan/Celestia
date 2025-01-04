package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

sealed class SpecialReqState {
    data object LOADING : SpecialReqState()
    data object SUCCESS : SpecialReqState()
    data object EMPTY : SpecialReqState()
    data class ERROR(val message: String) : SpecialReqState()
}

class SpecialRequestViewModel : ViewModel() {
    private val database : DatabaseReference = FirebaseDatabase.getInstance().getReference("special_requests")
    private val _specialReqData = MutableLiveData<List<SpecialRequest>>()
    private val _specialReqState = MutableLiveData<SpecialReqState>()
    val specialReqData: LiveData<List<SpecialRequest>> = _specialReqData
    val specialReqState: LiveData<SpecialReqState> = _specialReqState

    fun addSpecialRequest(
        uid: String,
        specialReq: SpecialRequest
    ) {
        viewModelScope.launch {
            _specialReqState.value = SpecialReqState.LOADING
            val query = database.child(uid).push()
            query.setValue(specialReq)
                .addOnCompleteListener {
                    _specialReqState.value = SpecialReqState.SUCCESS
                }
                .addOnFailureListener { exception ->
                    _specialReqState.value = SpecialReqState.ERROR(exception.message ?: "Unknown Error")
                }
        }
    }

    fun fetchSpecialRequests(
        filter: String
    ) {
        _specialReqState.value = SpecialReqState.LOADING
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.flatMap { userSnapshot ->
                    userSnapshot.children.mapNotNull { requestSnapshot ->
                        requestSnapshot.getValue(SpecialRequest::class.java)
                    }.filter { request ->
                        request.status.equals(filter, ignoreCase = true)
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

    fun updateSpecialRequest (specialReq: SpecialRequest) {
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
}