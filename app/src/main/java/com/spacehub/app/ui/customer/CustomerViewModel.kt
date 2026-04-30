package com.spacehub.app.ui.customer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spacehub.app.data.model.*
import com.spacehub.app.data.network.RetrofitClient
import com.spacehub.app.data.repository.SpaceHubRepository
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class CustomerViewModel : ViewModel() {

    val repository = SpaceHubRepository(RetrofitClient.apiService)

    // ── Membership ────────────────────────────────────────────────────
    private val _membership = MutableLiveData<UiState<List<Membership>>>()
    val membership: LiveData<UiState<List<Membership>>> = _membership

    fun loadMembership(userId: Int) {
        _membership.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getMembership(userId)
                if (resp.isSuccessful) _membership.value = UiState.Success(resp.body()?.memberships ?: emptyList())
                else _membership.value = UiState.Error("Failed to load membership")
            } catch (e: Exception) { _membership.value = UiState.Error(e.message ?: "Error") }
        }
    }

    // ── Booking ───────────────────────────────────────────────────────
    private val _availability = MutableLiveData<UiState<AvailabilityResponse>>()
    val availability: LiveData<UiState<AvailabilityResponse>> = _availability

    fun loadAvailability(date: String) {
        _availability.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getAvailability(date)
                if (resp.isSuccessful) _availability.value = UiState.Success(resp.body()!!)
                else _availability.value = UiState.Error("Failed to load slots")
            } catch (e: Exception) { _availability.value = UiState.Error(e.message ?: "Error") }
        }
    }

    private val _createBooking = MutableLiveData<UiState<Booking>>()
    val createBooking: LiveData<UiState<Booking>> = _createBooking

    fun createBooking(userId: Int, date: String, startTime: String, endTime: String, numDesks: Int) {
        _createBooking.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.createBooking(userId, date, startTime, endTime, numDesks)
                if (resp.isSuccessful) _createBooking.value = UiState.Success(resp.body()!!.booking)
                else _createBooking.value = UiState.Error("Booking failed. Active membership required.")
            } catch (e: Exception) { _createBooking.value = UiState.Error(e.message ?: "Error") }
        }
    }

    private val _payBooking = MutableLiveData<UiState<String>>()
    val payBooking: LiveData<UiState<String>> = _payBooking

    fun payBooking(bookingId: Int, userId: Int, paymentMethod: String) {
        _payBooking.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.payBooking(bookingId, userId, paymentMethod)
                if (resp.isSuccessful) _payBooking.value = UiState.Success("Payment confirmed!")
                else _payBooking.value = UiState.Error("Payment failed")
            } catch (e: Exception) { _payBooking.value = UiState.Error(e.message ?: "Error") }
        }
    }

    private val _myBookings = MutableLiveData<UiState<List<Booking>>>()
    val myBookings: LiveData<UiState<List<Booking>>> = _myBookings

    fun loadMyBookings(userId: Int) {
        _myBookings.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getUserBookings(userId)
                if (resp.isSuccessful) _myBookings.value = UiState.Success(resp.body()?.bookings ?: emptyList())
                else _myBookings.value = UiState.Error("Failed to load bookings")
            } catch (e: Exception) { _myBookings.value = UiState.Error(e.message ?: "Error") }
        }
    }

    private val _cancelBooking = MutableLiveData<UiState<String>>()
    val cancelBooking: LiveData<UiState<String>> = _cancelBooking

    fun cancelBooking(bookingId: Int, userId: Int) {
        _cancelBooking.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.cancelBooking(bookingId, userId)
                if (resp.isSuccessful) _cancelBooking.value = UiState.Success("Booking cancelled")
                else _cancelBooking.value = UiState.Error("Cannot cancel this booking")
            } catch (e: Exception) { _cancelBooking.value = UiState.Error(e.message ?: "Error") }
        }
    }

    // ── Support ───────────────────────────────────────────────────────
    private val _createTicket = MutableLiveData<UiState<String>>()
    val createTicket: LiveData<UiState<String>> = _createTicket

    fun createTicket(userId: Int, category: String, message: String) {
        if (message.isBlank()) {
            _createTicket.value = UiState.Error("Message cannot be empty")
            return
        }
        _createTicket.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.createTicket(userId, category, message)
                if (resp.isSuccessful) _createTicket.value = UiState.Success("Request sent!")
                else _createTicket.value = UiState.Error("Failed to send request")
            } catch (e: Exception) { _createTicket.value = UiState.Error(e.message ?: "Error") }
        }
    }

    private val _messages = MutableLiveData<UiState<List<SupportTicket>>>()
    val messages: LiveData<UiState<List<SupportTicket>>> = _messages

    fun loadMessages(userId: Int) {
        _messages.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getUserMessages(userId)
                if (resp.isSuccessful) _messages.value = UiState.Success(resp.body()?.messages ?: emptyList())
                else _messages.value = UiState.Error("Failed to load messages")
            } catch (e: Exception) { _messages.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun markRead(ticketId: Int) {
        viewModelScope.launch {
            try { repository.markTicketRead(ticketId) } catch (_: Exception) {}
        }
    }
}
