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

enum class EmprestimoStatus { PENDENTE, ATIVO, ATRASADO }

object DetalhesEmprestimoModalHelper {

    /**
     * Exibe os detalhes de uma reserva/empréstimo para o admin.
     * Quando for PENDENTE, recebe reservationId para aprovar a retirada pelo código.
     */
    fun show(
        activity: Activity,
        status: EmprestimoStatus,
        bookTitle: String = "Livro",
        bookAuthor: String = "Autor não informado",
        alunoName: String = "Usuário",
        dataLabel: String? = null,
        taxaAtual: String? = null,
        reservationId: String? = null,
        loanId: String? = null
    ) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_detalhes_emprestimo)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBack = dialog.findViewById<View>(R.id.btnBackDetalhesEmprestimo)
        val tvTitulo = dialog.findViewById<TextView>(R.id.tvDetalhesTitulo)
        val tvAutor = dialog.findViewById<TextView>(R.id.tvDetalhesAutor)
        val tvBadge = dialog.findViewById<TextView>(R.id.tvDetalhesStatusBadge)
        val tvAluno = dialog.findViewById<TextView>(R.id.tvDetalhesAluno)
        val tvData = dialog.findViewById<TextView>(R.id.tvDetalhesData)
        val tvTaxa = dialog.findViewById<TextView>(R.id.tvDetalhesTaxa)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarDetalhes)
        val btnAcao = dialog.findViewById<Button>(R.id.btnAcaoDetalhes)

        tvTitulo.text = bookTitle
        tvAutor.text = bookAuthor
        tvAluno.text = "Aluno: $alunoName"

        when (status) {
            EmprestimoStatus.PENDENTE -> {
                tvBadge.text = "● Pendente"
                tvBadge.setTextColor(Color.parseColor("#0D5DA3"))
                tvData.text = dataLabel ?: "Reservado agora"
                tvTaxa.visibility = View.GONE
                btnAcao.text = "Aprovar retirada"
            }

            EmprestimoStatus.ATIVO -> {
                tvBadge.text = "● Ativo"
                tvBadge.setTextColor(Color.parseColor("#16A34A"))
                tvData.text = dataLabel ?: "Empréstimo ativo"
                tvTaxa.visibility = View.VISIBLE
                tvTaxa.text = "Taxa de devolução atual: ${taxaAtual ?: "R$ 0,00"}"
                btnAcao.text = "Devolução"
            }

            EmprestimoStatus.ATRASADO -> {
                tvBadge.text = "● Atrasado"
                tvBadge.setTextColor(Color.parseColor("#B91C1C"))
                tvData.text = dataLabel ?: "Empréstimo atrasado"
                tvTaxa.visibility = View.VISIBLE
                tvTaxa.text = "Taxa de devolução atual: ${taxaAtual ?: "R$ 0,00"}"
                btnAcao.text = "Confirmar Devolução"
            }
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnAcao.setOnClickListener {
            dialog.dismiss()

            when (status) {
                EmprestimoStatus.PENDENTE -> {
                    if (reservationId != null) {
                        ConfirmarEmprestimoModalHelper.show(
                            activity = activity,
                            reservationId = reservationId
                        )
                    }
                }

                EmprestimoStatus.ATIVO,
                EmprestimoStatus.ATRASADO -> {
                    if (loanId != null) {
                        ConfirmarDevolucaoModalHelper.show(
                            activity = activity,
                            bookTitle = bookTitle,
                            loanId = loanId
                        )
                    } else {
                        ConfirmarDevolucaoModalHelper.show(
                            activity = activity,
                            bookTitle = bookTitle
                        )
                    }
                }
            }
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