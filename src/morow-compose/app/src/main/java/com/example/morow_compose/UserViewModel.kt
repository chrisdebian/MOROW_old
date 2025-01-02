package com.example.morow_compose

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    var currentUser : String? = null
        set(newValue) {
            field = newValue
            currentUserLive.value = newValue
        }
    var currentUserLive = MutableLiveData<String?>(currentUser)
}