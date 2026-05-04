package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class StartPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.start_page)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<View>(R.id.btnLogin).setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnRegister).setOnClickListener {
            val intent = Intent(this, CadastroPage::class.java)
            startActivity(intent)
        }
    }
}