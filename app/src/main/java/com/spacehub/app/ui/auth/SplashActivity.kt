package com.spacehub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spacehub.app.ui.customer.MainActivity
import com.spacehub.app.ui.employee.EmployeeActivity
import com.spacehub.app.ui.manager.ManagerActivity
import com.spacehub.app.util.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(this)
        if (session.isLoggedIn()) {
            val user = session.getUser()
            val dest = when (user?.role) {
                "employee" -> EmployeeActivity::class.java
                "manager" -> ManagerActivity::class.java
                else -> MainActivity::class.java
            }
            startActivity(Intent(this, dest))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
