package com.example.unilib.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.example.unilib.repository.BookRepository

class admin_book_details : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var bookColor: String = "blue"

    private val bookRepository = BookRepository()
    private var currentBookId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_book_details)

        val activeTab = intent.getStringExtra("ADMIN_NAV_TAB")
            ?.let { runCatching { AdminNavTab.valueOf(it) }.getOrNull() }
            ?: AdminNavTab.HOME
        AdminNavBarHelper.setup(this, activeTab)
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        bookColor = intent.getStringExtra("BOOK_COLOR") ?: "blue"

        loadBookFromFirestore()
    }

    private fun loadBookFromFirestore() {
        val bookId = intent.getStringExtra("BOOK_ID")
        val title = intent.getStringExtra("TITULO_LIVRO")

        when {
            !bookId.isNullOrBlank() -> {
                currentBookId = bookId
                db.collection("books").document(bookId).get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            showErrorAndClose("Livro não encontrado.")
                            return@addOnSuccessListener
                        }
                        onBookLoaded(document)
                    }
                    .addOnFailureListener { showErrorAndClose(it.message ?: "Erro ao carregar livro.") }
            }

            !title.isNullOrBlank() -> {
                db.collection("books").whereEqualTo("title", title).limit(1).get()
                    .addOnSuccessListener { result ->
                        val document = result.documents.firstOrNull()
                        if (document == null) {
                            showErrorAndClose("Livro não encontrado.")
                            return@addOnSuccessListener
                        }
                        onBookLoaded(document)
                    }
                    .addOnFailureListener { showErrorAndClose(it.message ?: "Erro ao carregar livro.") }
            }

            else -> showErrorAndClose("Nenhum livro foi informado.")
        }
    }

    private fun onBookLoaded(document: DocumentSnapshot) {
        val title = document.getString("title") ?: ""
        val author = document.getString("author") ?: ""
        val isbn = document.get("isbn")?.toString() ?: ""
        val synopsis = document.getString("synopsis") ?: ""
        val quantity = getLong(document, "quantity")
        val available = getLong(document, "available")
        val borrowed = (quantity - available).coerceAtLeast(0L)
        val reserved = getLong(document, "reserved")

        renderBook(title, author, isbn, quantity, available, borrowed, reserved, synopsis)
        applyBookColor(bookColor)
        wireEditModals(title, author, synopsis, quantity, available)
    }

    private fun renderBook(
        title: String, author: String, isbn: String,
        total: Long, available: Long, borrowed: Long, reserved: Long, synopsis: String
    ) {
        findViewById<TextView>(R.id.tvBookTitle)?.text = title
        findViewById<TextView>(R.id.tvBookAuthor)?.text = author
        findViewById<TextView>(R.id.tvIsbn)?.text = "ISBN: $isbn"
        findViewById<TextView>(R.id.tvTotal)?.text = total.toString()
        findViewById<TextView>(R.id.tvDisponiveis)?.text = available.toString()
        findViewById<TextView>(R.id.tvEmprestados)?.text = borrowed.toString()
        findViewById<TextView>(R.id.tvReservados)?.text = reserved.toString()
        findViewById<TextView>(R.id.tvSinopseDisplay)?.text = synopsis
    }

    private fun wireEditModals(
        title: String, author: String, synopsis: String,
        total: Long, available: Long
    ) {
        findViewById<View>(R.id.btnEditarNome)?.setOnClickListener {
            EditarNomeModalHelper.show(this, title)
        }
        findViewById<View>(R.id.btnEditarAutor)?.setOnClickListener {
            EditarAutorModalHelper.show(this, author)
        }
        findViewById<View>(R.id.btnEditarImagem)?.setOnClickListener {
            EditarImagemModalHelper.show(this)
        }
        findViewById<View>(R.id.btnEditarQuantidade)?.setOnClickListener {
            EditarQuantidadeModalHelper.show(
                this,
                currentQuantity = total.toString(),
                availableLabel = "$available Disponíveis"
            )
        }
        findViewById<View>(R.id.btnEditarSinopse)?.setOnClickListener {
            EditarSinopseModalHelper.show(this, synopsis)
        }
        findViewById<View>(R.id.btnEditarTags)?.setOnClickListener {
            EditarTagsModalHelper.show(this)
        }
        findViewById<View>(R.id.btnExcluirLivro)?.setOnClickListener {
            ConfirmarExclusaoModalHelper.show(
                this,
                title,
                onConfirm = {
                    bookRepository.deleteBook(
                        bookId = currentBookId,
                        onSuccess = {
                            Toast.makeText(this, "Livro excluído com sucesso!", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onError = { exception ->
                            Toast.makeText(this, "Erro ao excluir: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }

    private fun applyBookColor(color: String) {
        val theme = colorThemes[color] ?: colorThemes.getValue("blue")
        findViewById<View>(R.id.bookCover)?.setBackgroundResource(theme.coverDrawable)
        findViewById<View>(R.id.headerBar)?.setBackgroundColor(Color.parseColor(theme.darkColor))
        findViewById<View>(R.id.heroSection)?.setBackgroundColor(Color.parseColor(theme.heroColor))
    }

    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun getLong(document: DocumentSnapshot, field: String): Long {
        val value = document.get(field) ?: return 0L
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Float -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

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
    }
}
