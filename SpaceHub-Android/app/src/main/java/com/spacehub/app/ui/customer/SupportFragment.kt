package com.spacehub.app.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.spacehub.app.data.model.SupportTicket
import com.spacehub.app.databinding.FragmentSupportBinding
import com.spacehub.app.databinding.ItemTicketBinding
import androidx.recyclerview.widget.RecyclerView
import com.spacehub.app.util.SessionManager

class SupportFragment : Fragment() {

    private var _binding: FragmentSupportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerViewModel by viewModels()
    private lateinit var session: SessionManager

    private val categories = listOf("booking", "payment", "membership", "other")
    private val categoryLabels = listOf(
        "Booking Confirmation Error",
        "Payment & Billing Issue",
        "Membership Access",
        "General Inquiry"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        val user = session.getUser() ?: return

        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryLabels
        )

        // Quick reply buttons (CR-06: Perfective)
        val quickReplies = listOf("I have a booking issue", "Payment not confirmed", "How to cancel?", "Membership expired")
        binding.tvQuickReply1.text = quickReplies[0]
        binding.tvQuickReply2.text = quickReplies[1]
        binding.tvQuickReply3.text = quickReplies[2]
        binding.tvQuickReply4.text = quickReplies[3]

        val setQuick = { msg: String -> binding.etMessage.setText(msg) }
        binding.tvQuickReply1.setOnClickListener { setQuick(quickReplies[0]) }
        binding.tvQuickReply2.setOnClickListener { setQuick(quickReplies[1]) }
        binding.tvQuickReply3.setOnClickListener { setQuick(quickReplies[2]) }
        binding.tvQuickReply4.setOnClickListener { setQuick(quickReplies[3]) }

        binding.btnSendTicket.setOnClickListener {
            val idx = binding.spinnerCategory.selectedItemPosition
            val category = categories[idx]
            val message = binding.etMessage.text.toString().trim()
            viewModel.createTicket(user.id, category, message)
        }

        viewModel.createTicket.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSendTicket.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSendTicket.isEnabled = true
                    binding.etMessage.setText("")
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_LONG).show()
                    viewModel.loadMessages(user.id)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSendTicket.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Inbox tab
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
        viewModel.loadMessages(user.id)

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadMessages(user.id) }

        viewModel.messages.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is UiState.Success -> {
                    val tickets = state.data
                    if (tickets.isEmpty()) {
                        binding.tvNoMessages.visibility = View.VISIBLE
                        binding.recyclerMessages.visibility = View.GONE
                    } else {
                        binding.tvNoMessages.visibility = View.GONE
                        binding.recyclerMessages.visibility = View.VISIBLE
                        binding.recyclerMessages.adapter = TicketsAdapter(tickets) { ticket ->
                            if (ticket.status == "replied") viewModel.markRead(ticket.id)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TicketsAdapter(
    private val tickets: List<SupportTicket>,
    private val onRead: (SupportTicket) -> Unit
) : RecyclerView.Adapter<TicketsAdapter.VH>() {

    inner class VH(val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = tickets[position]
        holder.binding.tvCategory.text = t.category.uppercase()
        holder.binding.tvMessage.text = t.message
        holder.binding.tvStatus.text = t.status.uppercase()
        if (t.adminReply != null) {
            holder.binding.tvAdminReply.visibility = View.VISIBLE
            holder.binding.tvAdminReply.text = "Support: ${t.adminReply}"
            if (t.status == "replied") onRead(t)
        } else {
            holder.binding.tvAdminReply.visibility = View.GONE
        }
        val isUnread = t.status == "replied"
        holder.itemView.alpha = if (isUnread) 1f else 0.7f
    }
}
