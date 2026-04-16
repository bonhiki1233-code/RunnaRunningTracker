package com.example.runna_runningtracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runna_runningtracker.data.model.Run
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.RunsHistoryRepository

class SummaryActivity : AppCompatActivity() {
    private  var runId: String ="";
    private var distance: Double =0.0;

    var calories :Int =0;
    var startTime: Long =0L;
    var endTime: Long =0L
    var duration: Int =0;

    var pace : Double =0.0;

    var run_type :String ="";
    private val TAG:String ="SummaryActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val tvDuration = findViewById<TextView>(R.id.tvSumDuration)
        val tvDistance = findViewById<TextView>(R.id.tvSumDistance)
        val tvPace = findViewById<TextView>(R.id.tvSumPace)
        val btnDone = findViewById<Button>(R.id.btnDone)

         duration = intent.getIntExtra("duration",0)
         distance = intent.getDoubleExtra("distance",0.0)
         pace = intent.getDoubleExtra("pace",0.0)
        runId = intent.getStringExtra("run_id") ?: ""

         calories = intent.getIntExtra("calories", 0)
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
}