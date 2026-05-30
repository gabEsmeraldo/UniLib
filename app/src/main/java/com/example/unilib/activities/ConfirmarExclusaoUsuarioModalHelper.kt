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
import android.widget.TextView
import com.example.unilib.R

object ConfirmarExclusaoUsuarioModalHelper {

    fun show(activity: Activity, userName: String, onConfirm: () -> Unit) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_confirmar_exclusao_usuario)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackExcluirUsuario)
        val tvNome = dialog.findViewById<TextView>(R.id.tvNomeUsuarioExclusao)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarExcluirUsuario)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarExcluirUsuario)

        tvNome.text = userName

        btnBack.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener { onConfirm(); dialog.dismiss() }

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
