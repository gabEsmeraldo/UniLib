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

    fun show(
        activity: Activity,
        status: EmprestimoStatus,
        bookTitle: String = "Algoritmos e Estruturas de Dados",
        bookAuthor: String = "Thomas H. Cormen et al.",
        alunoName: String = "Narak Silva",
        dataLabel: String? = null,
        taxaAtual: String? = null
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
                tvData.text = dataLabel ?: "Reservado em: 05/05/2026"
                tvTaxa.visibility = View.GONE
                btnAcao.text = "Aprovar retirada"
            }
            EmprestimoStatus.ATIVO -> {
                tvBadge.text = "● Ativo"
                tvBadge.setTextColor(Color.parseColor("#16A34A"))
                tvData.text = dataLabel ?: "Emprestado até: 27/03/2026"
                tvTaxa.visibility = View.VISIBLE
                tvTaxa.text = "Taxa de devoluçao atual: ${taxaAtual ?: "R$ 0,00"}"
                btnAcao.text = "Devolução"
            }
            EmprestimoStatus.ATRASADO -> {
                tvBadge.text = "● Atrasado"
                tvBadge.setTextColor(Color.parseColor("#B91C1C"))
                tvData.text = dataLabel ?: "5 dias de atraso"
                tvTaxa.visibility = View.VISIBLE
                tvTaxa.text = "Taxa de devoluçao atual: ${taxaAtual ?: "R$ 10,00"}"
                btnAcao.text = "Confirmar Devolução"
            }
        }

        btnBack.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnAcao.setOnClickListener {
            dialog.dismiss()
            when (status) {
                EmprestimoStatus.PENDENTE -> ConfirmarEmprestimoModalHelper.show(activity)
                EmprestimoStatus.ATIVO,
                EmprestimoStatus.ATRASADO -> ConfirmarDevolucaoModalHelper.show(
                    activity,
                    bookTitle = bookTitle
                )
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
