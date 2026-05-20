package com.example.unilib.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.example.unilib.repository.ReservationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.max

class emprestimo_admin_page : AppCompatActivity() {

    private lateinit var pendentesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emprestimos_admin_page)

        AdminNavBarHelper.setup(this, AdminNavTab.LOANS)

        pendentesContainer = findViewById(R.id.pendentesContainer)

        setupTabs()
    }

    override fun onResume() {
        super.onResume()
        carregarReservasPendentes()
    }

    private fun setupTabs() {
        findViewById<LinearLayout>(R.id.tabAtivos)?.setOnClickListener {
            startActivity(Intent(this, emprestimo_admin_page_ativos::class.java))
            finish()
        }
    }

    /**
     * Busca no Firestore todas as reservas com status PENDING.
     * Essas reservas representam os empréstimos aguardando aprovação do admin.
     */
    private fun carregarReservasPendentes() {
        pendentesContainer.removeAllViews()
        mostrarMensagem("Carregando reservas pendentes...")

        ReservationRepository.getPendingReservationsForAdmin(
            onSuccess = { reservas ->
                pendentesContainer.removeAllViews()

                if (reservas.isEmpty()) {
                    mostrarMensagem("Nenhuma reserva pendente")
                    return@getPendingReservationsForAdmin
                }

                montarCardsPendentes(reservas)
            },
            onError = { exception ->
                pendentesContainer.removeAllViews()
                mostrarMensagem("Erro ao carregar reservas")

                Toast.makeText(
                    this,
                    exception.message ?: "Erro ao buscar reservas pendentes.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    /**
     * Para cada reserva, busca o livro e o usuário relacionados.
     * Depois cria um card dinâmico com dados reais do Firebase.
     */
    private fun montarCardsPendentes(reservas: List<DocumentSnapshot>) {
        pendentesContainer.removeAllViews()

        reservas.forEachIndexed { index, reservaDocument ->
            ReservationRepository.getBookFromReservation(
                reservationDocument = reservaDocument,
                onSuccess = { bookDocument ->
                    ReservationRepository.getUserFromReservation(
                        reservationDocument = reservaDocument,
                        onSuccess = { userDocument ->
                            val card = criarCardPendente(
                                reservaDocument = reservaDocument,
                                bookDocument = bookDocument,
                                userDocument = userDocument,
                                index = index
                            )

                            pendentesContainer.addView(card)
                        },
                        onError = {
                            val card = criarCardPendente(
                                reservaDocument = reservaDocument,
                                bookDocument = bookDocument,
                                userDocument = null,
                                index = index
                            )

                            pendentesContainer.addView(card)
                        }
                    )
                },
                onError = {
                    val card = criarCardPendente(
                        reservaDocument = reservaDocument,
                        bookDocument = null,
                        userDocument = null,
                        index = index
                    )

                    pendentesContainer.addView(card)
                }
            )
        }
    }

    /**
     * Cria o card de reserva pendente que aparece para o administrador.
     * O clique abre o modal de detalhes do empréstimo.
     */
    private fun criarCardPendente(
        reservaDocument: DocumentSnapshot,
        bookDocument: DocumentSnapshot?,
        userDocument: DocumentSnapshot?,
        index: Int
    ): LinearLayout {
        val title = bookDocument?.getString("title") ?: "Livro reservado"
        val author = bookDocument?.getString("author") ?: "Autor não informado"

        val userName = userDocument?.getString("name")
            ?: userDocument?.getString("nome")
            ?: "Usuário"

        val cpf = userDocument?.getString("cpf") ?: ""
        val userInfo = if (cpf.isNotBlank()) {
            "$userName · CPF: $cpf"
        } else {
            userName
        }

        val reserveTimestamp = reservaDocument.getTimestamp("reserve_timestamp")
        val dataLabel = tempoDesdeReserva(reserveTimestamp)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            isClickable = true
            isFocusable = true
            setBackgroundResource(R.drawable.card_border_blue)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            setOnClickListener {
                DetalhesEmprestimoModalHelper.show(
                    activity = this@emprestimo_admin_page,
                    status = EmprestimoStatus.PENDENTE,
                    bookTitle = title,
                    bookAuthor = author,
                    alunoName = userName,
                    dataLabel = dataLabel,
                    reservationId = reservaDocument.id
                )
            }
        }

        val capa = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(72)).apply {
                marginEnd = dp(12)
            }

            setBackgroundResource(getBookCover(index))
        }

        val icon = ImageView(this).apply {
            setImageResource(R.drawable.lent_books_icon)
            setColorFilter(Color.WHITE)

            layoutParams = FrameLayout.LayoutParams(
                dp(24),
                dp(24),
                Gravity.CENTER
            )
        }

        capa.addView(icon)

        val infoColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val tvTitle = TextView(this).apply {
            text = title
            setTextColor(Color.BLACK)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            maxLines = 1
        }

        val tvUserInfo = TextView(this).apply {
            text = userInfo
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 14f
            maxLines = 1

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(2)
            }
        }

        val tvStatusInfo = TextView(this).apply {
            text = dataLabel
            setTextColor(Color.parseColor("#0D5DA3"))
            textSize = 14f
            maxLines = 1

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        val tvBadge = TextView(this).apply {
            text = "● Pendente"
            setTextColor(Color.parseColor("#0D5DA3"))
            setBackgroundResource(R.drawable.badge_pendente)
            textSize = 12f
            setPadding(dp(8), dp(3), dp(8), dp(3))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }

        infoColumn.addView(tvTitle)
        infoColumn.addView(tvUserInfo)
        infoColumn.addView(tvStatusInfo)
        infoColumn.addView(tvBadge)

        card.addView(capa)
        card.addView(infoColumn)

        return card
    }

    private fun mostrarMensagem(mensagem: String) {
        pendentesContainer.removeAllViews()

        val textView = TextView(this).apply {
            text = mensagem
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 14f
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }

        pendentesContainer.addView(textView)
    }

    private fun tempoDesdeReserva(timestamp: Timestamp?): String {
        val reservaMillis = timestamp?.toDate()?.time ?: return "Reservado agora"
        val diffMillis = System.currentTimeMillis() - reservaMillis
        val minutos = max(0L, diffMillis / 60000L)

        return when {
            minutos <= 0L -> "Reservado agora"
            minutos == 1L -> "Reservado há 1 min"
            minutos < 60L -> "Reservado há $minutos min"
            else -> {
                val horas = minutos / 60L
                if (horas == 1L) {
                    "Reservado há 1 hora"
                } else {
                    "Reservado há $horas horas"
                }
            }
        }
    }

    private fun getBookCover(index: Int): Int {
        return when (index % 5) {
            0 -> R.drawable.book_cover_blue
            1 -> R.drawable.book_cover_green
            2 -> R.drawable.book_cover_red
            3 -> R.drawable.book_cover_orange
            else -> R.drawable.book_cover_blue
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
