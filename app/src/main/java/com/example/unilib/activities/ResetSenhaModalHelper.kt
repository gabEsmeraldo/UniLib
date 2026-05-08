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
import com.example.unilib.R

object ResetSenhaModalHelper {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun show(activity: Activity) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_reset_senha)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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

        val etCodigo = dialog.findViewById<EditText>(R.id.etResetCodigo)
        val btnVerificarCodigo = dialog.findViewById<Button>(R.id.btnVerificarCodigo)

        val btnConfirmarCodigoErro = dialog.findViewById<Button>(R.id.btnConfirmarCodigoErro)

        val etNovaSenha = dialog.findViewById<EditText>(R.id.etNovaSenha)
        val etConfirmarNovaSenha = dialog.findViewById<EditText>(R.id.etConfirmarNovaSenha)
        val btnSalvarNovaSenha = dialog.findViewById<Button>(R.id.btnSalvarNovaSenha)

        val btnFechar = dialog.findViewById<Button>(R.id.btnFecharResetSenha)

        // Header back: previous step or dismiss on Step 1
        btnBack.setOnClickListener {
            when {
                stepCodigo.visibility == View.VISIBLE -> showOnly(stepEmail)
                stepCodigoErro.visibility == View.VISIBLE -> showOnly(stepEmail)
                stepNovaSenha.visibility == View.VISIBLE -> showOnly(stepCodigo)
                stepSucesso.visibility == View.VISIBLE -> dialog.dismiss()
                else -> dialog.dismiss()
            }
        }

        btnEnviarCodigo.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty() || !emailRegex.matches(email)) {
                dialog.dismiss()
                EmailInexistenteModalHelper.show(activity)
            } else {
                etCodigo.setText("")
                showOnly(stepCodigo)
            }
        }

        btnVerificarCodigo.setOnClickListener {
            val code = etCodigo.text.toString().trim()
            if (code.isNotEmpty()) {
                etNovaSenha.setText("")
                etConfirmarNovaSenha.setText("")
                showOnly(stepNovaSenha)
            } else {
                showOnly(stepCodigoErro)
            }
        }

        btnConfirmarCodigoErro.setOnClickListener {
            etCodigo.setText("")
            showOnly(stepCodigo)
        }

        btnSalvarNovaSenha.setOnClickListener {
            val nova = etNovaSenha.text.toString()
            val confirmar = etConfirmarNovaSenha.text.toString()
            if (nova.isNotEmpty() && confirmar.isNotEmpty()) {
                showOnly(stepSucesso)
            } else {
                showOnly(stepCodigoErro)
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
