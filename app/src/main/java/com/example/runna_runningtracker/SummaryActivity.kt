package com.example.runna_runningtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runna_runningtracker.data.model.Run
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.RunsHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import com.example.runna_runningtracker.data.model.CompletedRun
import com.google.firebase.Timestamp
import java.util.Locale

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val tvDuration = findViewById<TextView>(R.id.tvSumDuration)
        val tvDistance = findViewById<TextView>(R.id.tvSumDistance)
        val tvPace = findViewById<TextView>(R.id.tvSumPace)
        val tvCalories = findViewById<TextView>(R.id.tvSumCal)
        val btnDone = findViewById<Button>(R.id.btnDone)

        val duration = intent.getIntExtra("duration", 0)
        val distance = intent.getDoubleExtra("distance", 0.0)
        val pace = intent.getDoubleExtra("pace", 0.0)
        val calories = intent.getIntExtra("calories", 0)
        val runId = intent.getStringExtra("run_id").orEmpty().ifBlank {
            "run_${System.currentTimeMillis()}"
        }
        val endedAtMillis = intent.getLongExtra("ended_at", System.currentTimeMillis())

        val minutes = duration / 60
        val seconds = duration % 60

        tvDuration.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        tvDistance.text = String.format(Locale.getDefault(), "%.2f", distance)

        val paceMin = pace.toInt()
        val paceSec = ((pace - paceMin) * 60).toInt()
        tvPace.text = String.format(Locale.getDefault(), "%d:%02d", paceMin, paceSec)
        tvCalories.text = calories.toString()

        val currentUserId = AuthSessionHelper.getCurrentUserId(this)
        if (!currentUserId.isNullOrBlank()) {
            val completedRun = CompletedRun(
                runId = runId,
                userId = currentUserId,
                distanceMeters = (distance * 1000.0).toLong(),
                durationSeconds = duration.toLong(),
                endedAt = Timestamp(java.util.Date(endedAtMillis))
            )
            ChallengeProgressService().applyCompletedRunToChallenges(completedRun) { }
        }

        btnDone.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

    }
    fun saveRun()
    {
        val userid = AuthRepository().getCurrentUserId();
        if(userid!=null)
        {
            Log.d(TAG,"lay thanh cong userid")
            val runData = Run(
                run_id,
                userid,
                distance,
                duration,
                pace,
                calories,
                startTime,
                endTime,
                run_type
            )
            RunsHistoryRepository.save(runData,){
                    bool ->
                if(bool)
                {
                    Log.d(TAG,"luu thanh cong lich su")
                }
                else{
                    Log.e(TAG,"khong luu duoc lich su chay")
                }
            }
        } else {
          Log.e(TAG,"co loi khi lay userid khong luu duoc lich su run")
        }  
    }
}
