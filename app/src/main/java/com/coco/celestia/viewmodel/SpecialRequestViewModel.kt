package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

sealed class SpecialReqState {
    data object LOADING : SpecialReqState()
    data object SUCCESS : SpecialReqState()
    data object EMPTY : SpecialReqState()
    data class ERROR(val message: String) : SpecialReqState()
}

class SpecialRequestViewModel : ViewModel() {
    private val database : DatabaseReference = FirebaseDatabase.getInstance().getReference("special_requests")
    private val _specialReqData = MutableLiveData<SpecialRequest>()
    private val _specialReqState = MutableLiveData<SpecialReqState>()
    val specialReqData: LiveData<SpecialRequest> = _specialReqData
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
}