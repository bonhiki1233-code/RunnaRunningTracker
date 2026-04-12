package com.example.runna_runningtracker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class RunningActivity : AppCompatActivity() {

    // Các biến cho bộ đếm thời gian
    private lateinit var tvDurationClock: TextView
    private lateinit var btnPause: Button

    private var seconds = 0
    private var running = true // Bật để tự chạy khi mở màn hình
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        // Ánh xạ View
        tvDurationClock = findViewById(R.id.tvDurationClock)
        btnPause = findViewById(R.id.btnPause)

        // Bắt đầu chạy bộ đếm thời gian
        runTimer()

        // Xử lý sự kiện nút Pause
        btnPause.setOnClickListener {
            if (running) {
                running = false
                btnPause.text = "Resume"
                btnPause.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0)
            } else {
                running = true
                btnPause.text = "Pause"
                btnPause.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0)
            }
        }
    }

    // Logic bộ đếm thời gian (00:00)
    private fun runTimer() {
        handler.post(object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60

                // Định dạng hiển thị MM:SS (hoặc HH:MM:SS nếu > 1 tiếng)
                val time = if (hours > 0) {
                    String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                } else {
                    String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
                }

                tvDurationClock.text = time

                if (running) {
                    seconds++
                }

                // Lặp lại sau mỗi 1 giây
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dừng handler khi đóng activity để tránh leak memory
        handler.removeCallbacksAndMessages(null)
    }
}