package com.example.unilib.activities

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
import com.example.unilib.repository.LoanRepository
import com.example.unilib.repository.ReservationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.ceil
import kotlin.math.max

class emprestimo_page : AppCompatActivity() {

    private lateinit var ativosContainer: LinearLayout
    private lateinit var reservasContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emprestimos_page)

        NavBarHelper.setup(this, NavTab.LOANS)

        ativosContainer = findViewById(R.id.ativosContainer)
        reservasContainer = findViewById(R.id.reservasEmprestimosContainer)

        
    }

    override fun onResume() {
        super.onResume()
        carregarEmprestimosAbertos()
        carregarReservasAtivas()
        LoanRepository.createDueSoonNotificationsForCurrentUser()
    }

    /**
     * Busca empréstimos ativos/atrasados reais do usuário logado.
     * Remove o antigo comportamento mockado do card "Design Patterns".
     */
    private fun carregarEmprestimosAbertos() {
        ativosContainer.removeAllViews()
        mostrarMensagem(ativosContainer, "Carregando empréstimos...")

        LoanRepository.getCurrentUserOpenLoans(
            onSuccess = { loans ->
                ativosContainer.removeAllViews()

                if (loans.isEmpty()) {
                    mostrarMensagem(ativosContainer, "Nenhum empréstimo ativo")
                    return@getCurrentUserOpenLoans
                }

                montarCardsEmprestimos(loans)
            },
            onError = { exception ->
                ativosContainer.removeAllViews()
                mostrarMensagem(ativosContainer, "Erro ao carregar empréstimos")

                Toast.makeText(
                    this,
                    exception.message ?: "Erro ao buscar empréstimos.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    /**
     * Busca reservas pendentes reais do usuário logado.
     */
    private fun carregarReservasAtivas() {
        reservasContainer.removeAllViews()
        mostrarMensagem(reservasContainer, "Carregando reservas...")

        ReservationRepository.getCurrentUserPendingReservations(
            onSuccess = { reservas ->
                reservasContainer.removeAllViews()

                if (reservas.isEmpty()) {
                    mostrarMensagem(reservasContainer, "Nenhuma reserva ativa")
                    return@getCurrentUserPendingReservations
                }

                montarCardsReservas(reservas)
            },
            onError = { exception ->
                reservasContainer.removeAllViews()
                mostrarMensagem(reservasContainer, "Erro ao carregar reservas")

                Toast.makeText(
                    this,
                    exception.message ?: "Erro ao buscar reservas.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    /**
     * Para cada empréstimo, busca o livro relacionado e cria um card dinâmico.
     */
    private fun montarCardsEmprestimos(loans: List<DocumentSnapshot>) {
        ativosContainer.removeAllViews()

        loans.forEachIndexed { index, loanDocument ->
            LoanRepository.updateLoanStatusIfLate(loanDocument)

            LoanRepository.getBookFromLoan(
                loanDocument = loanDocument,
                onSuccess = { bookDocument ->
                    val card = criarCardEmprestimo(
                        loanDocument = loanDocument,
                        bookDocument = bookDocument,
                        index = index
                    )

                    ativosContainer.addView(card)
                },
                onError = {
                    val card = criarCardEmprestimo(
                        loanDocument = loanDocument,
                        bookDocument = null,
                        index = index
                    )

                    ativosContainer.addView(card)
                }
            )
        }
    }

    /**
     * Para cada reserva, busca o livro relacionado e cria um card dinâmico.
     */
    private fun montarCardsReservas(reservas: List<DocumentSnapshot>) {
        reservasContainer.removeAllViews()

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
                },
                onError = {
                    val card = criarCardReserva(
                        reservaDocument = reservaDocument,
                        bookDocument = null,
                        index = index
                    )

                    reservasContainer.addView(card)
                }
            )
        }
    }

    /**
     * Cria card de empréstimo ativo ou atrasado com dados reais.
     * Se o empréstimo estiver atrasado, o clique abre o modal de multa.
     */
    private fun criarCardEmprestimo(
        loanDocument: DocumentSnapshot,
        bookDocument: DocumentSnapshot?,
        index: Int
    ): LinearLayout {
        val title = bookDocument?.getString("title") ?: "Livro"
        val author = bookDocument?.getString("author") ?: "Autor não informado"
        val imageUrl = bookDocument?.getString("imageUrl") ?: ""
        val endDate = loanDocument.getTimestamp("end_date")

        val lateDays = LoanRepository.calculateLateDays(endDate)
        val fineValue = LoanRepository.calculateFineValue(lateDays)
        val fineText = LoanRepository.formatCurrency(fineValue)
        val isLate = lateDays > 0L

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            isClickable = true
            isFocusable = true
            setBackgroundResource(
                if (isLate) R.drawable.card_border_orange else R.drawable.card_border_blue
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            setOnClickListener {
                if (isLate) {
                    DevolucaoAtrasadaModalHelper.show(
                        activity = this@emprestimo_page,
                        nomeLivro = title,
                        diasAtraso = lateDays.toInt(),
                        taxa = fineText
                    )
                }
            }
        }

        val capa = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(72)).apply {
                marginEnd = dp(12)
            }

            setBackgroundResource(
                if (isLate) R.drawable.book_cover_orange else R.drawable.book_cover_blue
            )
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
        ImageUtils.loadBookCoverImage(capa, imageUrl)

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

        val tvAuthor = TextView(this).apply {
            text = author
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

        val tvInfo = TextView(this).apply {
            text = if (isLate) {
                "Atrasado — $lateDays dias"
            } else {
                "Devolução: ${LoanRepository.formatDate(endDate)}"
            }

            setTextColor(
                if (isLate) Color.parseColor("#B91C1C") else Color.parseColor("#1474C4")
            )

            textSize = 14f

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        val tvBadge = TextView(this).apply {
            text = if (isLate) {
                "● Atrasado · $fineText"
            } else {
                "● Ativo"
            }

            setTextColor(
                if (isLate) Color.parseColor("#B91C1C") else Color.parseColor("#166534")
            )

            setBackgroundResource(
                if (isLate) R.drawable.badge_atrasado else R.drawable.badge_ativo
            )

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
        infoColumn.addView(tvAuthor)
        infoColumn.addView(tvInfo)
        infoColumn.addView(tvBadge)

        card.addView(capa)
        card.addView(infoColumn)

        return card
    }

    /**
     * Cria card de reserva ativa com código real.
     */
    private fun criarCardReserva(
        reservaDocument: DocumentSnapshot,
        bookDocument: DocumentSnapshot?,
        index: Int
    ): LinearLayout {
        val title = bookDocument?.getString("title") ?: "Livro reservado"
        val author = bookDocument?.getString("author") ?: "Autor não informado"
        val imageUrl = bookDocument?.getString("imageUrl") ?: ""
        val expiresAt = reservaDocument.getTimestamp("expires_at")
        val tempoRestante = calcularTempoRestante(expiresAt)
        val codigo = reservaDocument.getString("reservation_code") ?: "------"

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            isClickable = true
            isFocusable = true
            setBackgroundResource(R.drawable.card_border_green)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            setOnClickListener {
                ReservaAtivaModalHelper.show(
                    activity = this@emprestimo_page,
                    nomeLivro = title,
                    tempoRestante = tempoRestante,
                    codigo = codigo
                )
            }
        }

        val capa = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(72)).apply {
                marginEnd = dp(12)
            }

            setBackgroundResource(R.drawable.book_cover_green)
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
        ImageUtils.loadBookCoverImage(capa, imageUrl)

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

        val tvAuthor = TextView(this).apply {
            text = author
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

        val tvExpira = TextView(this).apply {
            text = "Expira em: $tempoRestante"
            setTextColor(Color.parseColor("#B91C1C"))
            textSize = 14f

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        val tvBadge = TextView(this).apply {
            text = "● Reservado"
            setTextColor(Color.parseColor("#166534"))
            setBackgroundResource(R.drawable.badge_reservado)
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
        infoColumn.addView(tvAuthor)
        infoColumn.addView(tvExpira)
        infoColumn.addView(tvBadge)

        card.addView(capa)
        card.addView(infoColumn)

        return card
    }

    private fun mostrarMensagem(container: LinearLayout, mensagem: String) {
        container.removeAllViews()

        val textView = TextView(this).apply {
            text = mensagem
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 14f
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }

        container.addView(textView)
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
