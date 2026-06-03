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

object EditarQuantidadeModalHelper {

    fun show(
        activity: Activity,
        currentTotal: Int,
        currentAvailable: Int,
        onConfirm: (Int) -> Unit
    ) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_editar_quantidade)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackEditarQuantidade)
        val tvTotal = dialog.findViewById<TextView>(R.id.tvQuantidadeAtual)
        val tvDisponivel = dialog.findViewById<TextView>(R.id.tvQuantidadeDisponivel)
        val btnDecrementar = dialog.findViewById<View>(R.id.btnDecrementarQuantidade)
        val btnIncrementar = dialog.findViewById<View>(R.id.btnIncrementarQuantidade)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarQuantidade)

        var count = currentTotal.coerceAtLeast(0)

        fun computedAvailable(): Int = maxOf(0, currentAvailable + (count - currentTotal))

        fun render() {
            tvTotal.text = "$count Total"
            tvDisponivel.text = "${computedAvailable()} Disponíveis"
        }
        render()

        btnDecrementar.setOnClickListener {
            if (count > 0) count -= 1
            render()
        }

        btnIncrementar.setOnClickListener {
            count += 1
            render()
        }

        btnBack.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            onConfirm(count)
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
