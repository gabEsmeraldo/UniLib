package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserHomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_home_page)

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<FloatingActionButton>(R.id.fabChat)?.setOnClickListener {
            startActivity(Intent(this, chat_ia_page::class.java))
        }

        findViewById<View>(R.id.navHome)?.setOnClickListener {
            // Já está na Home
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

        findViewById<View>(R.id.tvSeeAllRecommended)?.setOnClickListener {
            startActivity(Intent(this, UserSearchPage::class.java))
            finish()
        }

        findViewById<View>(R.id.tvSeeAllNews)?.setOnClickListener {
            startActivity(Intent(this, UserSearchPage::class.java))
            finish()
        }
    }
}