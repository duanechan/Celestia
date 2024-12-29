package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.FacilityData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class FacilityState {
    data object LOADING : FacilityState()
    data object SUCCESS : FacilityState()
    data object EMPTY : FacilityState()
    data class ERROR (val message: String) : FacilityState()
}

class FacilityViewModel: ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("facilities")
    private val _facilityData = MutableLiveData<FacilityData>()
    private val _facilitiesData = MutableLiveData<List<FacilityData>>()
    private val _facilityState = MutableLiveData<FacilityState>()
    val facilityData: LiveData<FacilityData> = _facilityData
    val facilitiesData: LiveData<List<FacilityData>> = _facilitiesData
    val facilityState: LiveData<FacilityState> = _facilityState

    fun fetchFacilities() {
        _facilityState.value = FacilityState.LOADING

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val facilities = snapshot.children.mapNotNull { it.getValue(FacilityData::class.java) }
                    _facilitiesData.value = facilities
                    _facilityState.value = if (facilities.isEmpty()) FacilityState.EMPTY else FacilityState.SUCCESS
                } else {
                    _facilitiesData.value = emptyList()
                    _facilityState.value = FacilityState.EMPTY
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _facilityState.value = FacilityState.ERROR(error.message)
            }
        })
    }

    fun createFacility(name: String, emails: MutableList<String>, onComplete: () -> Unit, onError: (String) -> Unit) {
        _facilityState.value = FacilityState.LOADING
        viewModelScope.launch {
            try {
                val facilityRef = database.child(name.lowercase())

                facilityRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        if (currentData.value != null) {
                            return Transaction.abort()
                        }

                        val facility = mapOf(
                            "name" to name,
                            "emails" to emails
                        )

                        currentData.value = facility
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        when {
                            error != null -> {
                                onError(error.message)
                                _facilityState.value = FacilityState.ERROR(error.message)
                            }
                            !committed && currentData?.exists() == true -> {
                                onError("$name already exists!")
                                _facilityState.value = FacilityState.ERROR("$name already exists!")
                            }
                            committed -> {
                                onComplete()
                                _facilityState.value = FacilityState.SUCCESS
                            }
                            else -> {
                                onError("Unknown error occurred.")
                                _facilityState.value = FacilityState.ERROR("Unknown error occurred.")
                            }
                        }
                    }

                })

            } catch (e: Exception) {
                _facilityState.value = FacilityState.ERROR(e.message.toString())
            }
        }
    }
}