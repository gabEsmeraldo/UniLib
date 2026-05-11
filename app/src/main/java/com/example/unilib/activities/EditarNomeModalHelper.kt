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

object EditarNomeModalHelper {

    fun show(activity: Activity, currentName: String = "") {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_editar_nome)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackEditarNome)
        val etNome = dialog.findViewById<EditText>(R.id.etEditarNome)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEditarNome)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarNome)

        etNome.setText(currentName)

        btnBack.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener { dialog.dismiss() }

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
