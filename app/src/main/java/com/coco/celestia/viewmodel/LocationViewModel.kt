package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.ContactData
import com.coco.celestia.LocationData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties

sealed class LocationState {
    object LOADING : LocationState()
    object SUCCESS : LocationState()
    object EMPTY : LocationState()
    data class ERROR(val message: String) : LocationState()
}

class LocationViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("location")
    private val _locationData = MutableLiveData<List<LocationData>>()
    private val _locationState = MutableLiveData<LocationState>()
    val locationData: LiveData<List<LocationData>> = _locationData
    val locationState: LiveData<LocationState> = _locationState

    fun fetchLocations(filter: String) {
        viewModelScope.launch {
            _locationState.value = LocationState.LOADING
            database.orderByChild("barangay").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }

                    val locations = snapshot.children
                        .mapNotNull { it.getValue(LocationData::class.java) }
                        .filter { location ->
                            val matches = filterKeywords.any { keyword ->
                                LocationData::class.memberProperties.any { prop ->
                                    val value = prop.get(location)
                                    value?.toString()?.contains(keyword, ignoreCase = true) == true
                                }
                            }
                            matches
                        }

                    _locationData.value = locations
                    _locationState.value = if (locations.isEmpty()) LocationState.EMPTY else LocationState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _locationState.value = LocationState.ERROR(error.message)
                }
            })
        }
    }
}