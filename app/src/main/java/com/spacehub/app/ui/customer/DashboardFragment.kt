package com.spacehub.app.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.spacehub.app.databinding.FragmentDashboardBinding
import com.spacehub.app.util.SessionManager

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerViewModel by viewModels()
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        val user = session.getUser() ?: return

        binding.tvWelcome.text = "Welcome, ${user.firstName}!"
        binding.tvEmail.text = user.email
        binding.tvRole.text = user.role.uppercase()

        viewModel.loadMembership(user.id)
        viewModel.loadMyBookings(user.id)

        viewModel.membership.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val active = state.data.firstOrNull { it.status == "active" }
                    if (active != null) {
                        binding.tvMembershipStatus.text = "✓ Active ${active.type.uppercase()} membership"
                        binding.tvMembershipExpiry.text = "Expires: ${active.endDate}"
                        binding.cardMembership.setCardBackgroundColor(
                            resources.getColor(android.R.color.holo_green_light, null)
                        )
                    } else {
                        binding.tvMembershipStatus.text = "No active membership"
                        binding.tvMembershipExpiry.text = "Subscribe to book desks"
                    }
                }
                is UiState.Error -> binding.tvMembershipStatus.text = "Failed to load"
                else -> {}
            }
        }

        viewModel.myBookings.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val upcoming = state.data.filter { it.status in listOf("pending", "confirmed") }
                    binding.tvUpcomingCount.text = "${upcoming.size} upcoming booking(s)"
                }
                else -> {}
            }
        }

        binding.btnBook.setOnClickListener {
            (activity as? MainActivity)?.let {
                it.binding.bottomNavigation.selectedItemId = com.spacehub.app.R.id.nav_book
            }
        }

        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
