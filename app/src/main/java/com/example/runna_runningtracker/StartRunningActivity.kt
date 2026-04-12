package com.example.runna_runningtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class StartRunningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_running)

        NavigationHelper.setupBottomNav(this, NavigationHelper.ActiveTab.START)

        val btnFinalStartRun = findViewById<Button>(R.id.btnFinalStartRun)
        val cardEasyRun = findViewById<LinearLayout>(R.id.cardEasyRun)
        val cardLongRun = findViewById<LinearLayout>(R.id.cardLongRun)
        val cardInterval = findViewById<LinearLayout>(R.id.cardInterval)
        val cardWalking = findViewById<LinearLayout>(R.id.cardWalking)

        val cards = listOf(cardEasyRun, cardLongRun, cardInterval, cardWalking)
        val modes = listOf(getString(R.string.run_type_easy), "Long Run", "Interval", "Walking")
        var selectedMode = modes[0]

        fun updateSelection(selectedCard: LinearLayout, mode: String) {
            cards.forEach { it.setBackgroundResource(R.drawable.bg_card_subtle) }
            selectedCard.setBackgroundResource(R.drawable.bg_button_outline)
            selectedMode = mode
        }

        cardEasyRun.setOnClickListener { updateSelection(cardEasyRun, modes[0]) }
        cardLongRun.setOnClickListener { updateSelection(cardLongRun, modes[1]) }
        cardInterval.setOnClickListener { updateSelection(cardInterval, modes[2]) }
        cardWalking.setOnClickListener { updateSelection(cardWalking, modes[3]) }

        btnFinalStartRun.setOnClickListener {
            val intent = Intent(this, RunningActivity::class.java)
            intent.putExtra("RUN_MODE", selectedMode)
            startActivity(intent)
        }
    }
}
