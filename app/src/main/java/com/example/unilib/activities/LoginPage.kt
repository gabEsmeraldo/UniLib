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
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        auth = FirebaseAuth.getInstance()

        val btnEntrar = findViewById<Button>(R.id.btnEnter)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val txtCriarConta = findViewById<TextView>(R.id.TextViewCreateAccount)
        val txtForgotPassword = findViewById<TextView>(R.id.TextViewForgotPassword)
        val btnReturn = findViewById<ImageView>(R.id.ImageViewReturn)
        val frameLayout = findViewById<FrameLayout>(R.id.FrameLayoutReturn)

        btnEntrar.setOnClickListener {
            val emailText = editEmail.text.toString().trim().lowercase()
            val passwordText = editPassword.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                DadosIncorretosModalHelper.show(this)
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, UserHomePage::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Erro ao entrar: E-mail ou senha incorretos.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao entrar: ${it.message}", Toast.LENGTH_LONG).show()
                }




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
