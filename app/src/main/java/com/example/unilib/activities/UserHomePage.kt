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

        NavBarHelper.setup(this, NavTab.HOME)
        setupBookCards()

        findViewById<FloatingActionButton>(R.id.fabChat)?.setOnClickListener {
            startActivity(Intent(this, chat_ia_page::class.java))
        }
    }

    private fun setupBookCards() {
        findViewById<View>(R.id.cardAlgoritmosHome).setOnClickListener {
            openBookDetails("Algoritmos e Estruturas de Dados", "blue")
        }

        findViewById<View>(R.id.cardCleanCodeHome).setOnClickListener {
            openBookDetails("Clean Code", "green")
        }

        findViewById<View>(R.id.cardEngenhariaHome).setOnClickListener {
            openBookDetails("Engenharia de Software", "purple")
        }

        findViewById<View>(R.id.cardIaHome).setOnClickListener {
            openBookDetails("Inteligência Artificial", "red")
        }

        findViewById<View>(R.id.cardBancoDadosHome).setOnClickListener {
            openBookDetails("Banco de Dados", "gray")
        }
    }

    private fun openBookDetails(title: String, color: String) {
        val intent = Intent(this, BookDetails::class.java)
        intent.putExtra("TITULO_LIVRO", title)
        intent.putExtra("BOOK_COLOR", color)
        intent.putExtra("NAV_TAB", NavTab.HOME.name)
        startActivity(intent)
    }
}
