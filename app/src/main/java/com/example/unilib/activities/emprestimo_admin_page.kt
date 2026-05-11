package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class emprestimo_admin_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emprestimos_admin_page)

        AdminNavBarHelper.setup(this, AdminNavTab.LOANS)
        findViewById<View>(R.id.tabAtivos)?.setOnClickListener {
            startActivity(Intent(this, emprestimo_admin_page_ativos::class.java))
            finish()
        }

        findViewById<View>(R.id.cardPendenteOne)?.setOnClickListener {
            DetalhesEmprestimoModalHelper.show(this, EmprestimoStatus.PENDENTE)
        }
        findViewById<View>(R.id.cardPendenteTwo)?.setOnClickListener {
            DetalhesEmprestimoModalHelper.show(this, EmprestimoStatus.PENDENTE)
        }
    }
}
