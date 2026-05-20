package com.example.unilib.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
import kotlin.math.ceil
import kotlin.math.max

class user_account : AppCompatActivity() {

    private lateinit var reservasContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_account)

        NavBarHelper.setup(this, NavTab.ACCOUNT)

        reservasContainer = findViewById(R.id.reservasContainer)

        setupAccountActions()
        setupNotificationsButton()
        esconderEmprestimosMockados()
        carregarReservasAtivas()
    }

    override fun onResume() {
        super.onResume()
        carregarReservasAtivas()
    }

    private fun setupNotificationsButton() {
        findViewById<FrameLayout>(R.id.btnNotifications).setOnClickListener {
            NotificationsModalHelper.show(this)
        }
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

    /**
     * Remove visualmente os empréstimos fixos antigos.
     * Eles serão substituídos depois pelos empréstimos reais vindos do Firebase.
     */
    private fun esconderEmprestimosMockados() {
        findViewById<View>(R.id.cardEmprestimoAlgoritmos)?.visibility = View.GONE
        findViewById<View>(R.id.cardEmprestimoDesenvolvimento)?.visibility = View.GONE
    }

    /**
     * Busca as reservas PENDING do usuário logado no Firestore.
     * Cada reserva encontrada gera um card dinâmico na tela Conta.
     */
    private fun carregarReservasAtivas() {
        reservasContainer.removeAllViews()
        mostrarMensagemReservas("Carregando reservas...")

        ReservationRepository.getCurrentUserPendingReservations(
            onSuccess = { reservas ->
                reservasContainer.removeAllViews()

                if (reservas.isEmpty()) {
                    mostrarMensagemReservas("Nenhuma reserva ativa")
                    return@getCurrentUserPendingReservations
                }

                montarCardsDeReservas(reservas)
            },
            onError = { exception ->
                reservasContainer.removeAllViews()
                mostrarMensagemReservas("Erro ao carregar reservas")

                Toast.makeText(
                    this,
                    exception.message ?: "Erro ao buscar reservas.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    /**
     * Para cada documento de reserva, busca o livro relacionado em books/{bookId}
     * e cria o card com título, autor, tempo restante e código real.
     */
    private fun montarCardsDeReservas(reservas: List<DocumentSnapshot>) {
        reservasContainer.removeAllViews()

        var pendentes = reservas.size

        reservas.forEachIndexed { index, reservaDocument ->
            ReservationRepository.getBookFromReservation(
                reservationDocument = reservaDocument,
                onSuccess = { bookDocument ->
                    val card = criarCardReserva(
                        reservaDocument = reservaDocument,
                        bookDocument = bookDocument,
                        index = index
                    )

                    reservasContainer.addView(card)

                    pendentes--
                    if (pendentes == 0 && reservasContainer.childCount == 0) {
                        mostrarMensagemReservas("Nenhuma reserva ativa")
                    }
                },
                onError = {
                    pendentes--
                    if (pendentes == 0 && reservasContainer.childCount == 0) {
                        mostrarMensagemReservas("Nenhuma reserva ativa")
                    }
                }
            )
        }
    }

    /**
     * Cria um card de reserva em código, usando o mesmo estilo visual dos cards antigos.
     */
    private fun criarCardReserva(
        reservaDocument: DocumentSnapshot,
        bookDocument: DocumentSnapshot?,
        index: Int
    ): LinearLayout {
        val title = bookDocument?.getString("title") ?: "Livro reservado"
        val author = bookDocument?.getString("author") ?: "Autor não informado"
        val codigo = reservaDocument.getString("reservation_code") ?: "------"
        val expiresAt = reservaDocument.getTimestamp("expires_at")
        val tempoRestante = calcularTempoRestante(expiresAt)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(dp(110), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dp(12)
            }

            setOnClickListener {
                ReservaAtivaModalHelper.show(
                    activity = this@user_account,
                    nomeLivro = title,
                    tempoRestante = tempoRestante,
                    codigo = codigo
                )
            }
        }

        val capa = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(110), dp(155))
            setBackgroundResource(getBookBackground(index))
        }

        val emoji = TextView(this).apply {
            text = getBookEmoji(index)
            textSize = 34f
            gravity = Gravity.CENTER

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }

        val badge = TextView(this).apply {
            text = "Reservado"
            setTextColor(Color.WHITE)
            textSize = 9f
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_avail_tag)
            setPadding(dp(6), dp(2), dp(6), dp(2))

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.START
            ).apply {
                marginStart = dp(6)
                bottomMargin = dp(6)
            }
        }

        capa.addView(emoji)
        capa.addView(badge)

        val tvTitulo = TextView(this).apply {
            text = title
            setTextColor(Color.parseColor("#1E2D3D"))
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            maxLines = 1

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        }

        val tvAutor = TextView(this).apply {
            text = author
            setTextColor(Color.parseColor("#9BAAC0"))
            textSize = 11f
            maxLines = 1
        }

        val tvTempo = TextView(this).apply {
            text = "Expira em $tempoRestante"
            setTextColor(Color.parseColor("#1474C4"))
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            maxLines = 1

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(2)
            }
        }

        card.addView(capa)
        card.addView(tvTitulo)
        card.addView(tvAutor)
        card.addView(tvTempo)

        return card
    }

    private fun mostrarMensagemReservas(mensagem: String) {
        reservasContainer.removeAllViews()

        val textView = TextView(this).apply {
            text = mensagem
            setTextColor(Color.parseColor("#9BAAC0"))
            textSize = 13f
            setPadding(dp(18), dp(12), dp(18), dp(12))
        }

        reservasContainer.addView(textView)
    }

    private fun calcularTempoRestante(expiresAt: Timestamp?): String {
        val expiresMillis = expiresAt?.toDate()?.time ?: return "0 minutos"
        val diffMillis = expiresMillis - System.currentTimeMillis()
        val minutos = max(0L, ceil(diffMillis / 60000.0).toLong())

        return when {
            minutos <= 0L -> "0 minutos"
            minutos == 1L -> "1 minuto"
            else -> "$minutos minutos"
        }
    }

    private fun getBookBackground(index: Int): Int {
        return when (index % 5) {
            0 -> R.drawable.bg_book_blue
            1 -> R.drawable.bg_book_green
            2 -> R.drawable.bg_book_purple
            3 -> R.drawable.bg_book_red
            else -> R.drawable.bg_book_gray
        }
    }

    private fun getBookEmoji(index: Int): String {
        return when (index % 5) {
            0 -> "📘"
            1 -> "📗"
            2 -> "📙"
            3 -> "📕"
            else -> "📓"
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}