package com.spacehub.app.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.spacehub.app.data.model.TimeSlot
import com.spacehub.app.databinding.FragmentBookingBinding
import com.spacehub.app.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerViewModel by viewModels()
    private lateinit var session: SessionManager

    private var selectedDate = ""
    private var selectedSlot: TimeSlot? = null
    private var currentBookingId: Int? = null

    // Step: 1=date, 2=slot, 3=payment
    private var step = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        showStep(1)

        // Step 1: Date picker
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())
        binding.datePicker.minDate = System.currentTimeMillis() - 1000

        binding.btnCheckAvailability.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.set(binding.datePicker.year, binding.datePicker.month, binding.datePicker.dayOfMonth)
            selectedDate = sdf.format(cal.time)
            viewModel.loadAvailability(selectedDate)
        }

        // Step 2: Slot selection
        viewModel.availability.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val slots = state.data.slots
                    val labels = slots.map { "${it.label} (${it.availableDesks} desks available)" }
                    binding.spinnerSlots.adapter = ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, labels
                    )
                    showStep(2)
                    binding.tvSelectedDate.text = "Date: $selectedDate"

                    binding.btnConfirmSlot.setOnClickListener {
                        val idx = binding.spinnerSlots.selectedItemPosition
                        selectedSlot = slots[idx]
                        val numDesks = binding.etNumDesks.text.toString().toIntOrNull() ?: 1
                        val user = session.getUser() ?: return@setOnClickListener
                        viewModel.createBooking(
                            user.id, selectedDate,
                            selectedSlot!!.startTime, selectedSlot!!.endTime, numDesks
                        )
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Step 3: Payment
        viewModel.createBooking.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    currentBookingId = state.data.id
                    val desks = state.data.desks?.joinToString(", ") { it.label } ?: ""
                    binding.tvBookingSummary.text =
                        "Date: $selectedDate\nTime: ${selectedSlot?.label}\nDesks: $desks"
                    showStep(3)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnPay.setOnClickListener {
            val bookingId = currentBookingId ?: return@setOnClickListener
            val user = session.getUser() ?: return@setOnClickListener
            val method = when (binding.rgPayment.checkedRadioButtonId) {
                com.spacehub.app.R.id.rbBankTransfer -> "bank_transfer"
                com.spacehub.app.R.id.rbTrueWallet -> "truewallet"
                else -> "credit_card"
            }
            viewModel.payBooking(bookingId, user.id, method)
        }

        viewModel.payBooking.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Booking confirmed!", Toast.LENGTH_LONG).show()
                    showStep(1)
                    currentBookingId = null
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showStep(s: Int) {
        step = s
        binding.step1Layout.visibility = if (s == 1) View.VISIBLE else View.GONE
        binding.step2Layout.visibility = if (s == 2) View.VISIBLE else View.GONE
        binding.step3Layout.visibility = if (s == 3) View.VISIBLE else View.GONE
        binding.tvStepIndicator.text = "Step $s of 3"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
