package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.example.unilib.repository.BookRepository
import com.example.unilib.repository.ReservationRepository
import com.google.firebase.Timestamp

class AdminHomePage : AppCompatActivity() {

    private val bookRepository = BookRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_home_page)

        AdminNavBarHelper.setup(this, AdminNavTab.HOME)
        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadTopLentBooks()
        loadPendingApprovals()
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

    private fun loadPendingApprovals() {
        val container = findViewById<LinearLayout>(R.id.llPendingApprovals)
        container.removeAllViews()

        ReservationRepository.getPendingReservationsForAdmin(
            onSuccess = { reservations ->
                reservations.forEach { reservaDoc ->
                    ReservationRepository.getBookFromReservation(
                        reservationDocument = reservaDoc,
                        onSuccess = { bookDoc ->
                            ReservationRepository.getUserFromReservation(
                                reservationDocument = reservaDoc,
                                onSuccess = { userDoc ->
                                    val title = bookDoc?.getString("title") ?: "Livro reservado"
                                    val author = bookDoc?.getString("author") ?: ""
                                    val userName = userDoc?.getString("name")
                                        ?: userDoc?.getString("nome")
                                        ?: "Usuário"
                                    val cpf = userDoc?.getString("cpf") ?: ""
                                    val userInfo = if (cpf.isNotBlank()) "$userName · CPF: $cpf" else userName
                                    val timestamp = reservaDoc.getTimestamp("reserve_timestamp")
                                    val timeLabel = tempoDesdeReserva(timestamp)

                                    val card = LayoutInflater.from(this)
                                        .inflate(R.layout.admin_emprestimo_card_pendente, container, false)
                                    card.findViewById<TextView>(R.id.emprestimo_title).text = title
                                    card.findViewById<TextView>(R.id.emprestimo_user_info).text = userInfo
                                    card.findViewById<TextView>(R.id.emprestimo_status_info).text = timeLabel
                                    card.setOnClickListener {
                                        startActivity(Intent(this, emprestimo_admin_page::class.java))
                                    }
                                    container.addView(card)
                                },
                                onError = {
                                    val title = bookDoc?.getString("title") ?: "Livro reservado"
                                    val timestamp = reservaDoc.getTimestamp("reserve_timestamp")
                                    val timeLabel = tempoDesdeReserva(timestamp)

                                    val card = LayoutInflater.from(this)
                                        .inflate(R.layout.admin_emprestimo_card_pendente, container, false)
                                    card.findViewById<TextView>(R.id.emprestimo_title).text = title
                                    card.findViewById<TextView>(R.id.emprestimo_status_info).text = timeLabel
                                    card.setOnClickListener {
                                        startActivity(Intent(this, emprestimo_admin_page::class.java))
                                    }
                                    container.addView(card)
                                }
                            )
                        },
                        onError = {
                            val card = LayoutInflater.from(this)
                                .inflate(R.layout.admin_emprestimo_card_pendente, container, false)
                            card.setOnClickListener {
                                startActivity(Intent(this, emprestimo_admin_page::class.java))
                            }
                            container.addView(card)
                        }
                    )
                }
            },
            onError = {}
        )
    }

    private fun tempoDesdeReserva(timestamp: Timestamp?): String {
        val millis = timestamp?.toDate()?.time ?: return "Reservado agora"
        val minutes = maxOf(0L, (System.currentTimeMillis() - millis) / 60000L)
        return when {
            minutes <= 0L -> "Reservado agora"
            minutes == 1L -> "Reservado há 1 min"
            minutes < 60L -> "Reservado há $minutes min"
            else -> {
                val hours = minutes / 60L
                if (hours == 1L) "Reservado há 1 hora" else "Reservado há $hours horas"
            }
        }
    }

    private fun loadTopLentBooks() {
        val container = findViewById<LinearLayout>(R.id.llFeaturedBooks)
        container.removeAllViews()
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
                        val intent = Intent(this, admin_book_details::class.java)
                        intent.putExtra("TITULO_LIVRO", book.title)
                        intent.putExtra("BOOK_ID", book.id)
                        intent.putExtra("BOOK_COLOR", colorName)
                        intent.putExtra("ADMIN_NAV_TAB", AdminNavTab.HOME.name)
                        startActivity(intent)
                    }

                    container.addView(card)
                }
            },
            onError = {}
        )
    }
}
