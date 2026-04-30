package com.spacehub.app

import android.content.Context
import android.content.SharedPreferences
import com.spacehub.app.data.model.User
import com.spacehub.app.util.SessionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class SessionManagerTest {

    @Mock lateinit var mockContext: Context
    @Mock lateinit var mockPrefs: SharedPreferences
    @Mock lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var sessionManager: SessionManager

    private val fakeUser = User(42, "Jane", "Smith", "jane@test.com", "0891234567", "Chiang Mai", "customer")

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockPrefs)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putInt(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.clear()).thenReturn(mockEditor)
        sessionManager = SessionManager(mockContext)
    }

    @Test
    fun `isLoggedIn returns false when no session`() {
        whenever(mockPrefs.getBoolean(eq("is_logged_in"), eq(false))).thenReturn(false)
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns true after saveUser`() {
        whenever(mockPrefs.getBoolean(eq("is_logged_in"), eq(false))).thenReturn(true)
        sessionManager.saveUser(fakeUser)
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun `getUser returns null when not logged in`() {
        whenever(mockPrefs.getBoolean(eq("is_logged_in"), eq(false))).thenReturn(false)
        assertNull(sessionManager.getUser())
    }

    @Test
    fun `getUser returns correct user when logged in`() {
        whenever(mockPrefs.getBoolean(eq("is_logged_in"), eq(false))).thenReturn(true)
        whenever(mockPrefs.getInt(eq("user_id"), eq(-1))).thenReturn(42)
        whenever(mockPrefs.getString(eq("first_name"), any())).thenReturn("Jane")
        whenever(mockPrefs.getString(eq("last_name"), any())).thenReturn("Smith")
        whenever(mockPrefs.getString(eq("email"), any())).thenReturn("jane@test.com")
        whenever(mockPrefs.getString(eq("phone"), any())).thenReturn("0891234567")
        whenever(mockPrefs.getString(eq("address"), any())).thenReturn("Chiang Mai")
        whenever(mockPrefs.getString(eq("role"), any())).thenReturn("customer")

        val user = sessionManager.getUser()
        assertNotNull(user)
        assertEquals(42, user!!.id)
        assertEquals("Jane", user.firstName)
        assertEquals("jane@test.com", user.email)
        assertEquals("customer", user.role)
    }

    @Test
    fun `getUserId returns -1 when no session`() {
        whenever(mockPrefs.getInt(eq("user_id"), eq(-1))).thenReturn(-1)
        assertEquals(-1, sessionManager.getUserId())
    }

    @Test
    fun `getUserId returns saved id`() {
        whenever(mockPrefs.getInt(eq("user_id"), eq(-1))).thenReturn(42)
        assertEquals(42, sessionManager.getUserId())
    }

    @Test
    fun `getRole defaults to customer`() {
        whenever(mockPrefs.getString(eq("role"), any())).thenReturn("customer")
        assertEquals("customer", sessionManager.getRole())
    }

    @Test
    fun `getRole returns employee role`() {
        whenever(mockPrefs.getString(eq("role"), any())).thenReturn("employee")
        assertEquals("employee", sessionManager.getRole())
    }

    @Test
    fun `getRole returns manager role`() {
        whenever(mockPrefs.getString(eq("role"), any())).thenReturn("manager")
        assertEquals("manager", sessionManager.getRole())
    }

    @Test
    fun `logout clears shared preferences`() {
        sessionManager.logout()
        whenever(mockPrefs.getBoolean(eq("is_logged_in"), eq(false))).thenReturn(false)
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `user fullName is concatenated correctly`() {
        assertEquals("Jane Smith", fakeUser.fullName)
    }

    @Test
    fun `user fullName with single char names`() {
        val user = User(1, "A", "B", "ab@test.com", "0", "x", "customer")
        assertEquals("A B", user.fullName)
    }
}
