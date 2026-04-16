package com.example.runna_runningtracker.data.repository

import android.icu.util.Calendar
import android.util.Log
import com.example.runna_runningtracker.data.model.Run
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object RunsHistoryRepository {
    private val db = FirebaseFirestore.getInstance();
    private val connect = db.collection("runs_history");
    private val tag ="RunsHistoryRepository";
    //ham save lich su chay voi run model
    fun save(run: Run, onHandle: (Boolean) -> Unit) {
        connect.add(run).addOnSuccessListener { onHandle(true) }
            .addOnFailureListener { onHandle(false) }

    }

    //ham lay lich su chay voi run model
    fun getHistory(userId: String, onResult: (List<Run>) -> Unit) {


        connect.whereEqualTo("user_id", userId)
            .orderBy("start_time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->

                onResult(snapshots.toObjects<Run>(Run::class.java))
            }
            .addOnFailureListener { e ->


                onResult(emptyList())
            }
    }
    //ham lay history trong tuan nay
    fun getWeekHistory(userId: String,onResult: (List<Run>) -> Unit)
    {
        val (start, end) = getWeekRange()

        connect.whereEqualTo("user_id", userId)
            .whereGreaterThanOrEqualTo("start_time", start)
            .whereLessThan("start_time", end)
            .orderBy("start_time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->

                onResult(snapshots.toObjects<Run>(Run::class.java))
            }
            .addOnFailureListener { e ->

                Log.e(tag, "LỖI TRUY VẤN TUẦN: ${e.message}")
                onResult(emptyList())
            }
    }

    //lay khoang thoi gian giua thu 2 trong tuan nay va tuan sau
    fun getWeekRange(): Pair<Long, Long>{
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,0)

            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        val startOfWeek =cal.timeInMillis;

        cal.add(Calendar.DAY_OF_WEEK,7);

        val endOfWeek =cal.timeInMillis;
        return Pair(startOfWeek,endOfWeek)
    }
}