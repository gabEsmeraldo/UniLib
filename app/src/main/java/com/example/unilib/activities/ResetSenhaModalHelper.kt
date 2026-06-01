package com.example.unilib.activities

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.unilib.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ResetSenhaModalHelper {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun show(activity: Activity) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_reset_senha)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val stepEmail = dialog.findViewById<LinearLayout>(R.id.stepEmail)
        val stepCodigo = dialog.findViewById<LinearLayout>(R.id.stepCodigo)
        val stepCodigoErro = dialog.findViewById<LinearLayout>(R.id.stepCodigoErro)
        val stepNovaSenha = dialog.findViewById<LinearLayout>(R.id.stepNovaSenha)
        val stepSucesso = dialog.findViewById<LinearLayout>(R.id.stepSucesso)

        val steps = listOf(stepEmail, stepCodigo, stepCodigoErro, stepNovaSenha, stepSucesso)

        fun showOnly(view: View) {
            steps.forEach { it.visibility = if (it === view) View.VISIBLE else View.GONE }
        }

        val btnBack = dialog.findViewById<ImageView>(R.id.btnBackResetSenha)
        val etEmail = dialog.findViewById<EditText>(R.id.etResetEmail)
        val btnEnviarCodigo = dialog.findViewById<Button>(R.id.btnEnviarCodigo)
        val btnFechar = dialog.findViewById<Button>(R.id.btnFecharResetSenha)

        val etCodigo = dialog.findViewById<EditText>(R.id.etResetCodigo)
        val btnVerificarCodigo = dialog.findViewById<Button>(R.id.btnVerificarCodigo)
        val btnConfirmarCodigoErro = dialog.findViewById<Button>(R.id.btnConfirmarCodigoErro)
        val etNovaSenha = dialog.findViewById<EditText>(R.id.etNovaSenha)
        val etConfirmarNovaSenha = dialog.findViewById<EditText>(R.id.etConfirmarNovaSenha)
        val btnSalvarNovaSenha = dialog.findViewById<Button>(R.id.btnSalvarNovaSenha)

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        btnEnviarCodigo.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase()

            if (email.isEmpty() || !emailRegex.matches(email)) {
                Toast.makeText(activity, "Por favor, digite um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        dialog.dismiss()
                        EmailInexistenteModalHelper.show(activity)
                    } else {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Avança a interface direto para o painel de sucesso
                                    showOnly(stepSucesso)
                                } else {
                                    dialog.dismiss()
                                    Toast.makeText(activity, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnFechar.setOnClickListener { dialog.dismiss() }

        showOnly(stepEmail)
        dialog.show()

        dialog.window?.let { window ->
            val width = (activity.resources.displayMetrics.widthPixels * 0.88).toInt()
            window.setLayout(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window.setGravity(Gravity.CENTER)

            val params = window.attributes
            params.dimAmount = 0.55f
            window.attributes = params

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }
}