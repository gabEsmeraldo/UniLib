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

        AdminNavBarHelper.setup(this, AdminNavTab.HOME)
        setupNavigation()
        setupBookCards()
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.btnAdminLogout)?.setOnClickListener {
            val intent = Intent(this, StartPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        findViewById<View>(R.id.btnAddBook)?.setOnClickListener {
            startActivity(Intent(this, AdminAddBook::class.java))
        }

        findViewById<View>(R.id.tvManageCollection)?.setOnClickListener {
            startActivity(Intent(this, AdminSearchPage::class.java))
            finish()
        }
    }

    private fun setupBookCards() {
        findViewById<View>(R.id.cardAlgoritmosAdminHome)?.setOnClickListener {
            openBookDetails("Algoritmos e Estruturas de Dados", "blue")
        }

        findViewById<View>(R.id.cardCleanCodeAdminHome)?.setOnClickListener {
            openBookDetails("Clean Code", "green")
        }

        findViewById<View>(R.id.cardBancoDadosAdminHome)?.setOnClickListener {
            openBookDetails("Banco de Dados", "red")
        }
    }

    private fun openBookDetails(title: String, color: String) {
        val intent = Intent(this, admin_book_details::class.java)
        intent.putExtra("TITULO_LIVRO", title)
        intent.putExtra("BOOK_COLOR", color)
        startActivity(intent)
    }
}
