package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unilib.R
import androidx.cardview.widget.CardView

class UserSearchPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_search_page)

        val cardAlgoritmos = findViewById<CardView>(R.id.CardView1)
        val cardEngenharia = findViewById<CardView>(R.id.CardView2)
        val cardCleanCode = findViewById<CardView>(R.id.CardView3)
        val cardRedes = findViewById<CardView>(R.id.CardView4)

        cardAlgoritmos.setOnClickListener {
            irParaDetalhes("Algoritmos e Estruturas de Dados")
        }

        cardEngenharia.setOnClickListener {
            irParaDetalhes("Engenharia de Software")
        }

        cardCleanCode.setOnClickListener {
            irParaDetalhes("Clean Code")
        }

        cardRedes.setOnClickListener {
            irParaDetalhes("Redes de Computadores")
        }

    }

    private fun irParaDetalhes(titulo: String) {
        val intent = Intent(this, BookDetails::class.java)
        intent.putExtra("TITULO_LIVRO", titulo)
        startActivity(intent)
    }
}