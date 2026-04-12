package com.example.runna_runningtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        NavigationHelper.setupBottomNav(this, NavigationHelper.ActiveTab.HOME)

        val tvWelcomeHome = findViewById<TextView>(R.id.tvWelcomeHome)
        val btnStartRunning = findViewById<Button>(R.id.btnStartRunning)
        val cvChallenges = findViewById<androidx.cardview.widget.CardView>(R.id.cvChallenges)

        val tvRunStats1 = findViewById<TextView>(R.id.tvRunStats1)
        val tvRunStats2 = findViewById<TextView>(R.id.tvRunStats2)
        val tvRunStats3 = findViewById<TextView>(R.id.tvRunStats3)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPref.getString("name", "Runner")
        tvWelcomeHome.text = "Welcome back, $savedName!"

        btnStartRunning.setOnClickListener {
            val intent = Intent(this, StartRunningActivity::class.java)
            startActivity(intent)
        }

        cvChallenges?.setOnClickListener {
            startActivity(Intent(this, ChallengesActivity::class.java))
        }
    }
}
