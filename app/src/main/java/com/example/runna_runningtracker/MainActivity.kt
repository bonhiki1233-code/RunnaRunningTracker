package com.example.runna_runningtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MainActivity chỉ còn giữ vai trò tương thích ngược cho flow cũ.
        // App hiện tại mở bằng LoginActivity, nên nếu có chỗ nào còn gọi
        // MainActivity thì mình chuyển thẳng sang LoginActivity để tránh
        // crash do đống view/id cũ không còn khớp layout nữa.
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
