package com.example.runna_runningtracker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class StartRunningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Phải khớp với tên file XML của bạn
        setContentView(R.layout.activity_start_running)

        // 2. Ánh xạ các View từ XML
        val cardEasyRun = findViewById<CardView>(R.id.cardEasyRun)
        val cardLongRun = findViewById<CardView>(R.id.cardLongRun)
        val cardIntervalRun = findViewById<CardView>(R.id.cardIntervalRun)
        val cardWalking = findViewById<CardView>(R.id.cardWalking)
        val btnStartRun = findViewById<CardView>(R.id.btnStartRun)

        val txtGpsStatus = findViewById<TextView>(R.id.txtGpsStatus)
        val txtLastRun = findViewById<TextView>(R.id.txtLastRun)
        val txtWeeklyGoal = findViewById<TextView>(R.id.txtWeeklyGoal)

        // 3. Thiết lập sự kiện click
        cardEasyRun.setOnClickListener {
            showToast("Bạn đã chọn: Easy Run")
        }

        cardLongRun.setOnClickListener {
            showToast("Bạn đã chọn: Long Run")
        }

        cardIntervalRun.setOnClickListener {
            showToast("Bạn đã chọn: Interval Run")
        }

        cardWalking.setOnClickListener {
            showToast("Bạn đã chọn: Walking")
        }

        btnStartRun.setOnClickListener {
            showToast("Bắt đầu chạy thôi! Giao diện GPS đang khởi động...")
            // Chèn code chuyển màn hình hoặc logic chạy ở đây
        }
    }

    // Hàm phụ để hiển thị thông báo nhanh
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}