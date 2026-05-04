package com.example.unilib.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class emprestimo_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emprestimos_page)

        NavBarHelper.setup(this, NavTab.LOANS)
    }
}
