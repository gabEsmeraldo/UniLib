package com.example.unilib.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class admin_book_details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_book_details)

        val activeTab = intent.getStringExtra("ADMIN_NAV_TAB")?.let { runCatching { AdminNavTab.valueOf(it) }.getOrNull() } ?: AdminNavTab.HOME
        AdminNavBarHelper.setup(this, activeTab)
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        val requestedTitle = intent.getStringExtra("TITULO_LIVRO")
        val book = requestedTitle?.let { booksByTitle[it] } ?: booksByTitle.values.first()
        renderBook(book)

        val requestedColor = intent.getStringExtra("BOOK_COLOR") ?: colorByTitle[book.title] ?: "blue"
        applyBookColor(requestedColor)
    }

    private fun renderBook(book: BookInfo) {
        findViewById<TextView>(R.id.tvBookTitle)?.text = book.title
        findViewById<TextView>(R.id.tvBookAuthor)?.text = book.author
        findViewById<TextView>(R.id.tvIsbn)?.text = "ISBN: ${book.isbn}"
        findViewById<TextView>(R.id.tvTotal)?.text = book.total
        findViewById<TextView>(R.id.tvDisponiveis)?.text = book.available
        findViewById<TextView>(R.id.tvEmprestados)?.text = book.borrowed
        findViewById<TextView>(R.id.tvReservados)?.text = book.reserved
        findViewById<TextView>(R.id.tvSinopseDisplay)?.text = book.synopsis
    }

    private fun applyBookColor(color: String) {
        val theme = colorThemes[color] ?: colorThemes.getValue("blue")
        findViewById<View>(R.id.bookCover)?.setBackgroundResource(theme.coverDrawable)
        findViewById<View>(R.id.headerBar)?.setBackgroundColor(Color.parseColor(theme.darkColor))
        findViewById<View>(R.id.heroSection)?.setBackgroundColor(Color.parseColor(theme.heroColor))
    }

    private data class BookInfo(
        val title: String,
        val author: String,
        val isbn: String,
        val total: String,
        val available: String,
        val borrowed: String,
        val reserved: String,
        val synopsis: String
    )

    private data class BookColorTheme(
        val coverDrawable: Int,
        val darkColor: String,
        val heroColor: String
    )

    private companion object {
        val colorThemes = mapOf(
            "blue" to BookColorTheme(R.drawable.bg_book_blue, "#0D47A1", "#1565C0"),
            "green" to BookColorTheme(R.drawable.bg_book_green, "#004D40", "#00695C"),
            "purple" to BookColorTheme(R.drawable.bg_book_purple, "#4A148C", "#6A1B9A"),
            "red" to BookColorTheme(R.drawable.bg_book_red, "#B71C1C", "#C62828"),
            "gray" to BookColorTheme(R.drawable.bg_book_gray, "#37474F", "#546E7A")
        )

        val colorByTitle = mapOf(
            "Algoritmos e Estruturas de Dados" to "blue",
            "Engenharia de Software" to "purple",
            "Clean Code" to "green",
            "Redes de Computadores" to "red",
            "Banco de Dados" to "red"
        )

        val booksByTitle = listOf(
            BookInfo("Algoritmos e Estruturas de Dados", "Thomas H. Cormen et al.", "978-85-216-1474-6", "5", "3", "2", "1", "Introducao ao estudo de algoritmos, estruturas de dados e analise de complexidade para resolver problemas computacionais com eficiencia."),
            BookInfo("Engenharia de Software", "Ian Sommerville", "978-85-7922-015-2", "3", "0", "3", "1", "Apresenta fundamentos de engenharia de software, processos, requisitos, arquitetura, testes e evolucao de sistemas."),
            BookInfo("Clean Code", "Robert C. Martin", "978-85-7522-200-1", "6", "1", "5", "2", "Guia pratico para escrever codigo legivel, simples e testavel, com tecnicas de refatoracao e boas praticas de manutencao."),
            BookInfo("Redes de Computadores", "Andrew S. Tanenbaum", "978-85-7605-924-0", "10", "1", "9", "0", "Aborda conceitos essenciais de redes, protocolos, camadas, transmissao de dados e funcionamento da Internet."),
            BookInfo("Banco de Dados", "Date, C.J.", "978-85-352-9176-2", "5", "2", "3", "1", "Cobre modelagem relacional, algebra relacional, normalizacao, SQL e fundamentos para projeto e manutencao de bancos de dados.")
        ).associateBy { it.title }
    }
}
