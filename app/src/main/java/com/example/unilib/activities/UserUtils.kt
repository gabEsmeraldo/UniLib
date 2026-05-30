package com.example.unilib.activities

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView

fun gerarIniciais(nome: String): String {
    val palavras = nome.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    if (palavras.isEmpty()) return ""
    val primeira = palavras.first().substring(0, 1).uppercase()
    return if (palavras.size > 1) "$primeira${palavras.last().substring(0, 1).uppercase()}" else primeira
}

fun formatarCpf(cpfBruto: String): String {
    val digits = cpfBruto.filter { it.isDigit() }
    return if (digits.length == 11) {
        "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9)}"
    } else {
        cpfBruto
    }
}

fun applyUserAvatar(ivPhoto: ImageView, tvInitials: TextView, nome: String, photoBase64: String) {
    if (photoBase64.isNotEmpty()) {
        val bitmap = ImageUtils.base64ToBitmap(photoBase64)
        if (bitmap != null) {
            ivPhoto.setImageBitmap(bitmap)
            ivPhoto.clipToOutline = true
            ivPhoto.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            ivPhoto.visibility = View.VISIBLE
            tvInitials.visibility = View.GONE
            return
        }
    }
    ivPhoto.visibility = View.GONE
    tvInitials.visibility = View.VISIBLE
    tvInitials.text = gerarIniciais(nome)
}
