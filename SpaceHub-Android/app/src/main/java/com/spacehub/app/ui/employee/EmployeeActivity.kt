package com.spacehub.app.ui.employee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.spacehub.app.data.network.RetrofitClient
import com.spacehub.app.data.repository.SpaceHubRepository
import com.spacehub.app.databinding.ActivityEmployeeBinding
import com.spacehub.app.ui.auth.LoginActivity
import com.spacehub.app.ui.customer.UiState
import com.spacehub.app.util.SessionManager
import kotlinx.coroutines.launch

class EmployeeViewModel : ViewModel() {
    val repository = SpaceHubRepository(RetrofitClient.apiService)

    val reservations = MutableLiveData<UiState<List<com.spacehub.app.data.model.Reservation>>>()
    val tickets = MutableLiveData<UiState<List<com.spacehub.app.data.model.SupportTicket>>>()
    val equipment = MutableLiveData<UiState<List<com.spacehub.app.data.model.Equipment>>>()
    val actionResult = MutableLiveData<UiState<String>>()

    fun loadReservations(date: String? = null) {
        reservations.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getReservations(date)
                if (resp.isSuccessful) reservations.value = UiState.Success(resp.body()?.reservations ?: emptyList())
                else reservations.value = UiState.Error("Failed to load")
            } catch (e: Exception) { reservations.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun checkIn(bookingId: Int) {
        viewModelScope.launch {
            try {
                val resp = repository.checkIn(bookingId)
                if (resp.isSuccessful) actionResult.value = UiState.Success("Checked in!")
                else actionResult.value = UiState.Error("Check-in failed")
            } catch (e: Exception) { actionResult.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun confirmBooking(bookingId: Int) {
        viewModelScope.launch {
            try {
                val resp = repository.confirmBooking(bookingId)
                if (resp.isSuccessful) { actionResult.value = UiState.Success("Booking confirmed!"); loadReservations() }
                else actionResult.value = UiState.Error("Failed to confirm")
            } catch (e: Exception) { actionResult.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun loadTickets() {
        tickets.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getEmployeeTickets()
                if (resp.isSuccessful) tickets.value = UiState.Success(resp.body()?.tickets ?: emptyList())
                else tickets.value = UiState.Error("Failed")
            } catch (e: Exception) { tickets.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun replyTicket(ticketId: Int, reply: String) {
        viewModelScope.launch {
            try {
                val resp = repository.replyTicket(ticketId, reply)
                if (resp.isSuccessful) { actionResult.value = UiState.Success("Reply sent!"); loadTickets() }
                else actionResult.value = UiState.Error("Failed to reply")
            } catch (e: Exception) { actionResult.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun loadEquipment() {
        equipment.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getEquipment()
                if (resp.isSuccessful) equipment.value = UiState.Success(resp.body()?.equipment ?: emptyList())
                else equipment.value = UiState.Error("Failed")
            } catch (e: Exception) { equipment.value = UiState.Error(e.message ?: "Error") }
        }
    }
}

class EmployeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeBinding
    private val viewModel: EmployeeViewModel by viewModels()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        val user = session.getUser()
        binding.tvWelcome.text = "Employee: ${user?.fullName}"

        binding.recyclerReservations.layoutManager = LinearLayoutManager(this)
        binding.recyclerTickets.layoutManager = LinearLayoutManager(this)

        viewModel.loadReservations()
        viewModel.loadTickets()

        viewModel.reservations.observe(this) { state ->
            if (state is UiState.Success) {
                binding.recyclerReservations.adapter = com.spacehub.app.ui.employee.ReservationsAdapter(
                    state.data,
                    onConfirm = { viewModel.confirmBooking(it.id) },
                    onCheckIn = { viewModel.checkIn(it.id) }
                )
            }
        }

        viewModel.tickets.observe(this) { state ->
            if (state is UiState.Success) {
                binding.recyclerTickets.adapter = com.spacehub.app.ui.employee.SupportTicketsAdapter(state.data) { ticket, reply ->
                    viewModel.replyTicket(ticket.id, reply)
                }
            }
        }

        viewModel.actionResult.observe(this) { state ->
            when (state) {
                is UiState.Success -> Toast.makeText(this, state.data, Toast.LENGTH_SHORT).show()
                is UiState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadReservations()
            viewModel.loadTickets()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}
