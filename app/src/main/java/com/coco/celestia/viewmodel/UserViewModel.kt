package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UserState {
    object LOADING : UserState()
    object SUCCESS : UserState()
    object EMPTY : UserState()
    data class ERROR(val message: String) : UserState()
}

class UserViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _userData = MutableLiveData<UserData?>()
    private val _userState = MutableLiveData<UserState>()
    val userData: LiveData<UserData?> = _userData
    val userState: LiveData<UserState> = _userState

    /**
     * Fetches user data from the database based on the provided UID.
     */
    fun fetchUser(uid: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val snapshot = database.child(uid).get().await()
                if (snapshot.exists()) {
                    val userInfo = snapshot.getValue(UserData::class.java)
                    _userData.value = userInfo
                    _userState.value = UserState.SUCCESS
                } else {
                    _userData.value = null
                    _userState.value = UserState.EMPTY
                }
            } catch (e: Exception) {
                _userData.value = null
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Registers a new user with the provided email, first name, last name, and password.
     */
    fun register(email: String, firstname: String, lastname: String, password: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                user?.let {
                    val userData = UserData(email, firstname, lastname, password)
                    database.child(user.uid).setValue(userData).await()
                    _userState.value = UserState.SUCCESS
                } ?: run {
                    _userState.value = UserState.ERROR("Registration failed")
                }
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Logs in a user with the provided email and password.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                user?.let {
                    _userState.value = UserState.SUCCESS
                } ?: run {
                    _userState.value = UserState.ERROR("Login failed")
                }
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }
}