package com.spacehub.app.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spacehub.app.databinding.FragmentProfileBinding
import com.spacehub.app.util.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())
        val user = session.getUser() ?: return

        val initials = "${user.firstName.firstOrNull() ?: ""}${user.lastName.firstOrNull() ?: ""}"
        binding.tvInitials.text = initials.uppercase()
        binding.tvFullName.text = user.fullName
        binding.tvEmail.text = user.email
        binding.tvPhone.text = user.phone
        binding.tvAddress.text = user.address
        binding.tvRole.text = "Role: ${user.role.uppercase()}"

        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
