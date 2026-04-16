package com.example.runna_runningtracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runna_runningtracker.adapter.HistoryAdapter
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.RunsHistoryRepository

class HistoryActivity : AppCompatActivity() {
 val TAG ="HistoryActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history) // Tên file XML của bạn

        NavigationHelper.setupBottomNav(this, NavigationHelper.ActiveTab.HISTORY)

        // Ánh xạ nút sắp xếp (a-z) nếu bạn muốn thêm tính năng click
        val imgSort = findViewById<ImageView>(R.id.imgSort)
        // Lưu ý: Bạn cần thêm android:id="@+id/imgSort" vào ImageView trong XML của bạn

        imgSort?.setOnClickListener {
            Toast.makeText(this, "Tính năng sắp xếp đang phát triển", Toast.LENGTH_SHORT).show()
        }

        historyDisplay();
    }
    fun historyDisplay()
    {
        val history_recycleView =findViewById<RecyclerView>(R.id.history);
        val nothing_display =findViewById<LinearLayout>(R.id.nothing_history)
        history_recycleView.layoutManager = LinearLayoutManager(this);
        val userid = AuthRepository().getCurrentUserId();
        if(userid!=null)
        {
            RunsHistoryRepository.getHistory(userid){
                runs->
                if(runs.isEmpty())
                {       nothing_display.visibility = View.VISIBLE;
                    Log.d(TAG,"history trong")
                }
                else{
                    nothing_display.visibility = View.GONE;
                    val adapter = HistoryAdapter(runs);

                    history_recycleView.adapter =adapter;

                }
            }
        }



    }
}