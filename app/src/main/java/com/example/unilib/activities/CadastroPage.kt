package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroPage : AppCompatActivity() {

    private companion object {
        val takenEmails = setOf("existente@unilib.com")
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Declarando os campos no escopo da classe para usar em qualquer método
    private lateinit var editNome: EditText
    private lateinit var editCPF: EditText
    private lateinit var editSenha: EditText
    private lateinit var editConfirmarSenha: EditText
    private lateinit var editEmail: EditText
    private lateinit var btnCadastrar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro_page)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        editNome = findViewById(R.id.etNomeCompleto)
        editCPF = findViewById(R.id.etCPF)
        editSenha = findViewById(R.id.etSenha)
        editConfirmarSenha = findViewById(R.id.etConfirmeSenha)
        editEmail = findViewById(R.id.etEmail)
        btnCadastrar = findViewById(R.id.btnCadastrar)


        setupNavigationButtons()


        btnCadastrar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim().lowercase()
            val senha = editSenha.text.toString().trim()
            val confirmaSenha = editConfirmarSenha.text.toString().trim()
            val cpf = editCPF.text.toString().trim()


            if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
                Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (email in takenEmails) {
                EmailJaCadastradoModalHelper.show(this)
                return@setOnClickListener
            }


            if (senha != confirmaSenha) {
                Toast.makeText(this, "As senhas devem ser iguais", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (senha.length < 6) {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {

                            val user = hashMapOf(
                                "uid" to userId,
                                "nome" to nome,
                                "cpf" to cpf,
                                "email" to email
                            )


                            db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Conta criada com sucesso", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, UserHomePage::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Erro ao salvar dados de perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
    private fun setupNavigationButtons() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnFazerLogin).setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }
    }
}