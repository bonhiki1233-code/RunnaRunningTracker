package com.example.runna_runningtracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import com.example.runna_runningtracker.data.model.CompletedRun
import com.google.firebase.Timestamp
import java.util.Locale
//import com.example.runna_runningtracker.data.model.Run
//import com.example.runna_runningtracker.data.repository.AuthRepository
//import com.example.runna_runningtracker.data.repository.RunsHistoryRepository

class SummaryActivity : AppCompatActivity() {
    private  var runId: String ="";
    private var distance: Double =0.0;

<<<<<<< master
    private lateinit var mapSummary: MapView

=======
    var startTime: Long =0L;
    var endTime: Long =0L


    var run_type :String ="";
    private val TAG:String ="SummaryActivity"
>>>>>>> master
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("osm_summary", MODE_PRIVATE))

        setContentView(R.layout.activity_summary)

        val tvDuration = findViewById<TextView>(R.id.tvSumDuration)
        val tvDistance = findViewById<TextView>(R.id.tvSumDistance)
        val tvPace = findViewById<TextView>(R.id.tvSumPace)
        val btnDone = findViewById<Button>(R.id.btnDone)
        mapSummary = findViewById<MapView>(R.id.mapSummary)
        mapSummary.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapSummary.setMultiTouchControls(true)

<<<<<<< master
        val duration = intent.getIntExtra("duration",0)
        val distance = intent.getDoubleExtra("distance",0.0)
        val pace = intent.getDoubleExtra("pace",0.0)
        val calories = intent.getIntExtra("calories", 0)
        val pathJson = intent.getStringExtra("path_data")
=======
        runId = intent.getStringExtra("run_id") ?: ""
>>>>>>> master

         startTime = intent.getLongExtra("start_time", 0L)
         endTime = intent.getLongExtra("end_time", 0L)
        run_type =intent.getStringExtra("RUN_MODE")?:""
        val minutes = duration / 60
        val seconds = duration % 60

        tvDuration.text = String.format("%02d:%02d", minutes, seconds)
        tvDistance.text = String.format("%.2f", distance)

        val paceMin = pace.toInt()
        val paceSec = ((pace - paceMin) * 60).toInt()

        tvPace.text = String.format("%d:%02d", paceMin, paceSec)
        tvCalories.text = calories.toString()

        if (!pathJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<GeoPoint>>() {}.type
            val pathPoints: List<GeoPoint> = Gson().fromJson(pathJson, type)

            if (pathPoints.isNotEmpty()) {
                setupMap(pathPoints)
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
    fun saveRun()
    {
        val userid = AuthRepository().getCurrentUserId();
        if(userid!=null)
        {
            Log.d(TAG,"lay thanh cong userid")
            val runData = Run(runId,userid,distance, duration,pace,calories,startTime,endTime,run_type)
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
        }
        Log.e(TAG,"co loi khi lay userid khong luu duoc lich su run")
    }

    private fun setupMap(points: List<GeoPoint>) {

        mapSummary.setUseDataConnection(false)
        mapSummary.setMultiTouchControls(true)

//        mapSummary.overlayManager.tilesOverlay.setColorFilter(
//            android.graphics.PorterDuffColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_OVER)
//        )
//
//        mapSummary.setBackgroundColor(Color.WHITE)
        //mapSummary.controller.setZoom(16.0)

        val summaryPolyline = Polyline().apply {
            setPoints(points)
            outlinePaint.color = Color.BLACK
            outlinePaint.strokeWidth = 12f
        }

        //xóa hình cũ thêm mới
        mapSummary.overlays.clear()
        mapSummary.overlays.add(summaryPolyline)

        //Sử dụng post đảm bảo MapView đã đưuojc layout mới tính toán vị trí
        mapSummary.post {
            if (points.isNotEmpty()) {
                //lấy giới hạn lộ trình
                val boundingBox = summaryPolyline.bounds

                //zoom
                mapSummary.zoomToBoundingBox(boundingBox, true, 100)

                //set tâm vào điểm đầu tiên, dành cho lộ trình quá ngắn sợ sai
                if (mapSummary.zoomLevelDouble < 2.0) {
                    mapSummary.controller.setZoom(17.0)
                    mapSummary.controller.setCenter(points[0])

                }
            }
            //ép bản đồ vẽ lại
            mapSummary.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        mapSummary.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapSummary.onPause()
    }
}
