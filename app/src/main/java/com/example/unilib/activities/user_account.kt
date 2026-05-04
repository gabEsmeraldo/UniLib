package com.example.unilib.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R

class user_account : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_account)

        NavBarHelper.setup(this, NavTab.ACCOUNT)
    }
}
