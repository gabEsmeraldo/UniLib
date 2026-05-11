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

object DevolucaoAtrasadaModalHelper {

    fun show(
        activity: Activity,
        nomeLivro: String,
        diasAtraso: Int,
        taxa: String
    ) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_devolucao_atrasada)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBackDevolucaoAtrasada =
            dialog.findViewById<View>(R.id.btnBackDevolucaoAtrasada)

        val btnConfirmarDevolucaoAtrasada =
            dialog.findViewById<Button>(R.id.btnConfirmarDevolucaoAtrasada)

        val tvTitle =
            dialog.findViewById<TextView>(R.id.tvDevolucaoAtrasadaTitle)

        val tvMessage =
            dialog.findViewById<TextView>(R.id.tvDevolucaoAtrasadaMessage)

        tvTitle.text = nomeLivro
        tvMessage.text = "Entrega $diasAtraso dias atrasada\numa taxa de $taxa será\ncobrada na devolução"

        btnBackDevolucaoAtrasada.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmarDevolucaoAtrasada.setOnClickListener {
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