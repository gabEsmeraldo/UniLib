package com.example.unilib.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class chat_ia_page : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_ia_page)

        NavBarHelper.setup(this, NavTab.NONE)

        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }
}
