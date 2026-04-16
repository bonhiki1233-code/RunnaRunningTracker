package com.example.runna_runningtracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runna_runningtracker.data.model.Run
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.RunsHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration

class SummaryActivity : AppCompatActivity() {
    private lateinit var mapSummary: MapView
    private var runId: String = "";
    private var distance: Double = 0.0;

    var calories: Int = 0;
    var startTime: Long = 0L;
    var endTime: Long = 0L
    var duration: Int = 0;

    var pace: Double = 0.0;

    var run_type: String = "";
    private val TAG: String = "SummaryActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences("osm_summary", MODE_PRIVATE))
        setContentView(R.layout.activity_summary)

        val tvDuration = findViewById<TextView>(R.id.tvSumDuration)
        val tvDistance = findViewById<TextView>(R.id.tvSumDistance)
        val tvPace = findViewById<TextView>(R.id.tvSumPace)
        val tvCalories = findViewById<TextView>(R.id.tvSumCal)
        val btnDone = findViewById<Button>(R.id.btnDone)
        mapSummary = findViewById(R.id.mapSummary)

        mapSummary.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapSummary.setMultiTouchControls(true)

        duration = intent.getIntExtra("duration", 0)
        distance = intent.getDoubleExtra("distance", 0.0)
        pace = intent.getDoubleExtra("pace", 0.0)
        runId = intent.getStringExtra("run_id") ?: ""

        calories = intent.getIntExtra("calories", 0)
        startTime = intent.getLongExtra("start_time", 0L)
        endTime = intent.getLongExtra("end_time", 0L)
        run_type = intent.getStringExtra("RUN_MODE") ?: ""

        val weight = UserPrefsManager.getUserWeightOrNull(this)
        val finalCalories = if (calories > 0) {
            calories
        } else {
            if (weight != null) (1.036 * weight * distance).toInt()
            else (distance * 60).toInt()
        }
        tvCalories.text = finalCalories.toString()

        val pathJson = intent.getStringExtra("path_data")

        val minutes = duration / 60
        val seconds = duration % 60

        tvDuration.text = String.format("%02d:%02d", minutes, seconds)
        tvDistance.text = String.format("%.2f", distance)
        tvCalories.text = calories.toString()

        val paceMin = pace.toInt()
        val paceSec = ((pace - paceMin) * 60).toInt()
        tvPace.text = String.format("%d:%02d", paceMin, paceSec)

        if (!pathJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<GeoPoint>>() {}.type
            val pathPoints : List<GeoPoint> = Gson().fromJson(pathJson, type)
            if (pathPoints.isNotEmpty()) {
                drawRoute(pathPoints)
            }
        }

        btnDone.setOnClickListener {

            // goi ham save lich su
            saveRun();
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()


        }

    }

    private fun drawRoute(pathPoints: List<GeoPoint>) {
        val polyline = Polyline().apply {
            setPoints(pathPoints)
            outlinePaint.color = Color.BLACK
            outlinePaint.strokeWidth = 12f
        }
        mapSummary.overlays.clear()
        mapSummary.overlays.add(polyline)

        mapSummary.post{
            if (pathPoints.isNotEmpty()) {
                mapSummary.zoomToBoundingBox(polyline.bounds, true, 100)
            }
            mapSummary.invalidate()
        }
    }

    override  fun onResume() {
        super.onResume()
        mapSummary.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapSummary.onPause()
    }

    fun saveRun() {
        val userid = AuthRepository().getCurrentUserId();
        if (userid != null) {
            Log.d(TAG, "lay thanh cong userid")
            val runData =
                Run(runId, userid, distance, duration, pace, calories, startTime, endTime, run_type)
            RunsHistoryRepository.save(runData) { bool ->
                if (bool) {
                    Log.d(TAG, "luu thanh cong lich su")
                } else {
                    Log.e(TAG, "khong luu duoc lich su chay")
                }
            }
        }
        Log.e(TAG, "co loi khi lay userid khong luu duoc lich su run")
    }

}