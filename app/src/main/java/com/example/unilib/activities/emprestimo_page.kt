package com.example.unilib.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class emprestimo_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emprestimos_page)

        NavBarHelper.setup(this, NavTab.LOANS)

        setupDevolucaoAtrasadaModal()
    }

    private fun setupDevolucaoAtrasadaModal() {
        findViewById<View>(R.id.cardEmprestimoAtrasado)?.setOnClickListener {
            DevolucaoAtrasadaModalHelper.show(
                activity = this,
                nomeLivro = "Design Patterns",
                diasAtraso = 5,
                taxa = "R$2,50"
            )
        }
    }
}