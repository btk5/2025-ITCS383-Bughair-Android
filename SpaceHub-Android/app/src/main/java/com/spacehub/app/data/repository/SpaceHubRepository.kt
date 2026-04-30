package com.spacehub.app.data.repository

import com.spacehub.app.data.model.*
import com.spacehub.app.data.network.ApiService
import retrofit2.Response

class SpaceHubRepository(private val api: ApiService) {

    // ── Auth ──────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Response<LoginResponse> =
        api.login(LoginRequest(email, password))

    suspend fun register(
        firstName: String, lastName: String, email: String,
        phone: String, address: String, password: String
    ): Response<RegisterResponse> =
        api.register(RegisterRequest(firstName, lastName, email, phone, address, password))

    // ── Membership ────────────────────────────────────────────────────
    suspend fun getMembership(userId: Int) = api.getMembership(userId)
    suspend fun createMembership(userId: Int, type: String, duration: Int) =
        api.createMembership(MembershipRequest(userId, type, duration))
    suspend fun payMembership(membershipId: Int, userId: Int, paymentMethod: String) =
        api.payMembership(membershipId, PaymentRequest(userId, paymentMethod))

    // ── Booking ───────────────────────────────────────────────────────
    suspend fun getTimeSlots() = api.getTimeSlots()
    suspend fun getAvailability(date: String) = api.getAvailability(date)
    suspend fun createBooking(userId: Int, date: String, startTime: String, endTime: String, numDesks: Int) =
        api.createBooking(BookingRequest(userId, date, startTime, endTime, numDesks))
    suspend fun payBooking(bookingId: Int, userId: Int, paymentMethod: String) =
        api.payBooking(bookingId, PaymentRequest(userId, paymentMethod))
    suspend fun getUserBookings(userId: Int) = api.getUserBookings(userId)
    suspend fun cancelBooking(bookingId: Int, userId: Int) =
        api.cancelBooking(bookingId, mapOf("userId" to userId))

    // ── Support ───────────────────────────────────────────────────────
    suspend fun createTicket(userId: Int, category: String, message: String) =
        api.createTicket(TicketRequest(userId, category, message))
    suspend fun getUserMessages(userId: Int) = api.getUserMessages(userId)
    suspend fun markTicketRead(ticketId: Int) = api.markTicketRead(ticketId)
    suspend fun deleteTicket(ticketId: Int) = api.deleteTicket(ticketId)

    // ── Notifications ─────────────────────────────────────────────────
    suspend fun getNotifications(userId: Int) = api.getNotifications(userId)

    // ── Employee ──────────────────────────────────────────────────────
    suspend fun getReservations(date: String? = null) = api.getReservations(date)
    suspend fun checkIn(bookingId: Int) = api.checkIn(mapOf("bookingId" to bookingId))
    suspend fun getEquipment() = api.getEquipment()
    suspend fun updateEquipment(equipmentId: Int, quantity: Int) =
        api.updateEquipment(equipmentId, UpdateEquipmentRequest(quantity))
    suspend fun addExpense(userId: Int, description: String, amount: Double) =
        api.addExpense(ExpenseRequest(userId, description, amount))
    suspend fun getExpenses() = api.getExpenses()
    suspend fun getEmployeeTickets() = api.getEmployeeTickets()
    suspend fun replyTicket(ticketId: Int, reply: String) =
        api.replyTicket(ticketId, mapOf("reply" to reply))
    suspend fun confirmBooking(bookingId: Int) = api.confirmBooking(bookingId)

    // ── Manager ───────────────────────────────────────────────────────
    suspend fun getRevenue() = api.getRevenue()
    suspend fun getReport() = api.getReport()
    suspend fun getSummary() = api.getSummary()
    suspend fun getEmployees() = api.getEmployees()
    suspend fun addEmployee(body: Map<String, String>) = api.addEmployee(body)
    suspend fun deleteEmployee(employeeId: Int) = api.deleteEmployee(employeeId)
}
