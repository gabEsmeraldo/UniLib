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
import android.widget.ImageView
import android.widget.Toast
import com.example.unilib.R

object EditarImagemModalHelper {

    private var activeDialog: Dialog? = null
    private var selectedBase64: String = ""
    private var confirmCallback: ((String) -> Unit)? = null

    fun show(
        activity: Activity,
        initialBase64: String = "",
        onPickImage: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        val dialog = Dialog(activity)
        activeDialog = dialog
        selectedBase64 = initialBase64
        confirmCallback = onConfirm

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_editar_imagem)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnDismissListener { activeDialog = null; confirmCallback = null }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackEditarImagem)
        val placeholder = dialog.findViewById<View>(R.id.imagePlaceholder)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEditarImagem)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarImagem)

        if (initialBase64.isNotEmpty()) showPreviewInDialog(dialog, initialBase64)

        placeholder.setOnClickListener { onPickImage() }

        btnBack.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            if (selectedBase64.isEmpty()) {
                Toast.makeText(activity, "Selecione uma imagem primeiro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onConfirm(selectedBase64)
            dialog.dismiss()
        }

        dialog.setOwnerActivity(activity)
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

    fun setSelectedImage(base64: String) {
        selectedBase64 = base64
        val dialog = activeDialog ?: return
        val owner = dialog.ownerActivity
        if (owner == null || owner.isFinishing || owner.isDestroyed) return
        if (!dialog.isShowing) return
        showPreviewInDialog(dialog, base64)
    }

    fun dismiss() {
        activeDialog?.dismiss()
        activeDialog = null
    }

    private fun showPreviewInDialog(dialog: Dialog, base64: String) {
        val bitmap = ImageUtils.base64ToBitmap(base64) ?: return
        dialog.findViewById<View>(R.id.placeholderHint)?.visibility = View.GONE
        dialog.findViewById<ImageView>(R.id.ivPreviewImage)?.apply {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
        }
    }
}
