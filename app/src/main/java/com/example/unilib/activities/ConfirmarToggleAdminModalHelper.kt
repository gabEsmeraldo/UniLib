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

object ConfirmarToggleAdminModalHelper {

    fun show(activity: Activity, isCurrentlyAdmin: Boolean, onConfirm: () -> Unit) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_confirmar_toggle_admin)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackToggleAdmin)
        val tvStatus = dialog.findViewById<TextView>(R.id.tvToggleAdminStatus)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarToggleAdmin)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarToggleAdmin)

        val statusAtual = if (isCurrentlyAdmin) "Administrador" else "Usuário comum"
        val novoStatus = if (isCurrentlyAdmin) "Usuário comum" else "Administrador"
        tvStatus.text = "Status atual: $statusAtual → Alterar para: $novoStatus"

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
