package com.spacehub.app

import com.spacehub.app.data.model.*
import com.spacehub.app.data.network.ApiService
import com.spacehub.app.data.repository.SpaceHubRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SpaceHubRepositoryTest {

    @Mock lateinit var mockApi: ApiService
    private lateinit var repository: SpaceHubRepository

    private val fakeUser = User(1, "Test", "User", "test@test.com", "081", "Bangkok", "customer")
    private val fakeBooking = Booking(1, 1, "2026-05-01", "08:00", "10:00", 1, "pending", null)
    private val fakeMembership = Membership(1, 1, "month", 1, 299.0, "2026-04-01", "2026-05-01", "active")
    private val fakeSlot = TimeSlot("08:00 - 10:00", "08:00", "10:00", 50, 48, 2)
    private val fakeTicket = SupportTicket(1, 1, "booking", "Help!", "pending", null, "2026-04-01")

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = SpaceHubRepository(mockApi)
    }

    // ── Auth ──────────────────────────────────────────────────────────

    @Test
    fun `login calls api login`() = runTest {
        val resp = Response.success(LoginResponse("OK", fakeUser))
        whenever(mockApi.login(LoginRequest("test@test.com", "pass"))).thenReturn(resp)
        val result = repository.login("test@test.com", "pass")
        assertTrue(result.isSuccessful)
        assertEquals("test@test.com", result.body()?.user?.email)
    }

    @Test
    fun `register calls api register`() = runTest {
        val resp = Response.success(RegisterResponse("Created"))
        whenever(mockApi.register(any())).thenReturn(resp)
        val result = repository.register("A", "B", "c@d.com", "e", "f", "g")
        assertTrue(result.isSuccessful)
    }

    // ── Membership ────────────────────────────────────────────────────

    @Test
    fun `getMembership returns memberships`() = runTest {
        whenever(mockApi.getMembership(1))
            .thenReturn(Response.success(MembershipListResponse(listOf(fakeMembership))))
        val result = repository.getMembership(1)
        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.memberships?.size)
    }

    @Test
    fun `createMembership sends correct request`() = runTest {
        val mem = fakeMembership.copy(status = "pending_payment")
        whenever(mockApi.createMembership(MembershipRequest(1, "month", 1)))
            .thenReturn(Response.success(MembershipResponse("Created", mem)))
        val result = repository.createMembership(1, "month", 1)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `payMembership sends correct request`() = runTest {
        whenever(mockApi.payMembership(1, PaymentRequest(1, "credit_card")))
            .thenReturn(Response.success(PaymentResponse("Paid")))
        val result = repository.payMembership(1, 1, "credit_card")
        assertTrue(result.isSuccessful)
    }

    // ── Booking ───────────────────────────────────────────────────────

    @Test
    fun `getTimeSlots returns slots`() = runTest {
        whenever(mockApi.getTimeSlots()).thenReturn(Response.success(listOf(fakeSlot)))
        val result = repository.getTimeSlots()
        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.size)
    }

    @Test
    fun `getAvailability calls api with date`() = runTest {
        val avail = AvailabilityResponse("2026-05-01", listOf(fakeSlot))
        whenever(mockApi.getAvailability("2026-05-01")).thenReturn(Response.success(avail))
        val result = repository.getAvailability("2026-05-01")
        assertTrue(result.isSuccessful)
        assertEquals("2026-05-01", result.body()?.date)
    }

    @Test
    fun `createBooking sends correct data`() = runTest {
        val req = BookingRequest(1, "2026-05-01", "08:00", "10:00", 1)
        whenever(mockApi.createBooking(req))
            .thenReturn(Response.success(BookingResponse("Created", fakeBooking)))
        val result = repository.createBooking(1, "2026-05-01", "08:00", "10:00", 1)
        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.booking?.id)
    }

    @Test
    fun `getUserBookings returns list`() = runTest {
        whenever(mockApi.getUserBookings(1))
            .thenReturn(Response.success(BookingListResponse(listOf(fakeBooking))))
        val result = repository.getUserBookings(1)
        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.bookings?.size)
    }

    @Test
    fun `cancelBooking sends userId in body`() = runTest {
        whenever(mockApi.cancelBooking(1, mapOf("userId" to 1)))
            .thenReturn(Response.success(mapOf("message" to "Cancelled")))
        val result = repository.cancelBooking(1, 1)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `payBooking sends correct payment method`() = runTest {
        whenever(mockApi.payBooking(1, PaymentRequest(1, "truewallet")))
            .thenReturn(Response.success(PaymentResponse("Paid")))
        val result = repository.payBooking(1, 1, "truewallet")
        assertTrue(result.isSuccessful)
    }

    // ── Support ───────────────────────────────────────────────────────

    @Test
    fun `createTicket sends correct data`() = runTest {
        val req = TicketRequest(1, "booking", "Need help")
        whenever(mockApi.createTicket(req)).thenReturn(Response.success(TicketResponse("OK")))
        val result = repository.createTicket(1, "booking", "Need help")
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `getUserMessages returns tickets`() = runTest {
        whenever(mockApi.getUserMessages(1))
            .thenReturn(Response.success(MessagesResponse(listOf(fakeTicket))))
        val result = repository.getUserMessages(1)
        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.messages?.size)
    }

    @Test
    fun `markTicketRead calls api`() = runTest {
        whenever(mockApi.markTicketRead(1))
            .thenReturn(Response.success(mapOf("success" to true)))
        val result = repository.markTicketRead(1)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `deleteTicket calls api with id`() = runTest {
        whenever(mockApi.deleteTicket(1))
            .thenReturn(Response.success(mapOf("success" to true)))
        val result = repository.deleteTicket(1)
        assertTrue(result.isSuccessful)
    }

    // ── Notifications ─────────────────────────────────────────────────

    @Test
    fun `getNotifications returns notification data`() = runTest {
        val notif = Notifications(unreadMessages = 2, pendingActionBookings = 0, newlyConfirmedBookings = 1)
        whenever(mockApi.getNotifications(1)).thenReturn(Response.success(notif))
        val result = repository.getNotifications(1)
        assertTrue(result.isSuccessful)
        assertEquals(2, result.body()?.unreadMessages)
        assertEquals(1, result.body()?.newlyConfirmedBookings)
    }

    // ── Employee ──────────────────────────────────────────────────────

    @Test
    fun `getReservations returns list`() = runTest {
        val res = com.spacehub.app.data.model.Reservation(1, 1, "2026-05-01", "08:00", "10:00", 1, "pending")
        whenever(mockApi.getReservations(null))
            .thenReturn(Response.success(ReservationsResponse(listOf(res))))
        val result = repository.getReservations()
        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.reservations?.size)
    }

    @Test
    fun `getEmployeeTickets returns pending tickets`() = runTest {
        whenever(mockApi.getEmployeeTickets())
            .thenReturn(Response.success(EmployeeTicketsResponse(listOf(fakeTicket))))
        val result = repository.getEmployeeTickets()
        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.tickets?.size)
    }

    @Test
    fun `replyTicket sends reply body`() = runTest {
        whenever(mockApi.replyTicket(1, mapOf("reply" to "We will fix it")))
            .thenReturn(Response.success(mapOf("message" to "Sent")))
        val result = repository.replyTicket(1, "We will fix it")
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `confirmBooking calls api`() = runTest {
        whenever(mockApi.confirmBooking(1))
            .thenReturn(Response.success(mapOf("success" to "true")))
        val result = repository.confirmBooking(1)
        assertTrue(result.isSuccessful)
    }
}
