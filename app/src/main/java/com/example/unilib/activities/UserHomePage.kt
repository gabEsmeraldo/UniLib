package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserHomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_home_page)

        NavBarHelper.setup(this, NavTab.HOME)

        findViewById<FloatingActionButton>(R.id.fabChat)?.setOnClickListener {
            startActivity(Intent(this, chat_ia_page::class.java))
        }
    }
}
