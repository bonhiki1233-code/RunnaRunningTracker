package com.example.runna_runningtracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runna_runningtracker.R
import com.example.runna_runningtracker.data.model.Run
import java.util.Calendar

class HomeRecentAdapter(private val items: List<Run>) :
    RecyclerView.Adapter<HomeRecentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parrent: ViewGroup,
        p1: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parrent.context)
            .inflate(R.layout.home_recent_recycle_item, parrent, false);

        return ViewHolder(view);
    }

    override fun onBindViewHolder(
        view: ViewHolder,
        pos: Int
    ) {
        val run = items[pos];
        view.runtype.setText(run.type_runing);
        view.rundate.setText(gDisplayDate(run.start_time))
        val time = String.format("%02d:%02d", run.duration / 60, run.duration % 60)
        val describe = "${run.getFormattedDistance()} • ${time}"
        view.runDescribe.setText(describe)

    }

    override fun getItemCount(): Int {
        val size = items.size;
        if (size >= 3) {
            return 3;
        } else {
            return size;
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val runtype: TextView = view.findViewById<TextView>(R.id.runType);
        val rundate: TextView = view.findViewById<TextView>(R.id.runDate);


        val runDescribe: TextView = view.findViewById<TextView>(R.id.runDescribe);


    }

    fun gDisplayDate(startTime: Long): String {
        val now = Calendar.getInstance()
        val runDate = Calendar.getInstance().apply { timeInMillis = startTime }


        val isSameYear = now.get(Calendar.YEAR) == runDate.get(Calendar.YEAR)
        val isSameDay =
            isSameYear && now.get(Calendar.DAY_OF_YEAR) == runDate.get(Calendar.DAY_OF_YEAR)


        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == runDate.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == runDate.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> "Today"
            isYesterday -> "Yesterday"
            else -> "Long time ago"

        }
    }
}