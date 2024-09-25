package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties

sealed class ContactState {
    object LOADING : ContactState()
    object SUCCESS : ContactState()
    object EMPTY : ContactState()
    data class ERROR(val message: String) : ContactState()
}

class ContactViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("contacts")
    private val _contactData = MutableLiveData<List<ContactData>>()
    private val _contactState = MutableLiveData<ContactState>()
    val contactData: LiveData<List<ContactData>> = _contactData
    val contactState: LiveData<ContactState> = _contactState

    fun fetchContacts(filter: String) {
        viewModelScope.launch {
            _contactState.value = ContactState.LOADING
            database.orderByChild("contactNumber").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }

                    val contacts = snapshot.children
                        .mapNotNull { it.getValue(ContactData::class.java) }
                        .filter { contact ->
                            val matches = filterKeywords.any { keyword ->
                                ContactData::class.memberProperties.any { prop ->
                                    val value = prop.get(contact)
                                    value?.toString()?.contains(keyword, ignoreCase = true) == true
                                }
                            }
                            matches
                        }

                    _contactData.value = contacts
                    _contactState.value = if (contacts.isEmpty()) ContactState.EMPTY else ContactState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _contactState.value = ContactState.ERROR(error.message)
                }
            })
        }
    }
}