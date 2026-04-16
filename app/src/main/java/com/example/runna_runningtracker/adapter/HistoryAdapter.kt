package com.example.runna_runningtracker.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runna_runningtracker.R
import com.example.runna_runningtracker.data.model.Run
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val runs: List<Run>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.title)
        val runType = view.findViewById<TextView>(R.id.run_type)
        val date = view.findViewById<TextView>(R.id.date)


        val distance = view.findViewById<TextView>(R.id.distance)
        val duration = view.findViewById<TextView>(R.id.duration)
<<<<<<< Updated upstream
        val pace = view.findViewById<TextView>(R.id.pace);
=======
        val pace = view.findViewById<TextView>(R.id.pace)
>>>>>>> Stashed changes
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int
    ): HistoryAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false);

        return ViewHolder(view)
    }

    override fun onBindViewHolder(view: HistoryAdapter.ViewHolder, postion: Int) {
            val run =runs[postion];

        val time = String.format("%02d:%02d", run.duration / 60, run.duration % 60)
        view.title.setText(run.type_runing)
        view.runType.setText(run.type_runing)
        view.distance.setText(run.getFormattedDistance());
        view.date.setText(formatDate(run.start_time))
        view.duration.setText(time)
        val pace  =getPace(run.duration.toLong(),run.duration.toDouble());
        view.pace.setText(pace)
        when (run.type_runing) {
             "Easy Run" -> {
                view.runType.setTextColor(0xFF2196F3.toInt())
                view.runType.backgroundTintList = ColorStateList.valueOf(0xFFE3F2FD.toInt())
            }
            "Long Run" -> {
                view.runType.setTextColor(0xFF4CAF50.toInt())
                view.runType.backgroundTintList = ColorStateList.valueOf(0xFFE8F5E9.toInt())
            }
            "Interval" -> {
                view.runType.setTextColor(0xFFFF5722.toInt())
                view.runType.backgroundTintList = ColorStateList.valueOf(0xFFFBE9E7.toInt())
            }
            "Walking" -> {
                view.runType.setTextColor(0xFF9C27B0.toInt())
                view.runType.backgroundTintList = ColorStateList.valueOf(0xFFF3E5F5.toInt())
            }
            else -> {
                view.runType.setTextColor(0xFF888888.toInt())
                view.runType.backgroundTintList = ColorStateList.valueOf(0xFFEEEEEE.toInt())
            }
        }
        view.runType.text = run.type_runing

    }

    override fun getItemCount(): Int {
        return runs.size
    }
    fun formatDate(timeInMillis: Long): String {

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date(timeInMillis)
        return sdf.format(date)
    }
    fun getPace(totalSeconds: Long, km: Double): String {
        if (km <= 0.0) return "0:00"

        // 1. Tính xem 1 km chạy mất bao nhiêu giây
        val secondsPerKm = (totalSeconds / km).toInt()

        // 2. Lấy số giây đó chia 60 để ra số phút
        val minutes = secondsPerKm / 60

        // 3. Phần dư chính là số giây lẻ
        val seconds = secondsPerKm % 60

        // 4. Trả về định dạng mm:ss (ví dụ 05:30)
        return String.format("%d:%02d", minutes, seconds)
    }
}