package com.example.unilib.activities

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

enum class AdminNavTab { HOME, SEARCH, LOANS, NONE }

object AdminNavBarHelper {
    fun setup(activity: AppCompatActivity, activeTab: AdminNavTab) {
        setupTab(
            activity = activity,
            ids = intArrayOf(R.id.navAdminHome, R.id.nav_home),
            active = activeTab == AdminNavTab.HOME
        ) {
            activity.startActivity(Intent(activity, AdminHomePage::class.java))
            activity.finish()
        }

        setupTab(
            activity = activity,
            ids = intArrayOf(R.id.navAdminSearch, R.id.nav_search),
            active = activeTab == AdminNavTab.SEARCH
        ) {
            activity.startActivity(Intent(activity, AdminSearchPage::class.java))
            activity.finish()
        }

        setupTab(
            activity = activity,
            ids = intArrayOf(R.id.navAdminLoans, R.id.nav_emprestimos),
            active = activeTab == AdminNavTab.LOANS
        ) {
            activity.startActivity(Intent(activity, emprestimo_admin_page::class.java))
            activity.finish()
        }
    }

    private fun setupTab(
        activity: AppCompatActivity,
        ids: IntArray,
        active: Boolean,
        onClick: () -> Unit
    ) {
        var view: View? = null
        for (id in ids) {
            view = activity.findViewById(id)
            if (view != null) break
        }
        view ?: return
        if (active) {
            view.setBackgroundResource(R.drawable.bg_nav_active)
            view.setOnClickListener(null)
        } else {
            view.setBackgroundResource(0)
            view.setOnClickListener { onClick() }
        }

        val color = if (active) Color.parseColor("#1474C4") else Color.parseColor("#9BAAC0")
        tintChildren(view, color)
    }

    private fun tintChildren(view: View, color: Int) {
        when (view) {
            is ImageView -> view.setColorFilter(color)
            is TextView -> view.setTextColor(color)
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    tintChildren(view.getChildAt(i), color)
                }
            }
        }
    }
}
