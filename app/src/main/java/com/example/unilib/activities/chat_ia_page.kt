package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class chat_ia_page : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_ia_page)

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.navHome)?.setOnClickListener {
            startActivity(Intent(this, UserHomePage::class.java))
            finish()
        }

        findViewById<View>(R.id.navSearch)?.setOnClickListener {
            startActivity(Intent(this, UserSearchPage::class.java))
            finish()
        }

        findViewById<View>(R.id.navLoans)?.setOnClickListener {
            startActivity(Intent(this, emprestimo_page::class.java))
            finish()
        }

        findViewById<View>(R.id.navAccount)?.setOnClickListener {
            startActivity(Intent(this, user_account::class.java))
            finish()
        }
    }
}