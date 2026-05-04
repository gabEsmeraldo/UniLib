package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class AdminHomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_home_page)

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.btnAddBook)?.setOnClickListener {
            startActivity(Intent(this, AdminAddBook::class.java))
        }

        findViewById<View>(R.id.tvManageCollection)?.setOnClickListener {
            startActivity(Intent(this, AdminSearchPage::class.java))
            finish()
        }

        findViewById<View>(R.id.navAdminHome)?.setOnClickListener {
            // Já está na home do administrador
        }

        findViewById<View>(R.id.navAdminSearch)?.setOnClickListener {
            startActivity(Intent(this, AdminSearchPage::class.java))
            finish()
        }

        findViewById<View>(R.id.navAdminLoans)?.setOnClickListener {
            startActivity(Intent(this, emprestimo_admin_page::class.java))
            finish()
        }
    }
}