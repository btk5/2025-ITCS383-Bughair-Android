package com.spacehub.app.ui.customer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.spacehub.app.R
import com.spacehub.app.data.network.RetrofitClient
import com.spacehub.app.data.repository.SpaceHubRepository
import com.spacehub.app.databinding.ActivityMainBinding
import com.spacehub.app.ui.auth.LoginActivity
import com.spacehub.app.util.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private val repository = SpaceHubRepository(RetrofitClient.apiService)
    private val notifHandler = Handler(Looper.getMainLooper())
    private val notifRunnable = object : Runnable {
        override fun run() {
            pollNotifications()
            notifHandler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        notifHandler.post(notifRunnable)
    }

    override fun onPause() {
        super.onPause()
        notifHandler.removeCallbacks(notifRunnable)
    }

    private fun pollNotifications() {
        val userId = session.getUserId()
        if (userId == -1) return
        lifecycleScope.launch {
            try {
                val resp = repository.getNotifications(userId)
                if (resp.isSuccessful) {
                    val notif = resp.body() ?: return@launch
                    val totalBadge = notif.unreadMessages + notif.newlyConfirmedBookings
                    val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_support)
                    if (totalBadge > 0) {
                        badge.isVisible = true
                        badge.number = totalBadge
                    } else {
                        binding.bottomNavigation.removeBadge(R.id.nav_support)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun logout() {
        notifHandler.removeCallbacks(notifRunnable)
        session.logout()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
