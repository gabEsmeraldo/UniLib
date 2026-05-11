package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class CadastroPage : AppCompatActivity() {

    private companion object {
        // Mock list of e-mails that already have an account; used only for the
        // prototype "E-mail já cadastrado" modal trigger.
        val takenEmails = setOf("existente@unilib.com")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro_page)

        setupButtons()
    }

    private fun setupButtons() {
        val etEmail = findViewById<EditText>(R.id.etEmail)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnCadastrar).setOnClickListener {
            val typed = etEmail.text.toString().trim().lowercase()
            if (typed in takenEmails) {
                EmailJaCadastradoModalHelper.show(this)
            } else {
                startActivity(Intent(this, UserHomePage::class.java))
            }
        }

        findViewById<View>(R.id.btnFazerLogin).setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
        }
    }
}
