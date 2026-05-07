package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class user_account : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_account)

        NavBarHelper.setup(this, NavTab.ACCOUNT)

        setupBookCards()
        setupAccountActions()
        setupNotificationsButton()
    }

    private fun setupNotificationsButton() {
        findViewById<FrameLayout>(R.id.btnNotifications).setOnClickListener {
            NotificationsModalHelper.show(this)
        }
    }

    private fun setupBookCards() {
        openBook(R.id.cardReservaCalculo, "Cálculo Vol. 1", "blue")
        openBook(R.id.cardReservaCleanArchitecture, "Clean Architecture", "green")
        openBook(R.id.cardReservaDesignPatterns, "Design Patterns", "purple")
        openBook(R.id.cardEmprestimoAlgoritmos, "Algoritmos 1", "blue")
        openBook(R.id.cardEmprestimoDesenvolvimento, "Desenvolvimento", "green")
    }

    private fun setupAccountActions() {
        findViewById<View>(R.id.tvVerTodasReservas)?.setOnClickListener {
            startActivity(Intent(this, emprestimo_page::class.java))
        }

        findViewById<View>(R.id.btnSair)?.setOnClickListener {
            val intent = Intent(this, StartPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun openBook(cardId: Int, title: String, color: String) {
        findViewById<View>(cardId)?.setOnClickListener {
            val intent = Intent(this, BookDetails::class.java)
            intent.putExtra("TITULO_LIVRO", title)
            intent.putExtra("BOOK_COLOR", color)
            intent.putExtra("NAV_TAB", NavTab.ACCOUNT.name)
            startActivity(intent)
        }
    }
}