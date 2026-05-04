package com.example.unilib.activities

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

enum class NavTab { HOME, SEARCH, LOANS, ACCOUNT, NONE }

object NavBarHelper {

    fun setup(activity: AppCompatActivity, activeTab: NavTab) {
        setupTab(activity, R.id.navHome, activeTab == NavTab.HOME) {
            activity.startActivity(Intent(activity, UserHomePage::class.java))
            activity.finish()
        }
        setupTab(activity, R.id.navSearch, activeTab == NavTab.SEARCH) {
            activity.startActivity(Intent(activity, UserSearchPage::class.java))
            activity.finish()
        }
        setupTab(activity, R.id.navLoans, activeTab == NavTab.LOANS) {
            activity.startActivity(Intent(activity, emprestimo_page::class.java))
            activity.finish()
        }
        setupTab(activity, R.id.navAccount, activeTab == NavTab.ACCOUNT) {
            activity.startActivity(Intent(activity, user_account::class.java))
            activity.finish()
        }
    }

    private fun setupTab(activity: AppCompatActivity, id: Int, active: Boolean, onClick: () -> Unit) {
        val view = activity.findViewById<View>(id) ?: return
        if (active) {
            view.setBackgroundResource(R.drawable.bg_nav_active)
        } else {
            view.setBackgroundResource(0)
            view.setOnClickListener { onClick() }
        }
        val color = if (active) Color.parseColor("#1474C4") else Color.parseColor("#9BAAC0")
        (view as? ViewGroup)?.let { group ->
            for (i in 0 until group.childCount) {
                when (val child = group.getChildAt(i)) {
                    is ImageView -> child.setColorFilter(color)
                    is TextView -> child.setTextColor(color)
                }
            }
        }
    }
}
