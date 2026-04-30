package com.spacehub.app.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.fragment.app.viewModels
import com.spacehub.app.data.model.Membership
import com.spacehub.app.data.network.RetrofitClient
import com.spacehub.app.data.repository.SpaceHubRepository
import com.spacehub.app.databinding.FragmentMembershipBinding
import com.spacehub.app.util.SessionManager
import kotlinx.coroutines.launch

class MembershipViewModel : ViewModel() {
    private val repository = SpaceHubRepository(RetrofitClient.apiService)

    private val _membership = MutableLiveData<UiState<List<Membership>>>()
    val membership: LiveData<UiState<List<Membership>>> = _membership

    private val _createMembership = MutableLiveData<UiState<Membership>>()
    val createMembership: LiveData<UiState<Membership>> = _createMembership

    private val _payMembership = MutableLiveData<UiState<String>>()
    val payMembership: LiveData<UiState<String>> = _payMembership

    fun loadMembership(userId: Int) {
        _membership.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getMembership(userId)
                if (resp.isSuccessful) _membership.value = UiState.Success(resp.body()?.memberships ?: emptyList())
                else _membership.value = UiState.Error("Failed")
            } catch (e: Exception) { _membership.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun createMembership(userId: Int, type: String, duration: Int) {
        _createMembership.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.createMembership(userId, type, duration)
                if (resp.isSuccessful) _createMembership.value = UiState.Success(resp.body()!!.membership)
                else _createMembership.value = UiState.Error("You may already have an active membership")
            } catch (e: Exception) { _createMembership.value = UiState.Error(e.message ?: "Error") }
        }
    }

    fun payMembership(membershipId: Int, userId: Int, method: String) {
        _payMembership.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.payMembership(membershipId, userId, method)
                if (resp.isSuccessful) _payMembership.value = UiState.Success("Membership activated!")
                else _payMembership.value = UiState.Error("Payment failed")
            } catch (e: Exception) { _payMembership.value = UiState.Error(e.message ?: "Error") }
        }
    }
}

class MembershipFragment : Fragment() {

    private var _binding: FragmentMembershipBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MembershipViewModel by viewModels()
    private lateinit var session: SessionManager
    private var pendingMembershipId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMembershipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        val user = session.getUser() ?: return

        binding.tvDayPrice.text = "Day: ฿15 / day"
        binding.tvMonthPrice.text = "Month: ฿299 / month"
        binding.tvYearPrice.text = "Year: ฿2,999 / year"

        viewModel.loadMembership(user.id)
        viewModel.membership.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                val active = state.data.firstOrNull { it.status == "active" }
                binding.tvCurrentStatus.text = if (active != null)
                    "Active: ${active.type.uppercase()} membership (expires ${active.endDate})"
                else "No active membership"
            }
        }

        binding.btnSubscribe.setOnClickListener {
            val type = when (binding.rgMembershipType.checkedRadioButtonId) {
                com.spacehub.app.R.id.rbMonth -> "month"
                com.spacehub.app.R.id.rbYear -> "year"
                else -> "day"
            }
            val duration = binding.etDuration.text.toString().toIntOrNull() ?: 1
            viewModel.createMembership(user.id, type, duration)
        }

        viewModel.createMembership.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    pendingMembershipId = state.data.id
                    binding.paymentLayout.visibility = View.VISIBLE
                    binding.tvPayAmount.text = "Amount to pay: ฿${state.data.pricePaid}"
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnPayMembership.setOnClickListener {
            val mid = pendingMembershipId ?: return@setOnClickListener
            val method = when (binding.rgPayMethod.checkedRadioButtonId) {
                com.spacehub.app.R.id.rbMBankTransfer -> "bank_transfer"
                com.spacehub.app.R.id.rbMTrueWallet -> "truewallet"
                else -> "credit_card"
            }
            viewModel.payMembership(mid, user.id, method)
        }

        viewModel.payMembership.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.paymentLayout.visibility = View.GONE
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_LONG).show()
                    viewModel.loadMembership(user.id)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
