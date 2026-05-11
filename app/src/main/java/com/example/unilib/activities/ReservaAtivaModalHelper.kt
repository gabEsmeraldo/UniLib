package com.example.unilib.activities

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.example.unilib.R

object ReservaAtivaModalHelper {

    fun show(
        activity: Activity,
        nomeLivro: String = "Livro reservado",
        tempoRestante: String = "17 minutos",
        codigo: String = "6XH-987"
    ) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_reserva_ativa)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarReservaAtiva)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarReservaAtiva)

        val tvTitle = dialog.findViewById<TextView>(R.id.tvReservaAtivaTitle)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvReservaAtivaMessage)
        val tvCodigo = dialog.findViewById<TextView>(R.id.tvReservaAtivaCodigo)

        tvTitle.text = nomeLivro
        tvMessage.text = "Apresente o código na biblioteca nos próximos $tempoRestante para receber o livro emprestado"
        tvCodigo.text = codigo

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
        }

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