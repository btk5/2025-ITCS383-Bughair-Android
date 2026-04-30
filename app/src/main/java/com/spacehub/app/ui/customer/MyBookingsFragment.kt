package com.spacehub.app.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.spacehub.app.data.model.Booking
import com.spacehub.app.databinding.FragmentMyBookingsBinding
import com.spacehub.app.databinding.ItemBookingBinding
import androidx.recyclerview.widget.RecyclerView
import com.spacehub.app.util.SessionManager

class MyBookingsFragment : Fragment() {

    private var _binding: FragmentMyBookingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerViewModel by viewModels()
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val user = session.getUser() ?: return
        viewModel.loadMyBookings(user.id)

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadMyBookings(user.id) }

        viewModel.myBookings.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.data.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.recyclerView.adapter = BookingsAdapter(state.data) { booking ->
                            confirmCancel(booking, user.id)
                        }
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.cancelBooking.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    viewModel.loadMyBookings(user.id)
                }
                is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun confirmCancel(booking: Booking, userId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Cancel booking on ${booking.bookingDate} (${booking.startTime}-${booking.endTime})?")
            .setPositiveButton("Cancel Booking") { _, _ -> viewModel.cancelBooking(booking.id, userId) }
            .setNegativeButton("Keep", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class BookingsAdapter(
    private val bookings: List<Booking>,
    private val onCancel: (Booking) -> Unit
) : RecyclerView.Adapter<BookingsAdapter.VH>() {

    inner class VH(val binding: ItemBookingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = bookings.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = bookings[position]
        holder.binding.tvDate.text = b.bookingDate
        holder.binding.tvTime.text = "${b.startTime} - ${b.endTime}"
        holder.binding.tvDesks.text = "${b.numDesks} desk(s)"
        holder.binding.tvStatus.text = b.status.uppercase()
        val canCancel = b.status in listOf("pending", "confirmed")
        holder.binding.btnCancel.visibility = if (canCancel) View.VISIBLE else View.GONE
        holder.binding.btnCancel.setOnClickListener { onCancel(b) }
        val color = when (b.status) {
            "confirmed", "checked_in" -> android.R.color.holo_green_light
            "cancelled", "expired" -> android.R.color.holo_red_light
            else -> android.R.color.holo_orange_light
        }
        holder.binding.tvStatus.setTextColor(
            holder.itemView.resources.getColor(color, null)
        )
    }
}
