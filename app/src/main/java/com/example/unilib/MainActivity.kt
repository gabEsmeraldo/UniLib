package com.example.unilib

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.unilib.fragments.ContaFragment
import com.example.unilib.fragments.EmprestimosFragment
import com.example.unilib.fragments.HomeFragment
import com.example.unilib.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    private var currentNavId: Int = R.id.nav_home

    private val activeColor by lazy { ContextCompat.getColor(this, R.color.nav_active) }
    private val inactiveColor by lazy { ContextCompat.getColor(this, R.color.nav_inactive) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()

        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            updateNavSelection(R.id.nav_home)
        }
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.nav_home).setOnClickListener {
            if (currentNavId != R.id.nav_home) {
                loadFragment(HomeFragment())
                updateNavSelection(R.id.nav_home)
            }
        }
        findViewById<View>(R.id.nav_search).setOnClickListener {
            if (currentNavId != R.id.nav_search) {
                loadFragment(SearchFragment())
                updateNavSelection(R.id.nav_search)
            }
        }
        findViewById<View>(R.id.nav_emprestimos).setOnClickListener {
            if (currentNavId != R.id.nav_emprestimos) {
                loadFragment(EmprestimosFragment())
                updateNavSelection(R.id.nav_emprestimos)
            }
        }
        findViewById<View>(R.id.nav_conta).setOnClickListener {
            if (currentNavId != R.id.nav_conta) {
                loadFragment(ContaFragment())
                updateNavSelection(R.id.nav_conta)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateNavSelection(selectedId: Int) {
        currentNavId = selectedId

        // Reset all nav items
        resetNavItem(R.id.nav_home, R.id.nav_home_icon, R.id.nav_home_text)
        resetNavItem(R.id.nav_search, R.id.nav_search_icon, R.id.nav_search_text)
        resetNavItem(R.id.nav_emprestimos, R.id.nav_emprestimos_icon, R.id.nav_emprestimos_text)
        resetNavItem(R.id.nav_conta, R.id.nav_conta_icon, R.id.nav_conta_text)

        // Activate selected nav item
        when (selectedId) {
            R.id.nav_home -> activateNavItem(R.id.nav_home, R.id.nav_home_icon, R.id.nav_home_text)
            R.id.nav_search -> activateNavItem(R.id.nav_search, R.id.nav_search_icon, R.id.nav_search_text)
            R.id.nav_emprestimos -> activateNavItem(R.id.nav_emprestimos, R.id.nav_emprestimos_icon, R.id.nav_emprestimos_text)
            R.id.nav_conta -> activateNavItem(R.id.nav_conta, R.id.nav_conta_icon, R.id.nav_conta_text)
        }
    }

    private fun resetNavItem(containerId: Int, iconId: Int, textId: Int) {
        findViewById<LinearLayout>(containerId).background = null
        findViewById<ImageView>(iconId).setColorFilter(inactiveColor)
        findViewById<TextView>(textId).setTextColor(inactiveColor)
    }

    private fun activateNavItem(containerId: Int, iconId: Int, textId: Int) {
        findViewById<LinearLayout>(containerId).setBackgroundResource(R.drawable.bg_nav_active)
        findViewById<ImageView>(iconId).setColorFilter(activeColor)
        findViewById<TextView>(textId).setTextColor(activeColor)
    }
}
