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
import com.example.unilib.R

object ReservaModalHelper {

    fun show(activity: Activity) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_reserva)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBackReserva = dialog.findViewById<View>(R.id.btnBackReserva)
        val btnConfirmarReserva = dialog.findViewById<Button>(R.id.btnConfirmarReserva)

        btnBackReserva.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmarReserva.setOnClickListener {
            dialog.dismiss()
            ReservaAtivaModalHelper.show(activity)
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