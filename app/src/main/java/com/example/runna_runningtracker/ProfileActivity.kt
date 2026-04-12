package com.example.runna_runningtracker

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. Ánh xạ các View từ file XML
        val txtName = findViewById<TextView>(R.id.profileName)
        val txtEmail = findViewById<TextView>(R.id.profileEmail)
        val txtAge = findViewById<TextView>(R.id.profileAge)
        val txtHeight = findViewById<TextView>(R.id.profileHeight)
        val txtWeight = findViewById<TextView>(R.id.profileWeight)
        val btnLogout = findViewById<View>(R.id.logoutButton)

        // 2. Lấy dữ liệu thật từ SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPref.getString("name", "Người dùng")
        val savedEmail = sharedPref.getString("email", "Chưa có email")
        val savedAge = sharedPref.getString("age", "0")
        val savedHeight = sharedPref.getString("height", "0")
        val savedWeight = sharedPref.getString("weight", "0")

        // 3. Đổ dữ liệu vào View (Xóa sạch bóng dáng John Doe)
        txtName.text = savedName
        txtEmail.text = savedEmail
        txtAge.text = "$savedAge years"
        txtHeight.text = "$savedHeight cm"
        txtWeight.text = "$savedWeight kg"

        // 4. Xử lý nút Logout
        btnLogout?.setOnClickListener {
            finish() // Thoát màn hình Profile quay lại màn hình trước
        }
    }
}