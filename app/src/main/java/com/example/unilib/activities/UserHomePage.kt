package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHomePage : AppCompatActivity() {

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
        setupBookCards()
        setupNotificationsButton()
        setupChatButton()
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

    private fun setupBookCards() {
        findViewById<View>(R.id.cardAlgoritmosHome).setOnClickListener {
            openBookDetails("Algoritmos e Estruturas de Dados", "blue")
        }

        findViewById<View>(R.id.cardCleanCodeHome).setOnClickListener {
            openBookDetails("Clean Code", "green")
        }

        findViewById<View>(R.id.cardEngenhariaHome).setOnClickListener {
            openBookDetails("Engenharia de Software", "purple")
        }

        findViewById<View>(R.id.cardIaHome).setOnClickListener {
            openBookDetails("Inteligência Artificial", "red")
        }

        findViewById<View>(R.id.cardBancoDadosHome).setOnClickListener {
            openBookDetails("Banco de Dados", "gray")
        }
    }

    private fun openBookDetails(title: String, color: String) {
        val intent = Intent(this, BookDetails::class.java)
        intent.putExtra("TITULO_LIVRO", title)
        intent.putExtra("BOOK_COLOR", color)
        intent.putExtra("NAV_TAB", NavTab.HOME.name)
        startActivity(intent)
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
}