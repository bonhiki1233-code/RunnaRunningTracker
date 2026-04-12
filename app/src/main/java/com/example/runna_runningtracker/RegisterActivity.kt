package com.example.runna_runningtracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runna_runningtracker.data.repository.AuthRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    // Chỉ cho phép chữ + số, dài 7-10 ký tự, và bắt buộc có hoa/thường/số.
    private val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{7,10}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authRepository = AuthRepository()

        val emailInput = findViewById<EditText>(R.id.registerEmailInput)
        val passwordInput = findViewById<EditText>(R.id.registerPasswordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.registerConfirmPasswordInput)
        val passwordRequirements = findViewById<TextView>(R.id.tvPasswordRequirements)
        val createBtn = findViewById<Button>(R.id.createAccountButton)
        val backBtn = findViewById<TextView>(R.id.backToLoginButton)

        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            passwordRequirements.visibility = if (hasFocus || passwordInput.text.isNotBlank()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val valid = isValidPassword(s?.toString().orEmpty())
                passwordRequirements.setTextColor(
                    Color.parseColor(if (valid) "#2E7D32" else "#888888")
                )
            }
        })

        createBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirm = confirmPasswordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_register_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                passwordInput.error = getString(R.string.password_rule_error)
                Toast.makeText(this, getString(R.string.password_rule_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                confirmPasswordInput.error = getString(R.string.passwords_do_not_match)
                Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authRepository.register(
                email = email,
                password = password,
                onSuccess = { uid ->
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("SHOW_PERSONAL_INFO", true)
                    intent.putExtra("USER_EMAIL", email)
                    intent.putExtra("USER_UID", uid)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                },
                onFailure = { error ->
                    Toast.makeText(this, getString(R.string.register_failed, error), Toast.LENGTH_SHORT).show()
                }
            )
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun isValidPassword(password: String): Boolean {
        return passwordRegex.matches(password)
    }
}
