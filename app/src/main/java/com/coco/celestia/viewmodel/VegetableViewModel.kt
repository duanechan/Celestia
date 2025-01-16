package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.VegetableData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

sealed class VegState {
    data object LOADING: VegState()
    data object SUCCESS: VegState()
    data object EMPTY: VegState()
    data class ERROR(val message: String): VegState()
}

class VegetableViewModel : ViewModel() {
    private val database : DatabaseReference = FirebaseDatabase.getInstance().getReference("vegetables")
    private val _vegData = MutableLiveData<List<VegetableData>>()
    private val _vegState = MutableLiveData<VegState>()
    val vegData: LiveData<List<VegetableData>> = _vegData
    val vegState: LiveData<VegState> = _vegState

    fun fetchVegetables() {
        viewModelScope.launch {
            _vegState.value = VegState.LOADING
            database.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val vegList = mutableListOf<VegetableData>()
                    for (childSnapshot in dataSnapshot.children) {
                        val vegetable = childSnapshot.getValue(VegetableData::class.java)
                        if (vegetable != null) {
                            vegList.add(vegetable)
                        }
                    }

                    _vegData.value = vegList
                    _vegState.value = VegState.SUCCESS
                } else {
                    _vegState.value = VegState.EMPTY
                }
            }.addOnFailureListener { exception ->
                _vegState.value = VegState.ERROR("${exception.message}")
            }
        }
    }
}