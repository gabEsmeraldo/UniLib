package com.example.unilib.activities

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class map_page : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_page)

        val activeTab = intent.getStringExtra("NAV_TAB")
            ?.let { runCatching { NavTab.valueOf(it) }.getOrNull() }
            ?: NavTab.SEARCH

        NavBarHelper.setup(this, activeTab)

        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        setupNotificationsButton()
    }

    private fun setupNotificationsButton() {
        findViewById<FrameLayout>(R.id.btnNotifications).setOnClickListener {
            NotificationsModalHelper.show(this)
        }
    }
}