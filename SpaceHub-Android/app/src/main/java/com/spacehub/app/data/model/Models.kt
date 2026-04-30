package com.spacehub.app.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: String,
    val role: String
) {
    val fullName get() = "$firstName $lastName"
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val message: String, val user: User)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String
)
data class RegisterResponse(val message: String)

// ── Membership ────────────────────────────────────────────────────────
data class Membership(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val type: String,
    val duration: Int,
    @SerializedName("price_paid") val pricePaid: Double,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val status: String
)

data class MembershipRequest(val userId: Int, val type: String, val duration: Int)
data class MembershipResponse(val message: String, val membership: Membership)
data class MembershipListResponse(val memberships: List<Membership>)

// ── Booking ───────────────────────────────────────────────────────────
data class TimeSlot(
    val label: String,
    val startTime: String,
    val endTime: String,
    val totalDesks: Int = 50,
    val availableDesks: Int = 50,
    val bookedDesks: Int = 0
)

data class AvailabilityResponse(val date: String, val slots: List<TimeSlot>)

data class BookingRequest(
    val userId: Int,
    val date: String,
    val startTime: String,
    val endTime: String,
    val numDesks: Int
)

data class Booking(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("num_desks") val numDesks: Int,
    val status: String,
    @SerializedName("expires_at") val expiresAt: String?,
    val desks: List<DeskLabel>? = null
)

data class DeskLabel(val id: Int, val label: String)

data class BookingResponse(val message: String, val booking: Booking)
data class BookingListResponse(val bookings: List<Booking>)

// ── Payment ───────────────────────────────────────────────────────────
data class PaymentRequest(val userId: Int, val paymentMethod: String)
data class PaymentResponse(val message: String)

// ── Support ───────────────────────────────────────────────────────────
data class SupportTicket(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val category: String,
    val message: String,
    val status: String,
    @SerializedName("admin_reply") val adminReply: String?,
    @SerializedName("created_at") val createdAt: String
)

data class TicketRequest(val userId: Int, val category: String, val message: String)
data class TicketResponse(val message: String)
data class MessagesResponse(val messages: List<SupportTicket>)
data class EmployeeTicketsResponse(val tickets: List<SupportTicket>)

// ── Notifications ─────────────────────────────────────────────────────
data class Notifications(
    val unreadMessages: Int,
    val pendingActionBookings: Int,
    val newlyConfirmedBookings: Int
)

// ── Employee ──────────────────────────────────────────────────────────
data class Reservation(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("num_desks") val numDesks: Int,
    val status: String
)

data class ReservationsResponse(val reservations: List<Reservation>)

data class Equipment(
    val id: Int,
    val name: String,
    val quantity: Int,
    val unit: String
)

data class EquipmentResponse(val equipment: List<Equipment>)
data class UpdateEquipmentRequest(val quantity: Int)

data class Expense(
    val id: Int,
    val description: String,
    val amount: Double,
    @SerializedName("created_at") val createdAt: String
)

data class ExpenseRequest(val userId: Int, val description: String, val amount: Double)
data class ExpensesResponse(val expenses: List<Expense>)

// ── Manager ───────────────────────────────────────────────────────────
data class RevenueSummary(
    val totalRevenue: Double,
    val totalExpenses: Double,
    val netIncome: Double
)

data class Summary(
    val totalReservations: Int,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netIncome: Double,
    val memberCount: Int
)

data class Employee(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)

data class EmployeesResponse(val employees: List<Employee>)
