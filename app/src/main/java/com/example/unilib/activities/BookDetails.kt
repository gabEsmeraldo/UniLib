package com.example.unilib.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class BookDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_details)

        val activeTab = intent.getStringExtra("NAV_TAB")?.let { runCatching { NavTab.valueOf(it) }.getOrNull() } ?: NavTab.NONE
        NavBarHelper.setup(this, activeTab)
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        val requestedTitle = intent.getStringExtra("TITULO_LIVRO")
        val book = requestedTitle?.let { booksByTitle[it] } ?: booksByTitle.values.first()
        renderBook(book)

        val requestedColor = intent.getStringExtra("BOOK_COLOR") ?: colorByTitle[book.title] ?: "blue"
        applyBookColor(requestedColor)

        val btnLocalizar = findViewById<LinearLayout>(R.id.btnLocalizar)
        val btnReservar = findViewById<LinearLayout>(R.id.btnReservar)

        btnLocalizar.setOnClickListener {
            val intent = Intent(this, map_page::class.java)
            intent.putExtra("NAV_TAB", activeTab.name)
            startActivity(intent)
            finish()
        }

        btnReservar.setOnClickListener {
            ReservaModalHelper.show(this)
        }
    }

    private fun renderBook(book: BookInfo) {
        findViewById<TextView>(R.id.tvBookTitle).text = book.title
        findViewById<TextView>(R.id.tvBookAuthor).text = book.author
        findViewById<TextView>(R.id.tvIsbnHero).text = "ISBN: ${book.isbn}"
        findViewById<TextView>(R.id.tvIsbn).text = book.isbn
        findViewById<TextView>(R.id.tvDisponiveis).text = book.available
        findViewById<TextView>(R.id.tvEmprestados).text = book.borrowed
        findViewById<TextView>(R.id.tvLocalizacao).text = book.location
        findViewById<TextView>(R.id.tvSinopse).text = book.synopsis
    }

    private fun applyBookColor(color: String) {
        val theme = colorThemes[color] ?: colorThemes.getValue("blue")
        findViewById<View>(R.id.bookCover).setBackgroundResource(theme.coverDrawable)
        findViewById<View>(R.id.headerBar).setBackgroundColor(Color.parseColor(theme.darkColor))
        findViewById<View>(R.id.heroSection).setBackgroundColor(Color.parseColor(theme.heroColor))
    }

    private data class BookInfo(
        val title: String,
        val author: String,
        val isbn: String,
        val available: String,
        val borrowed: String,
        val location: String,
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
            "C\u00e1lculo Vol. 1" to "blue",
            "Clean Architecture" to "green",
            "Design Patterns" to "purple",
            "Algoritmos 1" to "blue",
            "Desenvolvimento" to "green",
            "Algoritmos e Estruturas de Dados" to "blue",
            "Engenharia de Software" to "purple",
            "Clean Code" to "green",
            "Redes de Computadores" to "red",
            "Inteligência Artificial" to "red",
            "Banco de Dados" to "gray"
        )

        val booksByTitle = listOf(
            BookInfo(
                title = "C\u00e1lculo Vol. 1",
                author = "James Stewart",
                isbn = "978-85-216-2583-4",
                available = "2",
                borrowed = "4",
                location = "B-04",
                synopsis = "Livro introdutorio de calculo diferencial e integral, com exemplos aplicados, exercicios e fundamentos para cursos de ciencias exatas."
            ),
            BookInfo(
                title = "Clean Architecture",
                author = "Robert C. Martin",
                isbn = "978-85-7522-706-8",
                available = "1",
                borrowed = "3",
                location = "C-10",
                synopsis = "Apresenta principios para organizar sistemas de software com baixo acoplamento, regras de negocio protegidas e codigo mais facil de manter."
            ),
            BookInfo(
                title = "Design Patterns",
                author = "Gang of Four",
                isbn = "978-02-0163-361-0",
                available = "1",
                borrowed = "2",
                location = "C-12",
                synopsis = "Catalogo classico de padroes de projeto orientados a objetos, com solucoes reutilizaveis para problemas recorrentes de arquitetura."
            ),
            BookInfo(
                title = "Algoritmos 1",
                author = "Thomas H. Cormen et al.",
                isbn = "978-85-216-1474-6",
                available = "3",
                borrowed = "2",
                location = "A-12",
                synopsis = "Introducao ao estudo de algoritmos, estruturas de dados e analise de complexidade para resolver problemas computacionais com eficiencia."
            ),
            BookInfo(
                title = "Desenvolvimento",
                author = "Ian Sommerville",
                isbn = "978-85-7922-015-2",
                available = "2",
                borrowed = "3",
                location = "B-08",
                synopsis = "Material de apoio para desenvolvimento e engenharia de software, cobrindo requisitos, projeto, implementacao, testes e manutencao."
            ),
            BookInfo(
                title = "Algoritmos e Estruturas de Dados",
                author = "Thomas H. Cormen et al.",
                isbn = "978-85-216-1474-6",
                available = "3",
                borrowed = "2",
                location = "A-12",
                synopsis = "Introducao ao estudo de algoritmos, estruturas de dados e analise de complexidade para resolver problemas computacionais com eficiencia."
            ),
            BookInfo(
                title = "Engenharia de Software",
                author = "Ian Sommerville",
                isbn = "978-85-7922-015-2",
                available = "2",
                borrowed = "3",
                location = "B-08",
                synopsis = "Apresenta fundamentos de engenharia de software, processos, requisitos, arquitetura, testes e evolucao de sistemas."
            ),
            BookInfo(
                title = "Clean Code",
                author = "Robert C. Martin",
                isbn = "978-85-7522-200-1",
                available = "1",
                borrowed = "5",
                location = "C-09",
                synopsis = "Guia pratico para escrever codigo legivel, simples e testavel, com tecnicas de refatoracao e boas praticas de manutencao."
            ),
            BookInfo(
                title = "Redes de Computadores",
                author = "Andrew S. Tanenbaum",
                isbn = "978-85-7605-924-0",
                available = "4",
                borrowed = "1",
                location = "D-03",
                synopsis = "Aborda conceitos essenciais de redes, protocolos, camadas, transmissao de dados e funcionamento da Internet."
            ),
            BookInfo(
                title = "Inteligência Artificial",
                author = "Russell & Norvig",
                isbn = "978-85-508-0534-8",
                available = "5",
                borrowed = "1",
                location = "D-07",
                synopsis = "Introduz conceitos de agentes inteligentes, busca, representacao de conhecimento, aprendizado de maquina e tomada de decisao."
            ),
            BookInfo(
                title = "Banco de Dados",
                author = "Date, C.J.",
                isbn = "978-85-352-9176-2",
                available = "2",
                borrowed = "3",
                location = "B-11",
                synopsis = "Cobre modelagem relacional, algebra relacional, normalizacao, SQL e fundamentos para projeto e manutencao de bancos de dados."
            )
        ).associateBy { it.title }
    }
}
