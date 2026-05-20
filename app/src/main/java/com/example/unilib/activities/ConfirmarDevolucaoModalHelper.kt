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
import android.widget.Toast
import com.example.unilib.R
import com.example.unilib.repository.LoanRepository

object ConfirmarDevolucaoModalHelper {

    /**
     * Exibe o modal de confirmação de devolução.
     * Quando recebe loanId, a devolução é confirmada no Firestore.
     */
    fun show(
        activity: Activity,
        bookTitle: String = "Livro",
        loanId: String? = null
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

        tvTitle.text = "Devolução"
        tvMessage.text = "Confirmar devolução do livro $bookTitle?"

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            if (loanId.isNullOrBlank()) {
                Toast.makeText(
                    activity,
                    "Empréstimo não encontrado para confirmar devolução.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            btnConfirmar.isEnabled = false
            btnCancelar.isEnabled = false

            /**
             * Confirma a devolução no Firestore:
             * - status vira RETURNED;
             * - returned_at recebe a data atual;
             * - multa final é calculada;
             * - available do livro aumenta em 1.
             */
            LoanRepository.confirmReturn(
                loanId = loanId,
                onSuccess = {
                    Toast.makeText(
                        activity,
                        "Devolução confirmada com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()

                    // Recarrega a tela atual para o empréstimo sair da lista de ativos.
                    activity.recreate()
                },
                onError = { exception ->
                    btnConfirmar.isEnabled = true
                    btnCancelar.isEnabled = true

                    Toast.makeText(
                        activity,
                        exception.message ?: "Erro ao confirmar devolução.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
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