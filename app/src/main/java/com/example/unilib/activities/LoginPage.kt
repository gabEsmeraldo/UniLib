package com.example.unilib.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.unilib.R
import android.widget.ImageView
import android.widget.FrameLayout

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        val btnEntrar = findViewById<Button>(R.id.btnEnter)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val txtCriarConta = findViewById<TextView>(R.id.TextViewCreateAccount)
        val txtForgotPassword = findViewById<TextView>(R.id.TextViewForgotPassword)
        val btnReturn = findViewById<ImageView>(R.id.ImageViewReturn)
        val frameLayout = findViewById<FrameLayout>(R.id.FrameLayoutReturn)

        btnEntrar.setOnClickListener {
            val emailText = editEmail.text.toString().trim()
            val passwordText = editPassword.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                DadosIncorretosModalHelper.show(this)
                return@setOnClickListener
            }

            // Mock-only shortcut until real authentication exists.
            val targetActivity = if (emailText.contains("admin", ignoreCase = true)) {
                AdminHomePage::class.java
            } else {
                UserHomePage::class.java
            }
            val intent = Intent(this, targetActivity)
            startActivity(intent)
            finish()
        }

        txtForgotPassword.setOnClickListener {
            ResetSenhaModalHelper.show(this)
        }

        txtCriarConta.setOnClickListener {
            val intent = Intent(this, CadastroPage::class.java)
            startActivity(intent)
        }

        btnReturn.setOnClickListener {
            val intent = Intent(this, StartPage::class.java)
            startActivity(intent)
            finish()
         }

        frameLayout.setOnClickListener {
            val intent = Intent(this, StartPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}
