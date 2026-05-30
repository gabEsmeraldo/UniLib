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
import com.example.unilib.repository.LoanRepository
import com.google.firebase.firestore.DocumentSnapshot

class emprestimo_admin_page_ativos : AppCompatActivity() {

    private lateinit var ativosAdminContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emprestimos_admin_page_ativos)

        AdminNavBarHelper.setup(this, AdminNavTab.LOANS)

        ativosAdminContainer = findViewById(R.id.ativosAdminContainer)

        setupTabs()
    }

    override fun onResume() {
        super.onResume()
        carregarEmprestimosAtivos()
    }

    private fun setupTabs() {
        findViewById<LinearLayout>(R.id.tabPendentes)?.setOnClickListener {
            startActivity(Intent(this, emprestimo_admin_page::class.java))
            finish()
        }
    }

    /**
     * Busca no Firestore todos os empréstimos com status ACTIVE ou LATE.
     * Essa tela representa a aba "Ativos" do administrador.
     */
    private fun carregarEmprestimosAtivos() {
        ativosAdminContainer.removeAllViews()
        mostrarMensagem("Carregando empréstimos ativos...")

        LoanRepository.getOpenLoansForAdmin(
            onSuccess = { loans ->
                ativosAdminContainer.removeAllViews()

                if (loans.isEmpty()) {
                    mostrarMensagem("Nenhum empréstimo ativo")
                    return@getOpenLoansForAdmin
                }

                montarCardsEmprestimos(loans)
            },
            onError = { exception ->
                ativosAdminContainer.removeAllViews()
                mostrarMensagem("Erro ao carregar empréstimos")

                Toast.makeText(
                    this,
                    exception.message ?: "Erro ao buscar empréstimos.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    /**
     * Para cada empréstimo, atualiza o status se estiver atrasado,
     * busca o livro e o usuário relacionados, e cria um card dinâmico.
     */
    private fun montarCardsEmprestimos(loans: List<DocumentSnapshot>) {
        ativosAdminContainer.removeAllViews()

        loans.forEachIndexed { index, loanDocument ->
            LoanRepository.updateLoanStatusIfLate(loanDocument)

            LoanRepository.getBookFromLoan(
                loanDocument = loanDocument,
                onSuccess = { bookDocument ->
                    LoanRepository.getUserFromLoan(
                        loanDocument = loanDocument,
                        onSuccess = { userDocument ->
                            val card = criarCardEmprestimoAdmin(
                                loanDocument = loanDocument,
                                bookDocument = bookDocument,
                                userDocument = userDocument,
                                index = index
                            )

                            ativosAdminContainer.addView(card)
                        },
                        onError = {
                            val card = criarCardEmprestimoAdmin(
                                loanDocument = loanDocument,
                                bookDocument = bookDocument,
                                userDocument = null,
                                index = index
                            )

                            ativosAdminContainer.addView(card)
                        }
                    )
                },
                onError = {
                    val card = criarCardEmprestimoAdmin(
                        loanDocument = loanDocument,
                        bookDocument = null,
                        userDocument = null,
                        index = index
                    )

                    ativosAdminContainer.addView(card)
                }
            )
        }
    }

    /**
     * Cria o card visual de empréstimo ativo/atrasado para o admin.
     * O clique abre o modal de detalhes, já enviando o loanId real.
     */
    private fun criarCardEmprestimoAdmin(
        loanDocument: DocumentSnapshot,
        bookDocument: DocumentSnapshot?,
        userDocument: DocumentSnapshot?,
        index: Int
    ): LinearLayout {
        val title = bookDocument?.getString("title") ?: "Livro"
        val author = bookDocument?.getString("author") ?: "Autor não informado"
        val imageUrl = bookDocument?.getString("imageUrl") ?: ""

        val userName = userDocument?.getString("name")
            ?: userDocument?.getString("nome")
            ?: "Usuário"

        val cpf = userDocument?.getString("cpf") ?: ""
        val userInfo = if (cpf.isNotBlank()) {
            "$userName · CPF: $cpf"
        } else {
            userName
        }

        val endDate = loanDocument.getTimestamp("end_date")
        val lateDays = LoanRepository.calculateLateDays(endDate)
        val fineValue = LoanRepository.calculateFineValue(lateDays)
        val fineText = LoanRepository.formatCurrency(fineValue)
        val isLate = lateDays > 0L

        val status = if (isLate) {
            EmprestimoStatus.ATRASADO
        } else {
            EmprestimoStatus.ATIVO
        }

        val dataLabel = if (isLate) {
            "$lateDays dias de atraso"
        } else {
            "Emprestado até: ${LoanRepository.formatDate(endDate)}"
        }

        val statusInfo = if (isLate) {
            "Atrasado $lateDays dias · $fineText"
        } else {
            "Devolução: ${LoanRepository.formatDate(endDate)}"
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            isClickable = true
            isFocusable = true
            setBackgroundResource(
                if (isLate) R.drawable.card_border_red else R.drawable.card_border_green
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            setOnClickListener {
                DetalhesEmprestimoModalHelper.show(
                    activity = this@emprestimo_admin_page_ativos,
                    status = status,
                    bookTitle = title,
                    bookAuthor = author,
                    alunoName = userName,
                    dataLabel = dataLabel,
                    taxaAtual = fineText,
                    loanId = loanDocument.id,
                    imageUrl = imageUrl
                )
            }
        }

        val capa = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(72)).apply {
                marginEnd = dp(12)
            }

            setBackgroundResource(
                if (isLate) R.drawable.book_cover_red else R.drawable.book_cover_green
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
            text = statusInfo
            setTextColor(
                if (isLate) Color.parseColor("#B91C1C") else Color.parseColor("#166534")
            )
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
            text = if (isLate) "● Atrasado" else "● Ativo"
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
        infoColumn.addView(tvUserInfo)
        infoColumn.addView(tvStatusInfo)
        infoColumn.addView(tvBadge)

        card.addView(capa)
        card.addView(infoColumn)

        return card
    }

    private fun mostrarMensagem(mensagem: String) {
        ativosAdminContainer.removeAllViews()

        val textView = TextView(this).apply {
            text = mensagem
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 14f
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }

        ativosAdminContainer.addView(textView)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
