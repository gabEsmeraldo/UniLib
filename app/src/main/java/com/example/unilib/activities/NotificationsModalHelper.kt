package com.example.unilib.activities

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import com.example.unilib.R

object NotificationsModalHelper {

    fun show(activity: Activity) {
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.modal_notifications)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnMarkAsRead = dialog.findViewById<Button>(R.id.btnMarkAsRead)

        btnMarkAsRead.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.let { window ->
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            window.setGravity(Gravity.BOTTOM)

            val params = window.attributes
            params.dimAmount = 0.55f
            window.attributes = params

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }
}