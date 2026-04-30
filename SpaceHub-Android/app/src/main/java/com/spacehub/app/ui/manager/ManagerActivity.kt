package com.spacehub.app.ui.manager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spacehub.app.data.network.RetrofitClient
import com.spacehub.app.data.repository.SpaceHubRepository
import com.spacehub.app.databinding.ActivityManagerBinding
import com.spacehub.app.ui.auth.LoginActivity
import com.spacehub.app.ui.customer.UiState
import com.spacehub.app.util.SessionManager
import kotlinx.coroutines.launch

class ManagerViewModel : ViewModel() {
    private val repository = SpaceHubRepository(RetrofitClient.apiService)

    val summary = MutableLiveData<UiState<Map<String, Any>>>()
    val employees = MutableLiveData<UiState<List<com.spacehub.app.data.model.Employee>>>()
    val actionResult = MutableLiveData<UiState<String>>()

    fun loadSummary() {
        summary.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getSummary()
                if (resp.isSuccessful) summary.value = UiState.Success(resp.body() ?: emptyMap())
                else summary.value = UiState.Error("Failed")
            } catch (e: Exception) { summary.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun loadEmployees() {
        employees.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getEmployees()
                if (resp.isSuccessful) employees.value = UiState.Success(resp.body()?.employees ?: emptyList())
                else employees.value = UiState.Error("Failed")
            } catch (e: Exception) { employees.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun deleteEmployee(employeeId: Int) {
        viewModelScope.launch {
            try {
                val resp = repository.deleteEmployee(employeeId)
                if (resp.isSuccessful) { actionResult.value = UiState.Success("Employee removed"); loadEmployees() }
                else actionResult.value = UiState.Error("Failed to remove")
            } catch (e: Exception) { actionResult.value = UiState.Error(e.message ?: "Error") }
        }
    }
}

class ManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerBinding
    private val viewModel: ManagerViewModel by viewModels()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        val user = session.getUser()
        binding.tvWelcome.text = "Manager: ${user?.fullName}"

        viewModel.loadSummary()
        viewModel.loadEmployees()

        viewModel.summary.observe(this) { state ->
            if (state is UiState.Success) {
                val d = state.data
                binding.tvTotalReservations.text = "Reservations: ${d["totalReservations"]}"
                binding.tvTotalIncome.text = "Income: ฿${d["totalIncome"]}"
                binding.tvNetIncome.text = "Net: ฿${d["netIncome"]}"
                binding.tvMemberCount.text = "Members: ${d["memberCount"]}"
            }
        }

        viewModel.employees.observe(this) { state ->
            if (state is UiState.Success) {
                binding.tvEmployeeCount.text = "Employees: ${state.data.size}"
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
            viewModel.loadSummary()
            viewModel.loadEmployees()
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
