package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class emprestimo_admin_page_ativos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emprestimos_admin_page_ativos)

        AdminNavBarHelper.setup(this, AdminNavTab.LOANS)
        findViewById<View>(R.id.tabPendentes)?.setOnClickListener {
            startActivity(Intent(this, emprestimo_admin_page::class.java))
            finish()
        }

        findViewById<View>(R.id.cardAtivoOne)?.setOnClickListener {
            DetalhesEmprestimoModalHelper.show(this, EmprestimoStatus.ATIVO)
        }
        findViewById<View>(R.id.cardAtrasadoOne)?.setOnClickListener {
            DetalhesEmprestimoModalHelper.show(this, EmprestimoStatus.ATRASADO)
        }
        findViewById<View>(R.id.cardAtivoTwo)?.setOnClickListener {
            DetalhesEmprestimoModalHelper.show(this, EmprestimoStatus.ATIVO)
        }
    }
}
