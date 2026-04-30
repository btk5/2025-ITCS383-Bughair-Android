package com.spacehub.app.data.network

import com.spacehub.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // ── Membership ────────────────────────────────────────────────────
    @POST("api/membership")
    suspend fun createMembership(@Body request: MembershipRequest): Response<MembershipResponse>

    @POST("api/membership/{membershipId}/pay")
    suspend fun payMembership(
        @Path("membershipId") membershipId: Int,
        @Body request: PaymentRequest
    ): Response<PaymentResponse>

    @GET("api/membership/{userId}")
    suspend fun getMembership(@Path("userId") userId: Int): Response<MembershipListResponse>

    @GET("api/pricing")
    suspend fun getPricing(): Response<Map<String, Double>>

    // ── Booking ───────────────────────────────────────────────────────
    @GET("api/timeslots")
    suspend fun getTimeSlots(): Response<List<TimeSlot>>

    @GET("api/bookings/availability")
    suspend fun getAvailability(@Query("date") date: String): Response<AvailabilityResponse>

    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>

    @POST("api/bookings/{bookingId}/pay")
    suspend fun payBooking(
        @Path("bookingId") bookingId: Int,
        @Body request: PaymentRequest
    ): Response<PaymentResponse>

    @GET("api/bookings/user/{userId}")
    suspend fun getUserBookings(@Path("userId") userId: Int): Response<BookingListResponse>

    @POST("api/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: Int,
        @Body body: Map<String, Int>
    ): Response<Map<String, String>>

    // ── Support ───────────────────────────────────────────────────────
    @POST("api/support/tickets")
    suspend fun createTicket(@Body request: TicketRequest): Response<TicketResponse>

    @GET("api/user/messages")
    suspend fun getUserMessages(@Query("userId") userId: Int): Response<MessagesResponse>

    @POST("api/support/tickets/{id}/read")
    suspend fun markTicketRead(@Path("id") ticketId: Int): Response<Map<String, Boolean>>

    @DELETE("api/support/tickets/{id}")
    suspend fun deleteTicket(@Path("id") ticketId: Int): Response<Map<String, Boolean>>

    // ── Notifications ─────────────────────────────────────────────────
    @GET("api/user/notifications")
    suspend fun getNotifications(@Query("userId") userId: Int): Response<Notifications>

    // ── Employee ──────────────────────────────────────────────────────
    @GET("api/employee/reservations")
    suspend fun getReservations(@Query("date") date: String? = null): Response<ReservationsResponse>

    @POST("api/employee/checkin")
    suspend fun checkIn(@Body body: Map<String, Int>): Response<Map<String, String>>

    @GET("api/employee/equipment")
    suspend fun getEquipment(): Response<EquipmentResponse>

    @PUT("api/employee/equipment/{equipmentId}")
    suspend fun updateEquipment(
        @Path("equipmentId") equipmentId: Int,
        @Body request: UpdateEquipmentRequest
    ): Response<Map<String, String>>

    @POST("api/employee/expenses")
    suspend fun addExpense(@Body request: ExpenseRequest): Response<Map<String, String>>

    @GET("api/employee/expenses")
    suspend fun getExpenses(): Response<ExpensesResponse>

    @GET("api/employee/tickets")
    suspend fun getEmployeeTickets(): Response<EmployeeTicketsResponse>

    @POST("api/support/tickets/{id}/reply")
    suspend fun replyTicket(
        @Path("id") ticketId: Int,
        @Body body: Map<String, String>
    ): Response<Map<String, String>>

    @POST("api/employee/bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") bookingId: Int): Response<Map<String, String>>

    // ── Manager ───────────────────────────────────────────────────────
    @GET("api/manager/revenue")
    suspend fun getRevenue(): Response<Map<String, Any>>

    @GET("api/manager/report")
    suspend fun getReport(): Response<Map<String, Any>>

    @GET("api/manager/summary")
    suspend fun getSummary(): Response<Map<String, Any>>

    @GET("api/manager/employees")
    suspend fun getEmployees(): Response<EmployeesResponse>

    @POST("api/manager/employees")
    suspend fun addEmployee(@Body body: Map<String, String>): Response<Map<String, String>>

    @DELETE("api/manager/employees/{employeeId}")
    suspend fun deleteEmployee(@Path("employeeId") employeeId: Int): Response<Map<String, String>>
}
