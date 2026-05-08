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

object ConfirmarDevolucaoModalHelper {

    fun show(
        activity: Activity,
        bookTitle: String = "Devolução",
        dataDevolucao: String = "27/03/2026"
    ) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_confirmar_devolucao)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackConfirmarDevolucao)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvConfirmarDevolucaoTitle)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvConfirmarDevolucaoMessage)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarDevolucao)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarDevolucao)

        tvTitle.text = bookTitle
        tvMessage.text = "Confirmar devolução do livro em $dataDevolucao?"

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
