package com.example.runna_runningtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvWelcomeHome = findViewById<TextView>(R.id.tvWelcomeHome)
        val btnStartRunning = findViewById<Button>(R.id.btnStartRunning)

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
    }
}