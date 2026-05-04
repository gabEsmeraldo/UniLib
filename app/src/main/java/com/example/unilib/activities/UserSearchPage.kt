package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import androidx.cardview.widget.CardView

class UserSearchPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_search_page)

        NavBarHelper.setup(this, NavTab.SEARCH)

        val cardAlgoritmos = findViewById<CardView>(R.id.CardView1)
        val cardEngenharia = findViewById<CardView>(R.id.CardView2)
        val cardCleanCode = findViewById<CardView>(R.id.CardView3)
        val cardRedes = findViewById<CardView>(R.id.CardView4)

        cardAlgoritmos.setOnClickListener { irParaDetalhes("Algoritmos e Estruturas de Dados", "blue") }
        cardEngenharia.setOnClickListener { irParaDetalhes("Engenharia de Software", "purple") }
        cardCleanCode.setOnClickListener { irParaDetalhes("Clean Code", "green") }
        cardRedes.setOnClickListener { irParaDetalhes("Redes de Computadores", "red") }
    }

    private fun irParaDetalhes(titulo: String, color: String) {
        val intent = Intent(this, BookDetails::class.java)
        intent.putExtra("TITULO_LIVRO", titulo)
        intent.putExtra("BOOK_COLOR", color)
        intent.putExtra("NAV_TAB", NavTab.SEARCH.name)
        startActivity(intent)
    }
}
