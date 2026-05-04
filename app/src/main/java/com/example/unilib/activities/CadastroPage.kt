package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class CadastroPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro_page)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnCadastrar).setOnClickListener {
            startActivity(Intent(this, UserHomePage::class.java))
        }

        findViewById<View>(R.id.btnFazerLogin).setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
        }
    }
}
