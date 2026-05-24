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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.unilib.R
import com.example.unilib.repository.ReservationRepository

object ConfirmarEmprestimoModalHelper {

    /**
     * Exibe o modal onde o admin digita o código apresentado pelo usuário.
     * Se o código for válido, a reserva vira empréstimo ativo no Firestore.
     */
    fun show(
        activity: Activity,
        reservationId: String
    ) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_confirmar_emprestimo)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackConfirmarEmprestimo)
        val stateInput = dialog.findViewById<LinearLayout>(R.id.stateInputCodigo)
        val stateErro = dialog.findViewById<LinearLayout>(R.id.stateErroCodigo)
        val stateSuccess = dialog.findViewById<LinearLayout>(R.id.stateRetiradaConfirmada)

        val etCodigo = dialog.findViewById<EditText>(R.id.etCodigoRetirada)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarConfirmarEmprestimo)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarRetirada)
        val btnTentarNovamente = dialog.findViewById<Button>(R.id.btnTentarNovamenteCodigo)
        val btnFecharSucesso = dialog.findViewById<Button>(R.id.btnFecharRetiradaConfirmada)

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnTentarNovamente.setOnClickListener {
            stateErro.visibility = View.GONE
            stateSuccess.visibility = View.GONE
            stateInput.visibility = View.VISIBLE
            etCodigo.setText("")
            etCodigo.requestFocus()
        }

        btnFecharSucesso.setOnClickListener {
            dialog.dismiss()

            // Recarrega a tela atual para a reserva sair da lista de pendentes.
            activity.recreate()
        }

        btnConfirmar.setOnClickListener {
            val codigoDigitado = etCodigo.text.toString().trim()

            if (codigoDigitado.isBlank()) {
                Toast.makeText(
                    activity,
                    "Digite o código de retirada.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnConfirmar.isEnabled = false
            btnCancelar.isEnabled = false

            /**
             * Valida o código no Firestore.
             * Se estiver correto, cria o documento em user_lents_book
             * e atualiza a reserva para APPROVED.
             */
            ReservationRepository.approveReservationWithCode(
                reservationId = reservationId,
                typedCode = codigoDigitado,
                onSuccess = {
                    btnConfirmar.isEnabled = true
                    btnCancelar.isEnabled = true

                    stateInput.visibility = View.GONE
                    stateErro.visibility = View.GONE
                    stateSuccess.visibility = View.VISIBLE
                },
                onError = {
                    btnConfirmar.isEnabled = true
                    btnCancelar.isEnabled = true

                    stateInput.visibility = View.GONE
                    stateSuccess.visibility = View.GONE
                    stateErro.visibility = View.VISIBLE
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