package com.spacehub.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spacehub.app.data.model.User
import com.spacehub.app.data.network.RetrofitClient
import com.spacehub.app.data.repository.SpaceHubRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class RegisterSuccess(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repository = SpaceHubRepository(RetrofitClient.apiService)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) _authState.value = AuthState.Success(user)
                    else _authState.value = AuthState.Error("Login failed")
                } else {
                    _authState.value = AuthState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Connection error: ${e.message}")
            }
        }
    }

    fun register(
        firstName: String, lastName: String, email: String,
        phone: String, address: String, password: String
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() ||
            phone.isBlank() || address.isBlank() || password.isBlank()
        ) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repository.register(firstName, lastName, email, phone, address, password)
                if (response.isSuccessful) {
                    _authState.value = AuthState.RegisterSuccess("Account created! Please log in.")
                } else {
                    _authState.value = AuthState.Error("Email already in use")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Connection error: ${e.message}")
            }
        }
    }
}
