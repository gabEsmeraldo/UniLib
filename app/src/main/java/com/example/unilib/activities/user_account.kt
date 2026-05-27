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
import com.example.unilib.repository.LoanRepository
import com.example.unilib.repository.ReservationRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

class user_account : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var txtIniciais: TextView
    private lateinit var txtNome: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtCpf: TextView
    private lateinit var btnSair: View

    private lateinit var reservasContainer: LinearLayout
    private lateinit var returnedLoansContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_account)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        txtIniciais = findViewById(R.id.iniciaisUsuario)
        txtNome = findViewById(R.id.nomeUsuario)
        txtEmail = findViewById(R.id.emailUsuario)
        txtCpf = findViewById(R.id.cpfUsuario)
        btnSair = findViewById(R.id.btnSair)

        NavBarHelper.setup(this, NavTab.ACCOUNT)

        reservasContainer = findViewById(R.id.reservasContainer)
        returnedLoansContainer = findViewById(R.id.llReturnedLoans)

        setupAccountActions()
        setupNotificationsButton()
        esconderEmprestimosMockados()
        carregarDadosDoPerfil()
        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        carregarReservasAtivas()
        carregarEmprestimosDevolvidos()
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
        // mock cards removed from layout; nothing to hide
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

    private fun carregarEmprestimosDevolvidos() {
        returnedLoansContainer.removeAllViews()

        LoanRepository.getCurrentUserReturnedLoans(
            onSuccess = { loans ->
                returnedLoansContainer.removeAllViews()

                val limited = loans
                    .sortedByDescending { it.getTimestamp("returned_at")?.toDate()?.time ?: 0L }
                    .take(10)

                if (limited.isEmpty()) {
                    val tv = TextView(this).apply {
                        text = "Nenhum empréstimo devolvido"
                        setTextColor(Color.parseColor("#9BAAC0"))
                        textSize = 13f
                        setPadding(dp(18), dp(8), dp(18), dp(8))
                    }
                    returnedLoansContainer.addView(tv)
                    return@getCurrentUserReturnedLoans
                }

                limited.forEachIndexed { index, loanDoc ->
                    LoanRepository.getBookFromLoan(
                        loanDocument = loanDoc,
                        onSuccess = { bookDoc ->
                            val title = bookDoc?.getString("title") ?: "Livro devolvido"
                            val author = bookDoc?.getString("author") ?: ""
                            val returnedAt = loanDoc.getTimestamp("returned_at")
                            val dateLabel = if (returnedAt != null)
                                "Devolvido em ${LoanRepository.formatDate(returnedAt)}"
                            else "Devolvido"

                            val card = LinearLayout(this).apply {
                                orientation = LinearLayout.HORIZONTAL
                                isClickable = false
                                gravity = android.view.Gravity.CENTER_VERTICAL
                                setPadding(dp(12), dp(12), dp(12), dp(12))
                                setBackgroundResource(R.drawable.bg_card_white)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    marginStart = dp(18)
                                    marginEnd = dp(18)
                                    bottomMargin = dp(10)
                                }
                            }

                            val capa = FrameLayout(this).apply {
                                layoutParams = LinearLayout.LayoutParams(dp(56), dp(72))
                                setBackgroundResource(getBookBackground(index))
                            }
                            val emoji = TextView(this).apply {
                                text = getBookEmoji(index)
                                textSize = 24f
                                gravity = android.view.Gravity.CENTER
                                layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    android.view.Gravity.CENTER
                                )
                            }
                            capa.addView(emoji)

                            val info = LinearLayout(this).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                ).apply { marginStart = dp(12) }
                            }
                            val tvTitle = TextView(this).apply {
                                text = title
                                setTextColor(Color.parseColor("#1E2D3D"))
                                textSize = 14f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                maxLines = 1
                            }
                            val tvDate = TextView(this).apply {
                                text = dateLabel
                                setTextColor(Color.parseColor("#9BAAC0"))
                                textSize = 12f
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply { topMargin = dp(4) }
                            }
                            if (author.isNotBlank()) {
                                val tvAuthor = TextView(this).apply {
                                    text = author
                                    setTextColor(Color.parseColor("#5C6B82"))
                                    textSize = 12f
                                }
                                info.addView(tvTitle)
                                info.addView(tvAuthor)
                                info.addView(tvDate)
                            } else {
                                info.addView(tvTitle)
                                info.addView(tvDate)
                            }

                            card.addView(capa)
                            card.addView(info)

                            returnedLoansContainer.addView(card)
                        },
                        onError = {}
                    )
                }
            },
            onError = {}
        )
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

    private fun carregarDadosDoPerfil() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nomeCompleto = document.getString("nome")
                        val email = document.getString("email")
                        val cpf = "CPF: ${document.getString("cpf")}"

                        txtNome.text = nomeCompleto ?: "Nome não disponível"
                        txtEmail.text = email ?: "E-mail não disponível"
                        txtCpf.text = if (cpf.isNotEmpty()) {
                            "CPF: ${formatarCpf(cpf)}"
                        } else {
                            "CPF não cadastrado"
                        }



                        if (!nomeCompleto.isNullOrEmpty()) {
                            txtIniciais.text = gerarIniciais(nomeCompleto)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            txtNome.text = "Usuário não autenticado"
        }
    }

    private fun gerarIniciais(nome: String): String {
        val palavras = nome.trim().split("\\s+".toRegex())

        val palavrasValidas = palavras.filter { it.isNotEmpty() }

        if (palavrasValidas.isEmpty()) return ""

        val primeiraLetra = palavrasValidas.first().substring(0, 1)

        val iniciais = if (palavrasValidas.size > 1) {
            val ultimaLetra = palavrasValidas.last().substring(0, 1)
            "$primeiraLetra$ultimaLetra"
        } else {
            primeiraLetra
        }
        return iniciais.map { caractere ->
            if (caractere in 'a'..'z') caractere - 32 else caractere
        }.joinToString("")
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        btnSair.setOnClickListener {
            auth.signOut()

            Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun formatarCpf(cpfBruto: String): String {
        val apenasNumeros = cpfBruto.replace("\\D".toRegex(), "")

        if (apenasNumeros.length != 11) {
            return cpfBruto
        }

        val bloco1 = apenasNumeros.substring(0, 3)
        val bloco2 = apenasNumeros.substring(3, 6)
        val bloco3 = apenasNumeros.substring(6, 9)
        val digitos = apenasNumeros.substring(9, 11)

        return "$bloco1.$bloco2.$bloco3-$digitos"
    }

}
