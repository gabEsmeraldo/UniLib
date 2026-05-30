package com.example.unilib.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.example.unilib.repository.UserRepository
import com.google.firebase.firestore.DocumentSnapshot

class AdminUsersPage : AppCompatActivity() {

    private lateinit var usersContainer: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var etSearch: EditText

    private val allUsers = mutableListOf<DocumentSnapshot>()
    private var lastVisible: DocumentSnapshot? = null
    private var isLoading = false
    private var allLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_users_page)

        AdminNavBarHelper.setup(this, AdminNavTab.USERS)

        usersContainer = findViewById(R.id.usersContainer)
        tvEmpty = findViewById(R.id.tvEmptyUsers)
        scrollView = findViewById(R.id.scrollUsers)
        etSearch = findViewById(R.id.etSearchUsers)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterAndRender(s?.toString() ?: "")
            }
        })

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val child = scrollView.getChildAt(0) ?: return@setOnScrollChangeListener
            val diff = child.bottom - (scrollView.height + scrollY)
            if (diff <= dp(200) && !isLoading && !allLoaded) {
                loadUsers(reset = false)
            }
        }

        loadUsers(reset = true)
    }

    private fun loadUsers(reset: Boolean) {
        if (isLoading) return
        isLoading = true

        if (reset) {
            allUsers.clear()
            lastVisible = null
            allLoaded = false
            filterAndRender("") // clears dynamic cards, resets empty state
        }

        val onSuccess: (List<DocumentSnapshot>) -> Unit = { docs ->
            isLoading = false
            allUsers.addAll(docs)
            if (docs.isNotEmpty()) lastVisible = docs.last()
            if (docs.size < 20) allLoaded = true
            filterAndRender(etSearch.text?.toString() ?: "")
        }

        val onError: (Exception) -> Unit = { e ->
            isLoading = false
            Toast.makeText(this, "Erro ao carregar usuários: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        val last = lastVisible
        if (last == null) {
            UserRepository.getAllUsers(20, onSuccess, onError)
        } else {
            UserRepository.getAllUsersAfter(last, 20, onSuccess, onError)
        }
    }

    private fun filterAndRender(query: String) {
        val filtered = if (query.isBlank()) {
            allUsers
        } else {
            val q = query.lowercase()
            allUsers.filter { doc ->
                val nome = doc.getString("nome") ?: ""
                val cpf = doc.getString("cpf") ?: ""
                nome.lowercase().contains(q) || cpf.contains(q)
            }
        }

        // Remove only dynamic card views — keep tvEmpty in place (visibility-toggled only)
        for (i in usersContainer.childCount - 1 downTo 0) {
            val child = usersContainer.getChildAt(i)
            if (child !== tvEmpty) usersContainer.removeViewAt(i)
        }

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            for (doc in filtered) {
                usersContainer.addView(criarCardUsuario(doc))
            }
        }
    }

    private fun criarCardUsuario(doc: DocumentSnapshot): LinearLayout {
        val nome = doc.getString("nome") ?: "Usuário"
        val email = doc.getString("email") ?: ""
        val cpfRaw = doc.getString("cpf") ?: ""
        val photoBase64 = doc.getString("photoUrl") ?: ""
        val isAdmin = doc.getBoolean("admin") ?: false

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.bg_card_white)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            isClickable = true
            isFocusable = true
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
            setOnClickListener {
                startActivity(
                    Intent(this@AdminUsersPage, AdminUserDetailsPage::class.java)
                        .putExtra("USER_ID", doc.id)
                        .putExtra("ADMIN_NAV_TAB", AdminNavTab.USERS.name)
                )
            }
        }

        // Avatar circle
        val avatar = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52)).apply {
                marginEnd = dp(14)
            }
            setBackgroundResource(R.drawable.bg_avatar_circle)
        }

        val tvInitials = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            setTextColor(Color.WHITE)
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
        }

        val ivPhoto = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }

        avatar.addView(tvInitials)
        avatar.addView(ivPhoto)
        applyUserAvatar(ivPhoto, tvInitials, nome, photoBase64)

        // Info column
        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvNome = TextView(this).apply {
            text = if (isAdmin) "$nome · Admin" else nome
            setTextColor(Color.parseColor("#1E2D3D"))
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
        }

        val tvEmail = TextView(this).apply {
            text = email
            setTextColor(Color.parseColor("#9BAAC0"))
            textSize = 12f
            maxLines = 1
        }

        val tvCpf = TextView(this).apply {
            text = if (cpfRaw.isNotEmpty()) "CPF: ${formatarCpf(cpfRaw)}" else "CPF não cadastrado"
            setTextColor(Color.parseColor("#5C6B82"))
            textSize = 12f
        }

        info.addView(tvNome)
        info.addView(tvEmail)
        info.addView(tvCpf)

        val chevron = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
            setImageResource(R.drawable.ic_chevron_right)
            setColorFilter(Color.parseColor("#9BAAC0"))
        }

        card.addView(avatar)
        card.addView(info)
        card.addView(chevron)

        return card
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
