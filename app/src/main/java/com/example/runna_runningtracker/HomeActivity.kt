package com.example.runna_runningtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runna_runningtracker.adapter.HomeRecentAdapter
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.RunsHistoryRepository

class HomeActivity : AppCompatActivity() {
    private val TAG : String ="HomeActivity"
    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        NavigationHelper.setupBottomNav(this, NavigationHelper.ActiveTab.HOME)

        val tvWelcomeHome = findViewById<TextView>(R.id.tvWelcomeHome)
        val btnStartRunning = findViewById<Button>(R.id.btnStartRunning)
        val cvChallenges = findViewById<androidx.cardview.widget.CardView>(R.id.cvChallenges)

//        val tvRunStats1 = findViewById<TextView>(R.id.tvRunStats1)
//        val tvRunStats2 = findViewById<TextView>(R.id.tvRunStats2)
//        val tvRunStats3 = findViewById<TextView>(R.id.tvRunStats3)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPref.getString("name", "Runner")
        tvWelcomeHome.text = "Welcome back, $savedName!"

        btnStartRunning.setOnClickListener {
            val intent = Intent(this, StartRunningActivity::class.java)
            startActivity(intent)
        }

        cvChallenges?.setOnClickListener {
            startActivity(Intent(this, ChallengesActivity::class.java))
        }
        recent_view();
        week_statisic_Display()
    }
    fun recent_view() {
        val recent_nothing =findViewById<TextView>(R.id.recent_nothing);
        val userid = AuthRepository().getCurrentUserId()
        if (userid != null) {

            val recyclerView: RecyclerView = findViewById(R.id.recent_recyclerview)


            recyclerView.layoutManager = LinearLayoutManager(this)

            RunsHistoryRepository.getHistory(userid) { runs ->
                if (runs.isEmpty()) {

                    recent_nothing.visibility = View.VISIBLE;
                } else {
                    recent_nothing.visibility = View.GONE;

                    val adapter = HomeRecentAdapter(runs)
                    recyclerView.adapter = adapter
                }
            }
        } else {
            Log.d("home", "userid bị null")
        }
    }
    fun week_statisic_Display()

    {
        Log.d(TAG,"dang bat dau hien thi week statisic")
        val total_disntance =findViewById<TextView>(R.id.total_distance_km);
        val total_run =findViewById<TextView>(R.id.total_runs);

        val userId = AuthRepository().getCurrentUserId();
        if(userId!=null)
        {
            Log.d(TAG,"lay duoc user id de lay danh sach tuan")
            RunsHistoryRepository.getWeekHistory(userId){
                runs->

                if(runs.isEmpty())
                {
                    total_disntance.setText("0 km")
                    total_run.setText("0")
                }
                else{
                    val total_km =runs.sumOf { it.distance }

                    val total =runs.size;
                    total_disntance.setText("${String.format("%.2f km", total_km)} km")
                    total_run.setText(total.toString())
                }
            }
        }


    }


}
