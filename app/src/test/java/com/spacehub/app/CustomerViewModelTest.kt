package com.spacehub.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.spacehub.app.data.model.*
import com.spacehub.app.data.repository.SpaceHubRepository
import com.spacehub.app.ui.customer.CustomerViewModel
import com.spacehub.app.ui.customer.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock lateinit var mockRepo: SpaceHubRepository

    private lateinit var viewModel: CustomerViewModel

    private val fakeBooking = Booking(
        id = 1, userId = 1, bookingDate = "2026-05-01",
        startTime = "08:00", endTime = "10:00", numDesks = 1,
        status = "pending", expiresAt = null, desks = listOf()
    )

    private val fakeMembership = Membership(
        id = 1, userId = 1, type = "month", duration = 1,
        pricePaid = 299.0, startDate = "2026-04-01", endDate = "2026-05-01", status = "active"
    )

    private val fakeSlot = TimeSlot("08:00 - 10:00", "08:00", "10:00", 50, 48, 2)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = CustomerViewModel()
        val field = CustomerViewModel::class.java.getDeclaredField("repository")
        field.isAccessible = true
        field.set(viewModel, mockRepo)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    // ── Membership ────────────────────────────────────────────────────

    @Test
    fun `loadMembership success returns list`() = runTest {
        whenever(mockRepo.getMembership(1))
            .thenReturn(Response.success(MembershipListResponse(listOf(fakeMembership))))

        viewModel.loadMembership(1)
        advanceUntilIdle()

        val state = viewModel.membership.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data.size == 1)
        assert(state.data[0].type == "month")
    }

    @Test
    fun `loadMembership error returns error state`() = runTest {
        val errBody = okhttp3.ResponseBody.create(null, "error")
        whenever(mockRepo.getMembership(any())).thenReturn(Response.error(500, errBody))

        viewModel.loadMembership(1)
        advanceUntilIdle()

        assert(viewModel.membership.value is UiState.Error)
    }

    @Test
    fun `loadMembership exception returns error`() = runTest {
        whenever(mockRepo.getMembership(any())).thenThrow(RuntimeException("Network error"))
        viewModel.loadMembership(1)
        advanceUntilIdle()
        assert(viewModel.membership.value is UiState.Error)
    }

    // ── Availability ──────────────────────────────────────────────────

    @Test
    fun `loadAvailability success returns slots`() = runTest {
        val resp = AvailabilityResponse("2026-05-01", listOf(fakeSlot))
        whenever(mockRepo.getAvailability("2026-05-01")).thenReturn(Response.success(resp))

        viewModel.loadAvailability("2026-05-01")
        advanceUntilIdle()

        val state = viewModel.availability.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data.slots.size == 1)
        assert(state.data.slots[0].availableDesks == 48)
    }

    @Test
    fun `loadAvailability error returns error state`() = runTest {
        val errBody = okhttp3.ResponseBody.create(null, "error")
        whenever(mockRepo.getAvailability(any())).thenReturn(Response.error(500, errBody))
        viewModel.loadAvailability("2026-05-01")
        advanceUntilIdle()
        assert(viewModel.availability.value is UiState.Error)
    }

    // ── Booking ───────────────────────────────────────────────────────

    @Test
    fun `createBooking success returns booking`() = runTest {
        whenever(mockRepo.createBooking(1, "2026-05-01", "08:00", "10:00", 1))
            .thenReturn(Response.success(BookingResponse("Created", fakeBooking)))

        viewModel.createBooking(1, "2026-05-01", "08:00", "10:00", 1)
        advanceUntilIdle()

        val state = viewModel.createBooking.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data.bookingDate == "2026-05-01")
    }

    @Test
    fun `createBooking without membership returns error`() = runTest {
        val errBody = okhttp3.ResponseBody.create(null, "{\"error\":\"No membership\"}")
        whenever(mockRepo.createBooking(any(), any(), any(), any(), any()))
            .thenReturn(Response.error(400, errBody))

        viewModel.createBooking(1, "2026-05-01", "08:00", "10:00", 1)
        advanceUntilIdle()

        assert(viewModel.createBooking.value is UiState.Error)
    }

    @Test
    fun `payBooking success returns success state`() = runTest {
        whenever(mockRepo.payBooking(1, 1, "credit_card"))
            .thenReturn(Response.success(PaymentResponse("Confirmed")))

        viewModel.payBooking(1, 1, "credit_card")
        advanceUntilIdle()

        val state = viewModel.payBooking.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data == "Payment confirmed!")
    }

    @Test
    fun `payBooking with expired booking returns error`() = runTest {
        val errBody = okhttp3.ResponseBody.create(null, "{\"error\":\"Expired\"}")
        whenever(mockRepo.payBooking(any(), any(), any())).thenReturn(Response.error(400, errBody))

        viewModel.payBooking(99, 1, "credit_card")
        advanceUntilIdle()

        assert(viewModel.payBooking.value is UiState.Error)
    }

    @Test
    fun `loadMyBookings returns list`() = runTest {
        whenever(mockRepo.getUserBookings(1))
            .thenReturn(Response.success(BookingListResponse(listOf(fakeBooking))))

        viewModel.loadMyBookings(1)
        advanceUntilIdle()

        val state = viewModel.myBookings.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data.size == 1)
    }

    @Test
    fun `loadMyBookings empty list returns empty success`() = runTest {
        whenever(mockRepo.getUserBookings(1))
            .thenReturn(Response.success(BookingListResponse(emptyList())))

        viewModel.loadMyBookings(1)
        advanceUntilIdle()

        val state = viewModel.myBookings.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data.isEmpty())
    }

    @Test
    fun `cancelBooking success`() = runTest {
        whenever(mockRepo.cancelBooking(1, 1))
            .thenReturn(Response.success(mapOf("message" to "Cancelled")))

        viewModel.cancelBooking(1, 1)
        advanceUntilIdle()

        assert(viewModel.cancelBooking.value is UiState.Success)
    }

    @Test
    fun `cancelBooking with 1-day restriction returns error`() = runTest {
        val errBody = okhttp3.ResponseBody.create(null, "{\"error\":\"Too late to cancel\"}")
        whenever(mockRepo.cancelBooking(any(), any())).thenReturn(Response.error(400, errBody))

        viewModel.cancelBooking(1, 1)
        advanceUntilIdle()

        assert(viewModel.cancelBooking.value is UiState.Error)
    }

    // ── Support ───────────────────────────────────────────────────────

    @Test
    fun `createTicket with empty message shows error`() {
        viewModel.createTicket(1, "booking", "")
        assert(viewModel.createTicket.value is UiState.Error)
    }

    @Test
    fun `createTicket with blank message shows error`() {
        viewModel.createTicket(1, "booking", "   ")
        assert(viewModel.createTicket.value is UiState.Error)
    }

    @Test
    fun `createTicket success`() = runTest {
        whenever(mockRepo.createTicket(1, "booking", "I have an issue"))
            .thenReturn(Response.success(TicketResponse("Ticket created!")))

        viewModel.createTicket(1, "booking", "I have an issue")
        advanceUntilIdle()

        val state = viewModel.createTicket.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data == "Request sent!")
    }

    @Test
    fun `loadMessages returns list`() = runTest {
        val fakeTicket = SupportTicket(1, 1, "booking", "Test msg", "pending", null, "2026-04-01")
        whenever(mockRepo.getUserMessages(1))
            .thenReturn(Response.success(MessagesResponse(listOf(fakeTicket))))

        viewModel.loadMessages(1)
        advanceUntilIdle()

        val state = viewModel.messages.value
        assert(state is UiState.Success)
        assert((state as UiState.Success).data[0].message == "Test msg")
    }

    @Test
    fun `loadMessages error returns error`() = runTest {
        val errBody = okhttp3.ResponseBody.create(null, "error")
        whenever(mockRepo.getUserMessages(any())).thenReturn(Response.error(500, errBody))
        viewModel.loadMessages(1)
        advanceUntilIdle()
        assert(viewModel.messages.value is UiState.Error)
    }

    @Test
    fun `createTicket network failure returns error`() = runTest {
        whenever(mockRepo.createTicket(any(), any(), any())).thenThrow(RuntimeException("Timeout"))
        viewModel.createTicket(1, "other", "Help!")
        advanceUntilIdle()
        assert(viewModel.createTicket.value is UiState.Error)
    }
}
