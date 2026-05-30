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

object EditarCpfUsuarioModalHelper {

    fun show(activity: Activity, currentCpf: String = "", onConfirm: (String) -> Unit) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_editar_cpf_usuario)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackEditarCpfUsuario)
        val etCpf = dialog.findViewById<EditText>(R.id.etEditarCpfUsuario)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEditarCpfUsuario)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarCpfUsuario)

        etCpf.setText(currentCpf.filter { it.isDigit() })

        btnBack.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            val newCpf = etCpf.text.toString().trim()
            if (newCpf.isNotBlank()) {
                onConfirm(newCpf)
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
