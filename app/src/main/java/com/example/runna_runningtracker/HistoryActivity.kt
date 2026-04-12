package com.example.runna_runningtracker

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history) // Tên file XML của bạn

        // Ánh xạ nút sắp xếp (a-z) nếu bạn muốn thêm tính năng click
        val imgSort = findViewById<ImageView>(R.id.imgSort)
        // Lưu ý: Bạn cần thêm android:id="@+id/imgSort" vào ImageView trong XML của bạn

        imgSort?.setOnClickListener {
            Toast.makeText(this, "Tính năng sắp xếp đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }
}