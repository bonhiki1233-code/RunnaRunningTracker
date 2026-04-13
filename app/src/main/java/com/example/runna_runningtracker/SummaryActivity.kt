package com.example.runna_runningtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val tvDuration = findViewById<TextView>(R.id.tvSumDuration)
        val tvDistance = findViewById<TextView>(R.id.tvSumDistance)
        val tvPace = findViewById<TextView>(R.id.tvSumPace)
        val btnDone = findViewById<Button>(R.id.btnDone)

        val duration = intent.getIntExtra("duration",0)
        val distance = intent.getDoubleExtra("distance",0.0)
        val pace = intent.getDoubleExtra("pace",0.0)

        val minutes = duration / 60
        val seconds = duration % 60

        tvDuration.text = String.format("%02d:%02d", minutes, seconds)
        tvDistance.text = String.format("%.2f", distance)

        val paceMin = pace.toInt()
        val paceSec = ((pace - paceMin) * 60).toInt()

        tvPace.text = String.format("%d:%02d", paceMin, paceSec)

        btnDone.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}