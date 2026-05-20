package com.example.unilib.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.example.unilib.repository.ReservationRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class BookDetails : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var activeTab: NavTab = NavTab.NONE
    private var currentBookId: String? = null
    private var currentBookTitle: String? = null
    private var isCreatingReservation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_details)

        activeTab = intent.getStringExtra("NAV_TAB")
            ?.let { runCatching { NavTab.valueOf(it) }.getOrNull() }
            ?: NavTab.NONE

        NavBarHelper.setup(this, activeTab)

        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        setupActionButtons()
        loadBookFromFirestore()
    }

    /**
     * Busca o livro real no Firestore.
     * Prioriza BOOK_ID, pois é o identificador mais seguro.
     * Se a tela anterior ainda não enviar BOOK_ID, busca pelo campo title.
     */
    private fun loadBookFromFirestore() {
        val bookId = intent.getStringExtra("BOOK_ID")
        val title = intent.getStringExtra("TITULO_LIVRO")

        when {
            !bookId.isNullOrBlank() -> {
                db.collection("books")
                    .document(bookId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            showErrorAndClose("Livro não encontrado.")
                            return@addOnSuccessListener
                        }

                        currentBookId = document.id
                        currentBookTitle = document.getString("title")
                        renderBook(document)
                    }
                    .addOnFailureListener { exception ->
                        showErrorAndClose(exception.message ?: "Erro ao carregar livro.")
                    }
            }

            !title.isNullOrBlank() -> {
                db.collection("books")
                    .whereEqualTo("title", title)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        val document = result.documents.firstOrNull()

                        if (document == null) {
                            showErrorAndClose("Livro não encontrado no banco.")
                            return@addOnSuccessListener
                        }

                        currentBookId = document.id
                        currentBookTitle = document.getString("title")
                        renderBook(document)
                    }
                    .addOnFailureListener { exception ->
                        showErrorAndClose(exception.message ?: "Erro ao carregar livro.")
                    }
            }

            else -> {
                showErrorAndClose("Nenhum livro foi informado.")
            }
        }
    }

    /**
     * Preenche a tela com dados reais do documento books/{bookId}.
     */
    private fun renderBook(document: DocumentSnapshot) {
        val title = document.getString("title") ?: "Livro"
        val author = document.getString("author") ?: "Autor não informado"
        val isbn = document.get("isbn")?.toString() ?: ""
        val available = getLongField(document, "available") ?: 0L
        val quantity = getLongField(document, "quantity")
            ?: getLongField(document, "copies")
            ?: 0L
        val borrowed = (quantity - available).coerceAtLeast(0L)
        val location = document.getString("location") ?: "Não informado"
        val synopsis = document.getString("synopsis") ?: "Sinopse não informada"

        findViewById<TextView>(R.id.tvBookTitle).text = title
        findViewById<TextView>(R.id.tvBookAuthor).text = author
        findViewById<TextView>(R.id.tvIsbnHero).text = "ISBN: $isbn"
        findViewById<TextView>(R.id.tvIsbn).text = isbn
        findViewById<TextView>(R.id.tvDisponiveis).text = available.toString()
        findViewById<TextView>(R.id.tvEmprestados).text = borrowed.toString()
        findViewById<TextView>(R.id.tvLocalizacao).text = location
        findViewById<TextView>(R.id.tvSinopse).text = synopsis

        applyBookColor("blue")
    }

    private fun setupActionButtons() {
        val btnLocalizar = findViewById<LinearLayout>(R.id.btnLocalizar)
        val btnReservar = findViewById<LinearLayout>(R.id.btnReservar)

        btnLocalizar.setOnClickListener {
            val intent = Intent(this, map_page::class.java)
            intent.putExtra("NAV_TAB", activeTab.name)
            startActivity(intent)
            finish()
        }

        btnReservar.setOnClickListener {
            if (currentBookId.isNullOrBlank() && currentBookTitle.isNullOrBlank()) {
                Toast.makeText(this, "Livro ainda não carregado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ReservaModalHelper.show(
                activity = this,
                onConfirm = {
                    createReservation()
                }
            )
        }
    }

    /**
     * Cria a reserva real somente após o usuário confirmar no modal.
     * Se existir BOOK_ID, usa o ID do documento. Caso contrário, usa o título carregado.
     */
    private fun createReservation() {
        if (isCreatingReservation) {
            return
        }

        isCreatingReservation = true

        Toast.makeText(this, "Criando reserva...", Toast.LENGTH_SHORT).show()

        val bookId = currentBookId
        val title = currentBookTitle

        if (!bookId.isNullOrBlank()) {
            ReservationRepository.createReservationByBookId(
                bookId = bookId,
                onSuccess = { result ->
                    onReservationCreated(result)
                },
                onError = { exception ->
                    onReservationError(exception)
                }
            )
            return
        }

        if (!title.isNullOrBlank()) {
            ReservationRepository.createReservationByBookTitle(
                bookTitle = title,
                onSuccess = { result ->
                    onReservationCreated(result)
                },
                onError = { exception ->
                    onReservationError(exception)
                }
            )
            return
        }

        isCreatingReservation = false
        Toast.makeText(this, "Livro não encontrado.", Toast.LENGTH_LONG).show()
    }

    private fun onReservationCreated(result: ReservationRepository.ReservationCreatedResult) {
        isCreatingReservation = false

        ReservaAtivaModalHelper.show(
            activity = this,
            nomeLivro = result.bookTitle,
            tempoRestante = "30 minutos",
            codigo = result.reservationCode
        )

        loadBookFromFirestore()
    }

    private fun onReservationError(exception: Exception) {
        isCreatingReservation = false

        Toast.makeText(
            this,
            exception.message ?: "Erro ao criar reserva.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun applyBookColor(color: String) {
        val theme = colorThemes[color] ?: colorThemes.getValue("blue")

        findViewById<View>(R.id.bookCover).setBackgroundResource(theme.coverDrawable)
        findViewById<View>(R.id.headerBar).setBackgroundColor(Color.parseColor(theme.darkColor))
        findViewById<View>(R.id.heroSection).setBackgroundColor(Color.parseColor(theme.heroColor))
    }

    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun getLongField(document: DocumentSnapshot, field: String): Long? {
        val value = document.get(field) ?: return null

        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Float -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
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