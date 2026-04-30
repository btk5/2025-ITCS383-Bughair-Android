package com.spacehub.app.ui.employee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.spacehub.app.data.model.Reservation
import com.spacehub.app.data.model.SupportTicket
import com.spacehub.app.databinding.ItemReservationBinding
import com.spacehub.app.databinding.ItemEmployeeTicketBinding

class ReservationsAdapter(
    private val items: List<Reservation>,
    private val onConfirm: (Reservation) -> Unit,
    private val onCheckIn: (Reservation) -> Unit
) : RecyclerView.Adapter<ReservationsAdapter.VH>() {

    inner class VH(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.binding.tvReservationDate.text = r.bookingDate
        holder.binding.tvReservationTime.text = "${r.startTime} - ${r.endTime}"
        holder.binding.tvReservationDesks.text = "${r.numDesks} desk(s)"
        holder.binding.tvReservationStatus.text = r.status.uppercase()
        holder.binding.btnConfirm.visibility = if (r.status == "pending") View.VISIBLE else View.GONE
        holder.binding.btnCheckIn.visibility = if (r.status == "confirmed") View.VISIBLE else View.GONE
        holder.binding.btnConfirm.setOnClickListener { onConfirm(r) }
        holder.binding.btnCheckIn.setOnClickListener { onCheckIn(r) }
    }
}

class SupportTicketsAdapter(
    private val tickets: List<SupportTicket>,
    private val onReply: (SupportTicket, String) -> Unit
) : RecyclerView.Adapter<SupportTicketsAdapter.VH>() {

    inner class VH(val binding: ItemEmployeeTicketBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemEmployeeTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = tickets[position]
        holder.binding.tvTicketCategory.text = t.category.uppercase()
        holder.binding.tvTicketMessage.text = t.message
        holder.binding.tvTicketStatus.text = t.status

        holder.binding.btnReply.setOnClickListener {
            val et = EditText(holder.itemView.context)
            et.hint = "Type your reply..."
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Reply to ticket")
                .setView(et)
                .setPositiveButton("Send") { _, _ ->
                    val reply = et.text.toString().trim()
                    if (reply.isNotEmpty()) onReply(t, reply)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
