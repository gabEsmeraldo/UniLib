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
        val txtCriarConta = findViewById<TextView>(R.id.TextViewCreateAccount)
        val btnReturn = findViewById<ImageView>(R.id.ImageViewReturn)
        val frameLayout = findViewById<FrameLayout>(R.id.FrameLayoutReturn)

        btnEntrar.setOnClickListener {
            val targetActivity = if (editEmail.text.toString().contains("admin", ignoreCase = true)) {
                AdminHomePage::class.java
            } else {
                UserHomePage::class.java
            }
            val intent = Intent(this, targetActivity)
            startActivity(intent)
            finish()
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
