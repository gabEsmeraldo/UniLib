package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.example.unilib.repository.BookRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHomePage : AppCompatActivity() {

    private val bookRepository = BookRepository()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var txtNomeUsuario: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_home_page)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        txtNomeUsuario = findViewById(R.id.nomeUsuario)

        carregarNomeUsuario()

        NavBarHelper.setup(this, NavTab.HOME)
        setupNotificationsButton()
        setupChatButton()
        loadTopLentBooks()
        loadNewestBooks()
    }

    private fun setupNotificationsButton() {
        findViewById<FrameLayout>(R.id.btnNotifications).setOnClickListener {
            NotificationsModalHelper.show(this)
        }
    }

    private fun setupChatButton() {
        findViewById<FloatingActionButton>(R.id.fabChat)?.setOnClickListener {
            val intent = Intent(this, chat_ia_page::class.java)
            intent.putExtra("NAV_TAB", NavTab.HOME.name)
            startActivity(intent)
        }
    }

    private fun carregarNomeUsuario() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nomeBruto = document.get("nome")
                        val nomeFormatado = obterDoisPrimeirosNomes(nomeBruto)

                        if (nomeFormatado.isNotEmpty()) {
                            txtNomeUsuario.text = nomeFormatado
                        } else {
                            txtNomeUsuario.text = "Usuário"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao carregar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            txtNomeUsuario.text = "Usuário"
        }
    }

    private fun obterDoisPrimeirosNomes(nomeDoBanco: Any?): String {
        val nomeCompleto = java.lang.String.valueOf(nomeDoBanco ?: "")
        val nomeLimpo = nomeCompleto.replace("[", "").replace("]", "").trim()

        return nomeLimpo.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString(" ")
    }

    private fun loadNewestBooks() {
        val container = findViewById<LinearLayout>(R.id.llNewBooks)
        val backgrounds = listOf(
            R.drawable.bg_book_blue,
            R.drawable.bg_book_green,
            R.drawable.bg_book_purple,
            R.drawable.bg_book_red,
            R.drawable.bg_book_gray
        )
        val colorNames = listOf("blue", "green", "purple", "red", "gray")

        bookRepository.getNewestBooks(10,
            onSuccess = { books ->
                books.forEachIndexed { index, book ->
                    val card = LayoutInflater.from(this)
                        .inflate(R.layout.item_book_card, container, false)

                    card.findViewById<FrameLayout>(R.id.bookCover)
                        .setBackgroundResource(backgrounds[index % backgrounds.size])
                    card.findViewById<TextView>(R.id.tvAvailBadge).text = "${book.available} disp."
                    card.findViewById<TextView>(R.id.tvBookTitle).text = book.title
                    card.findViewById<TextView>(R.id.tvBookAuthor).text = book.author

                    val colorName = colorNames[index % colorNames.size]
                    card.setOnClickListener {
                        val intent = Intent(this, BookDetails::class.java)
                        intent.putExtra("BOOK_ID", book.id)
                        intent.putExtra("BOOK_COLOR", colorName)
                        intent.putExtra("NAV_TAB", NavTab.HOME.name)
                        startActivity(intent)
                    }

                    container.addView(card)
                }
            },
            onError = {}
        )
    }

    private fun loadTopLentBooks() {
        val container = findViewById<LinearLayout>(R.id.llRecommendedBooks)
        val backgrounds = listOf(
            R.drawable.bg_book_blue,
            R.drawable.bg_book_green,
            R.drawable.bg_book_purple,
            R.drawable.bg_book_red,
            R.drawable.bg_book_gray
        )
        val colorNames = listOf("blue", "green", "purple", "red", "gray")

        bookRepository.getTopLentBooks(10,
            onSuccess = { books ->
                books.forEachIndexed { index, book ->
                    val card = LayoutInflater.from(this)
                        .inflate(R.layout.item_book_card, container, false)

                    card.findViewById<FrameLayout>(R.id.bookCover)
                        .setBackgroundResource(backgrounds[index % backgrounds.size])
                    card.findViewById<TextView>(R.id.tvAvailBadge).text = "${book.available} disp."
                    card.findViewById<TextView>(R.id.tvBookTitle).text = book.title
                    card.findViewById<TextView>(R.id.tvBookAuthor).text = book.author

                    val colorName = colorNames[index % colorNames.size]
                    card.setOnClickListener {
                        val intent = Intent(this, BookDetails::class.java)
                        intent.putExtra("BOOK_ID", book.id)
                        intent.putExtra("BOOK_COLOR", colorName)
                        intent.putExtra("NAV_TAB", NavTab.HOME.name)
                        startActivity(intent)
                    }

                    container.addView(card)
                }
            },
            onError = {}
        )
    }
}
