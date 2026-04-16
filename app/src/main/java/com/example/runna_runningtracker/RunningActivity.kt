package com.example.runna_runningtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.util.Locale
import org.osmdroid.views.overlay.Marker
import com.google.gson.Gson
import java.util.UUID

class RunningActivity : AppCompatActivity() {
    private lateinit var userMarker: Marker
    private lateinit var tvDistanceMain: TextView
    private lateinit var tvCaloriesMain: TextView
    private lateinit var tvPaceMain: TextView

    private lateinit var mapView: MapView
    private lateinit var polyline: Polyline

    private var runType : String ="";
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var tvDurationClock: TextView
    private lateinit var btnPause: Button

    private var seconds = 0
    private var running = true
    private val handler = Handler(Looper.getMainLooper())

    private var totalDistance = 0f
    private var startTime :Long =0L;
    private var lastLocation: Location? = null
    private var calories: Int =0;
    private var runId : String="";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("osm", MODE_PRIVATE))
        //set thoi gian cho start khi man hinh nay chay
        startTime = System.currentTimeMillis();
        //set id cho phien chay nay
        run_id= UUID.randomUUID().toString();

        //lay run type tu intent
        run_type =intent.getStringExtra("RUN_MODE")?:""


        //set id cho phien chay nay
        runId= UUID.randomUUID().toString();

        //lay run type tu intent
        runType =intent.getStringExtra("RUN_MODE")?:""


        setContentView(R.layout.activity_running)
        tvDistanceMain = findViewById(R.id.tvDistanceMain)
        tvCaloriesMain = findViewById(R.id.tvCaloriesMain)
        tvPaceMain = findViewById(R.id.tvPaceMain)

        mapView = findViewById(R.id.osmMap)
        mapView.setMultiTouchControls(true)

        polyline = Polyline()
        mapView.overlays.add(polyline)

        userMarker = Marker(mapView)
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(userMarker)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvDurationClock = findViewById(R.id.tvDurationClock)
        btnPause = findViewById(R.id.btnPause)

        val pausePanel = findViewById<LinearLayout>(R.id.pausePanel)
        val btnOverlayResume = findViewById<Button>(R.id.btnOverlayResume)
        val btnOverlayFinish = findViewById<Button>(R.id.btnOverlayFinish)

        runTimer()

        btnPause.setOnClickListener {
            running = false
            findViewById<TextView>(R.id.tvPauseTime).text = tvDurationClock.text
            pausePanel.visibility = View.VISIBLE
        }

        btnOverlayResume.setOnClickListener {
            pausePanel.visibility = View.GONE
            running = true
        }

        btnOverlayFinish.setOnClickListener {

            val distanceKm = totalDistance / 1000.0
            val pace = if (distanceKm > 0)
                (seconds / 60.0) / distanceKm
            else 0.0

            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtra("distance", distanceKm)
            intent.putExtra("duration", seconds)
            intent.putExtra("pace", pace)
            intent.putExtra("run_id",run_id)
            intent.putExtra("end_time", System.currentTimeMillis())
            intent.putExtra("start_time",startTime)
            intent.putExtra("calories",calories)
            intent.putExtra("RUN_MODE",run_type);
//            data class Run(
//                val run_id: String = "",
//                val user_id: String = "",
//                val distance: Double = 0.0,
//                val durationSeconds: Int = 0,
//                val pace: Double = 0.0,
//                val calories: Int = 0,
//                val start_time: Long = 0L,
//                val end_time : Long =0L,

//            )

            val pathPoints = polyline.actualPoints
            val pathJson = Gson().toJson(pathPoints)

            val intent = Intent(this, SummaryActivity::class.java).apply {
                putExtra("distance", distanceKm)
                putExtra("duration", seconds)
                putExtra("pace", if (distanceKm > 0) (seconds/60.0) / distanceKm else 0.0)
                putExtra("calories",finalCalories)
                putExtra("path_data", pathJson)
                putExtra("run_id", runId)
                putExtra("end_time", System.currentTimeMillis())
                putExtra("RUN_MODE", runType)
            }
        fusedLocationClient.removeLocationUpdates(locationCallback)
            startActivity(intent)
            finish()
        }

        startLocationUpdates()
    }

    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).build()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {

                val location = locationResult.lastLocation ?: return

                if (location.accuracy > 30) return

                val geoPoint = GeoPoint(location.latitude, location.longitude)
                userMarker.position = geoPoint
                userMarker.title = "Bạn đang ở đây"
                userMarker.icon = getDrawable(org.osmdroid.library.R.drawable.marker_default)

                // DISTANCE
                lastLocation?.let {
                    val distance = it.distanceTo(location)
                    if (distance in 2f..30f) {
                        totalDistance += distance
                    }
                }

                if (lastLocation == null) {
                    mapView.controller.setZoom(18.0)
                    mapView.controller.setCenter(geoPoint)
                } else {
                    mapView.controller.animateTo(geoPoint)
                }

                lastLocation = location

                polyline.addPoint(geoPoint)
                mapView.invalidate()

                val distanceKm = totalDistance / 1000.0
                tvDistanceMain.text = String.format("%.2f", distanceKm)

                 calories = (distanceKm * 60).toInt()
                tvCaloriesMain.text = calories.toString()

                if (distanceKm > 0.05) {
                    val pace = (seconds.toDouble() / 60) / distanceKm
                    val paceMin = pace.toInt()
                    val paceSec = ((pace - paceMin) * 60).toInt()

                    tvPaceMain.text =
                        String.format(Locale.getDefault(), "%d:%02d /km", paceMin, paceSec)
                }
            }

        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun runTimer() {

        handler.post(object : Runnable {

            override fun run() {

                val minutes = seconds / 60
                val secs = seconds % 60

                tvDurationClock.text =
                    String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)

                if (running) seconds++

                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }
}

