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
import android.widget.LinearLayout
import com.example.unilib.R

object ConfirmarEmprestimoModalHelper {

    private const val VALID_CODE = "A4C123"

    fun show(activity: Activity) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_confirmar_emprestimo)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val stateInput = dialog.findViewById<LinearLayout>(R.id.stateInputCodigo)
        val stateErro = dialog.findViewById<LinearLayout>(R.id.stateErroCodigo)
        val stateSucesso = dialog.findViewById<LinearLayout>(R.id.stateRetiradaConfirmada)

        val states = listOf(stateInput, stateErro, stateSucesso)
        fun showOnly(view: View) {
            states.forEach { it.visibility = if (it === view) View.VISIBLE else View.GONE }
        }

        val btnBack = dialog.findViewById<View>(R.id.btnBackConfirmarEmprestimo)
        val etCodigo = dialog.findViewById<EditText>(R.id.etCodigoRetirada)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarConfirmarEmprestimo)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarRetirada)
        val btnTentar = dialog.findViewById<Button>(R.id.btnTentarNovamenteCodigo)
        val btnFechar = dialog.findViewById<Button>(R.id.btnFecharRetiradaConfirmada)

        btnBack.setOnClickListener {
            when {
                stateErro.visibility == View.VISIBLE -> showOnly(stateInput)
                stateSucesso.visibility == View.VISIBLE -> dialog.dismiss()
                else -> dialog.dismiss()
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnConfirmar.setOnClickListener {
            val typed = etCodigo.text.toString().trim().uppercase()
            if (typed == VALID_CODE) {
                showOnly(stateSucesso)
            } else {
                showOnly(stateErro)
            }
        }

        btnTentar.setOnClickListener {
            etCodigo.setText("")
            showOnly(stateInput)
        }

        btnFechar.setOnClickListener { dialog.dismiss() }

        showOnly(stateInput)
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
