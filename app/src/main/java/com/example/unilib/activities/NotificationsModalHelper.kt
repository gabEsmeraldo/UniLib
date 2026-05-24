package com.example.unilib.activities

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.unilib.R
import com.example.unilib.repository.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

object NotificationsModalHelper {

    /**
     * Exibe o modal de notificações e carrega os dados reais do Firestore.
     * As notificações ficam em users/{uid}/notifications.
     */
    fun show(activity: Activity) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_notifications)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBackNotifications = dialog.findViewById<View>(R.id.btnBackNotifications)
        val btnMarkAsRead = dialog.findViewById<Button>(R.id.btnMarkAsRead)
        val notificationsContainer =
            dialog.findViewById<LinearLayout>(R.id.notificationsListContainer)

        btnBackNotifications.setOnClickListener {
            dialog.dismiss()
        }

        btnMarkAsRead.setOnClickListener {
            /**
             * Marca todas as notificações não lidas como lidas.
             * Depois fecha o modal.
             */
            NotificationRepository.markCurrentUserNotificationsAsRead(
                onSuccess = {
                    Toast.makeText(activity, "Notificações marcadas como lidas.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                },
                onError = { exception ->
                    Toast.makeText(
                        activity,
                        exception.message ?: "Erro ao marcar notificações.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }

        carregarNotificacoes(activity, notificationsContainer)

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

    /**
     * Busca as notificações reais do usuário logado e mostra apenas as não lidas.
     */
    private fun carregarNotificacoes(
        activity: Activity,
        container: LinearLayout
    ) {
        container.removeAllViews()
        mostrarMensagem(activity, container, "Carregando notificações...")

        NotificationRepository.getCurrentUserNotifications(
            onSuccess = { notifications ->
                container.removeAllViews()

                val unreadNotifications = notifications
                    .filter { it.getBoolean("read") != true }
                    .sortedByDescending {
                        it.getTimestamp("time_sent")?.toDate()?.time ?: 0L
                    }

                if (unreadNotifications.isEmpty()) {
                    mostrarMensagem(activity, container, "Nenhuma notificação não lida")
                    return@getCurrentUserNotifications
                }

                unreadNotifications.forEach { notification ->
                    container.addView(criarCardNotificacao(activity, notification))
                }
            },
            onError = { exception ->
                container.removeAllViews()
                mostrarMensagem(activity, container, "Erro ao carregar notificações")

                Toast.makeText(
                    activity,
                    exception.message ?: "Erro ao buscar notificações.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    /**
     * Cria o card visual de uma notificação usando content e time_sent do Firestore.
     */
    private fun criarCardNotificacao(
        activity: Activity,
        notification: DocumentSnapshot
    ): LinearLayout {
        val content = notification.getString("content") ?: "Notificação"
        val timeSent = notification.getTimestamp("time_sent")

        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_notification_item)
            setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 10))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(activity, 10)
            }
        }

        val tvContent = TextView(activity).apply {
            text = content
            setTextColor(Color.parseColor("#1E2D3D"))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setLineSpacing(dp(activity, 2).toFloat(), 1.0f)
        }

        val tvDate = TextView(activity).apply {
            text = formatNotificationDate(timeSent)
            setTextColor(Color.parseColor("#5B6B85"))
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            textAlignment = View.TEXT_ALIGNMENT_TEXT_END

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(activity, 8)
            }
        }

        card.addView(tvContent)
        card.addView(tvDate)

        return card
    }

    private fun mostrarMensagem(
        activity: Activity,
        container: LinearLayout,
        mensagem: String
    ) {
        container.removeAllViews()

        val textView = TextView(activity).apply {
            text = mensagem
            setTextColor(Color.parseColor("#5B6B85"))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(activity, 8), dp(activity, 16), dp(activity, 8), dp(activity, 16))
        }

        container.addView(textView)
    }

    private fun formatNotificationDate(timestamp: Timestamp?): String {
        if (timestamp == null) return "Agora"

        val now = System.currentTimeMillis()
        val sentMillis = timestamp.toDate().time
        val diffMillis = now - sentMillis
        val oneDay = 24L * 60L * 60L * 1000L

        return when {
            diffMillis < oneDay -> "Hoje"
            diffMillis < oneDay * 2 -> "Ontem"
            diffMillis < oneDay * 7 -> "${diffMillis / oneDay} dias atrás"
            else -> {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                formatter.format(timestamp.toDate())
            }
        }
    }

    private fun dp(activity: Activity, value: Int): Int {
        return (value * activity.resources.displayMetrics.density).toInt()
    }
}