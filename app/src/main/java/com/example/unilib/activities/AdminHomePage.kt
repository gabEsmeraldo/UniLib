package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.FirebaseFirestoreLegacyRegistrar

class AdminHomePage : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var txtPendentes: TextView
    private lateinit var txtAtivos: TextView
    private lateinit var txtAcervo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_home_page)

        db = FirebaseFirestore.getInstance()

        txtPendentes = findViewById(R.id.CountPendentes)
        txtAtivos = findViewById(R.id.CountAtivos)
        txtAcervo = findViewById(R.id.CountAcervo)

        txtPendentes.text = "..."
        txtAtivos.text = "..."
        txtAcervo.text = "..."

        carregarEstatisticasDashboard()

        AdminNavBarHelper.setup(this, AdminNavTab.HOME)
        setupNavigation()
        setupBookCards()
    }

    private fun carregarEstatisticasDashboard() {
        carregarPendentes()
        carregarAtivos()
        carregarAcervo()
    }

    private fun carregarPendentes() {
        db.collection("user_reserves_book")
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener { documents ->
                txtPendentes.text = documents.size().toString()
            }
            .addOnFailureListener { e ->
                txtPendentes.text = "-"
                Toast.makeText(this, "Erro Pendentes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarAtivos() {
        db.collection("user_lents_book")
            .whereIn("status", listOf("ACTIVE", "Atrasado"))
            .get()
            .addOnSuccessListener { documents ->
                txtAtivos.text = documents.size().toString()
            }
            .addOnFailureListener { e ->
                txtAtivos.text = "-"
                Toast.makeText(this, "Erro Ativos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarAcervo() {
        db.collection("books")
            .get()
            .addOnSuccessListener { documents ->
                txtAcervo.text = documents.size().toString()
            }
            .addOnFailureListener { e ->
                txtAcervo.text = "-"
                Toast.makeText(this, "Erro Acervo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

    private fun setupBookCards() {
        findViewById<View>(R.id.cardAlgoritmosAdminHome)?.setOnClickListener {
            openBookDetails("Algoritmos e Estruturas de Dados", "blue")
        }

        findViewById<View>(R.id.cardCleanCodeAdminHome)?.setOnClickListener {
            openBookDetails("Clean Code", "green")
        }

        findViewById<View>(R.id.cardBancoDadosAdminHome)?.setOnClickListener {
            openBookDetails("Banco de Dados", "red")
        }
    }

    private fun openBookDetails(title: String, color: String) {
        val intent = Intent(this, admin_book_details::class.java)
        intent.putExtra("TITULO_LIVRO", title)
        intent.putExtra("BOOK_COLOR", color)
        intent.putExtra("ADMIN_NAV_TAB", AdminNavTab.HOME.name)
        startActivity(intent)
    }
}
