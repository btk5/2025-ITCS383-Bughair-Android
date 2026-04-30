package com.spacehub.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.spacehub.app.data.model.LoginResponse
import com.spacehub.app.data.model.RegisterResponse
import com.spacehub.app.data.model.User
import com.spacehub.app.data.repository.SpaceHubRepository
import com.spacehub.app.ui.auth.AuthState
import com.spacehub.app.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockRepo: SpaceHubRepository

    private lateinit var viewModel: AuthViewModel

    private val fakeUser = User(1, "John", "Doe", "john@test.com", "0812345678", "Bangkok", "customer")

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel()
        // Inject mock repo via reflection for testing
        val field = AuthViewModel::class.java.getDeclaredField("repository")
        field.isAccessible = true
        field.set(viewModel, mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Login tests ───────────────────────────────────────────────────

    @Test
    fun `login with empty email shows error`() {
        viewModel.login("", "password123")
        val state = viewModel.authState.value
        assert(state is AuthState.Error)
        assert((state as AuthState.Error).message.contains("required"))
    }

    @Test
    fun `login with empty password shows error`() {
        viewModel.login("test@test.com", "")
        val state = viewModel.authState.value
        assert(state is AuthState.Error)
    }

    @Test
    fun `login with valid credentials succeeds`() = runTest {
        val response = Response.success(LoginResponse("Login successful!", fakeUser))
        whenever(mockRepo.login("john@test.com", "password123")).thenReturn(response)

        viewModel.login("john@test.com", "password123")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assert(state is AuthState.Success)
        assert((state as AuthState.Success).user.email == "john@test.com")
    }

    @Test
    fun `login with wrong credentials shows error`() = runTest {
        val errorBody = okhttp3.ResponseBody.create(null, "{\"error\":\"Invalid credentials\"}")
        val response = Response.error<LoginResponse>(401, errorBody)
        whenever(mockRepo.login(any(), any())).thenReturn(response)

        viewModel.login("wrong@test.com", "badpass")
        advanceUntilIdle()

        assert(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `login network failure shows error`() = runTest {
        whenever(mockRepo.login(any(), any())).thenThrow(RuntimeException("No internet"))

        viewModel.login("test@test.com", "pass")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assert(state is AuthState.Error)
        assert((state as AuthState.Error).message.contains("Connection"))
    }

    // ── Register tests ────────────────────────────────────────────────

    @Test
    fun `register with all empty fields shows error`() {
        viewModel.register("", "", "", "", "", "")
        assert(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `register with missing first name shows error`() {
        viewModel.register("", "Doe", "test@test.com", "081", "Bangkok", "pass")
        assert(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `register with valid data succeeds`() = runTest {
        val response = Response.success(RegisterResponse("Account created!"))
        whenever(mockRepo.register(any(), any(), any(), any(), any(), any())).thenReturn(response)

        viewModel.register("John", "Doe", "john@test.com", "0812345678", "Bangkok", "securePass123")
        advanceUntilIdle()

        assert(viewModel.authState.value is AuthState.RegisterSuccess)
    }

    @Test
    fun `register with duplicate email shows error`() = runTest {
        val errorBody = okhttp3.ResponseBody.create(null, "{\"error\":\"Email exists\"}")
        val response = Response.error<RegisterResponse>(409, errorBody)
        whenever(mockRepo.register(any(), any(), any(), any(), any(), any())).thenReturn(response)

        viewModel.register("John", "Doe", "dup@test.com", "081", "Bangkok", "pass")
        advanceUntilIdle()

        assert(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `register network failure shows error`() = runTest {
        whenever(mockRepo.register(any(), any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException("Timeout"))

        viewModel.register("A", "B", "c@d.com", "e", "f", "g")
        advanceUntilIdle()

        assert(viewModel.authState.value is AuthState.Error)
    }
}
