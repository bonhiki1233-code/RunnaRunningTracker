package com.example.runna_runningtracker

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

object NavigationHelper {

    enum class ActiveTab {
        HOME, START, CHALLENGES, HISTORY, PROFILE
    }

    fun setupBottomNav(activity: Activity, activeTab: ActiveTab) {
        val navHome = activity.findViewById<LinearLayout>(R.id.nav_home)
        val navAction2 = activity.findViewById<LinearLayout>(R.id.nav_action2)
        val navHistory = activity.findViewById<LinearLayout>(R.id.nav_history)
        val navProfile = activity.findViewById<LinearLayout>(R.id.nav_profile)

        if (navHome == null || navAction2 == null || navHistory == null || navProfile == null) return

        val navHomeIcon = activity.findViewById<ImageView>(R.id.nav_home_icon)
        val navHomeText = activity.findViewById<TextView>(R.id.nav_home_text)

        val navAction2Icon = activity.findViewById<ImageView>(R.id.nav_action2_icon)
        val navAction2Text = activity.findViewById<TextView>(R.id.nav_action2_text)

        val navHistoryIcon = activity.findViewById<ImageView>(R.id.nav_history_icon)
        val navHistoryText = activity.findViewById<TextView>(R.id.nav_history_text)

        val navProfileIcon = activity.findViewById<ImageView>(R.id.nav_profile_icon)
        val navProfileText = activity.findViewById<TextView>(R.id.nav_profile_text)

        val defaultColor = Color.parseColor("#888888")
        val activeColor = Color.parseColor("#FF7243")

        // Reset all to default first
        navHomeIcon.setColorFilter(defaultColor)
        navHomeText.setTextColor(defaultColor)

        // Action2 defaults to Start
        navAction2Icon.setImageResource(R.drawable.ic_pulse)
        navAction2Text.text = "Start"
        navAction2Icon.setColorFilter(defaultColor)
        navAction2Text.setTextColor(defaultColor)

        navHistoryIcon.setColorFilter(defaultColor)
        navHistoryText.setTextColor(defaultColor)

        navProfileIcon.setColorFilter(defaultColor)
        navProfileText.setTextColor(defaultColor)

        // Highlight active and handle Start vs Challenges flip
        when (activeTab) {
            ActiveTab.HOME -> {
                navHomeIcon.setColorFilter(activeColor)
                navHomeText.setTextColor(activeColor)
            }
            ActiveTab.START -> {
                navAction2Icon.setColorFilter(activeColor)
                navAction2Text.setTextColor(activeColor)
            }
            ActiveTab.CHALLENGES -> {
                navAction2Icon.setImageResource(R.drawable.ic_trophy)
                navAction2Text.text = "Challenges"
                navAction2Icon.setColorFilter(activeColor)
                navAction2Text.setTextColor(activeColor)
            }
            ActiveTab.HISTORY -> {
                navHistoryIcon.setColorFilter(activeColor)
                navHistoryText.setTextColor(activeColor)
            }
            ActiveTab.PROFILE -> {
                navProfileIcon.setColorFilter(activeColor)
                navProfileText.setTextColor(activeColor)
            }
        }

        // Set Click Listeners
        navHome.setOnClickListener {
            if (activeTab != ActiveTab.HOME) {
                navigateTo(activity, HomeActivity::class.java)
            }
        }
        navAction2.setOnClickListener {
            // Action2 flips based on whether we were showing Challenges or Start
            if (activeTab == ActiveTab.CHALLENGES) {
                // Ignore if already on Challenges. Wait, shouldn't clicking Start icon take us to Start?
                // The requirements say Challenges *replaces* Start visually, or reflects Challenges is active.
            } else {
                if (activeTab != ActiveTab.START) {
                    navigateTo(activity, StartRunningActivity::class.java)
                }
            }
        }
        navHistory.setOnClickListener {
            if (activeTab != ActiveTab.HISTORY) {
                navigateTo(activity, HistoryActivity::class.java)
            }
        }
        navProfile.setOnClickListener {
            if (activeTab != ActiveTab.PROFILE) {
                navigateTo(activity, ProfileActivity::class.java)
            }
        }
    }

    private fun navigateTo(activity: Activity, target: Class<*>) {
        val intent = Intent(activity, target)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
        activity.finish()
    }
}
