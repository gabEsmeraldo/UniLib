package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.example.unilib.repository.BookRepository

class AdminHomePage : AppCompatActivity() {

    private val bookRepository = BookRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_home_page)

        AdminNavBarHelper.setup(this, AdminNavTab.HOME)
        setupNavigation()
        loadTopLentBooks()
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.btnAdminLogout)?.setOnClickListener {
            val intent = Intent(this, StartPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        findViewById<View>(R.id.btnAddBook)?.setOnClickListener {
            startActivity(Intent(this, AdminAddBook::class.java))
        }

        findViewById<View>(R.id.tvManageCollection)?.setOnClickListener {
            startActivity(Intent(this, AdminSearchPage::class.java))
            finish()
        }
    }

    private fun loadTopLentBooks() {
        val container = findViewById<LinearLayout>(R.id.llFeaturedBooks)
        val backgrounds = listOf(
            R.drawable.bg_book_blue,
            R.drawable.bg_book_green,
            R.drawable.bg_book_purple,
            R.drawable.bg_book_red,
            R.drawable.bg_book_gray
        )
        val colorNames = listOf("blue", "green", "purple", "red", "gray")

        bookRepository.getTopLentBooks(10,
            onSuccess = { books ->
                books.forEachIndexed { index, book ->
                    val card = LayoutInflater.from(this)
                        .inflate(R.layout.item_book_card, container, false)

                    card.findViewById<FrameLayout>(R.id.bookCover)
                        .setBackgroundResource(backgrounds[index % backgrounds.size])
                    card.findViewById<TextView>(R.id.tvAvailBadge).text = "${book.available} disp."
                    card.findViewById<TextView>(R.id.tvBookTitle).text = book.title
                    card.findViewById<TextView>(R.id.tvBookAuthor).text = book.author

                    val colorName = colorNames[index % colorNames.size]
                    card.setOnClickListener {
                        val intent = Intent(this, admin_book_details::class.java)
                        intent.putExtra("TITULO_LIVRO", book.title)
                        intent.putExtra("BOOK_ID", book.id)
                        intent.putExtra("BOOK_COLOR", colorName)
                        intent.putExtra("ADMIN_NAV_TAB", AdminNavTab.HOME.name)
                        startActivity(intent)
                    }

                    container.addView(card)
                }
            },
            onError = {}
        )
    }
}
