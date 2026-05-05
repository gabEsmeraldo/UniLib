package com.example.unilib.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import android.widget.Button
import android.widget.ImageButton
import android.content.Intent


class AdminAddBook : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_add_book)

        val btnEnter = findViewById<Button>(R.id.btnEnter)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        btnBack.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
            startActivity(intent)
            finish()
        }

        btnEnter.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
            startActivity(intent)
            finish()
        }

        btnCancel.setOnClickListener { finish() }
    
    }


}