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
import com.example.unilib.R

object EditarEmailUsuarioModalHelper {

    fun show(activity: Activity, currentEmail: String = "", onConfirm: (String) -> Unit) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_editar_email_usuario)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackEditarEmailUsuario)
        val etEmail = dialog.findViewById<EditText>(R.id.etEditarEmailUsuario)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEditarEmailUsuario)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarEmailUsuario)

        etEmail.setText(currentEmail)

        btnBack.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            val newEmail = etEmail.text.toString().trim()
            if (newEmail.isNotBlank()) {
                onConfirm(newEmail)
                dialog.dismiss()
            }
        }

        dialog.show()
        dialog.window?.let { window ->
            val width = (activity.resources.displayMetrics.widthPixels * 0.88).toInt()
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
            val params = window.attributes
            params.dimAmount = 0.55f
            window.attributes = params
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }
}
