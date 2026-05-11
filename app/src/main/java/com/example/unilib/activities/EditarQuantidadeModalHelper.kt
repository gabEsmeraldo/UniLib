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
        currentQuantity: String = "0",
        availableLabel: String? = null
    ) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_editar_quantidade)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackEditarQuantidade)
        val tvAtual = dialog.findViewById<TextView>(R.id.tvQuantidadeAtual)
        val btnDecrementar = dialog.findViewById<View>(R.id.btnDecrementarQuantidade)
        val btnIncrementar = dialog.findViewById<View>(R.id.btnIncrementarQuantidade)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarQuantidade)

        // Stepper state — start from the current quantity, fall back to 0 on parse failure.
        var count = currentQuantity.toIntOrNull()?.coerceAtLeast(0) ?: 0
        // The "<n> Disponíveis" label tracks the live count. If a custom label was passed,
        // honor it for the initial render but switch to the dynamic format on first tap.
        var customLabelInUse = availableLabel != null

        fun render() {
            tvAtual.text = if (customLabelInUse) availableLabel else "$count Disponíveis"
        }
        render()

        btnDecrementar.setOnClickListener {
            customLabelInUse = false
            if (count > 0) count -= 1
            render()
        }

        btnIncrementar.setOnClickListener {
            customLabelInUse = false
            count += 1
            render()
        }

        btnBack.setOnClickListener { dialog.dismiss() }
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
