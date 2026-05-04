package com.example.unilib.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class admin_book_details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_book_details)

        AdminNavBarHelper.setup(this, AdminNavTab.NONE)
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        intent.getStringExtra("TITULO_LIVRO")?.let { title ->
            findViewById<TextView>(R.id.tvBookTitle)?.text = title
        }
    }
}
