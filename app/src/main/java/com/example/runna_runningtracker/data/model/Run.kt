package com.example.runna_runningtracker.data.model

data class Run(
    val run_id: String = "",
    val user_id: String = "",
    val distance: Double = 0.0,
    val duration: Int = 0,
    val pace: Double = 0.0,
    val calories: Int = 0,
    val start_time: Long = 0L,
    val end_time : Long =0L,
    val type_runing :String=""

)
{
    fun getFormattedDistance(): String {
        return String.format("%.2f km", distance)
    }
}