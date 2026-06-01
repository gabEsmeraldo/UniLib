package com.example.unilib.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.R
import com.example.unilib.models.Book
import com.example.unilib.repository.BookRepository
import com.google.firebase.firestore.DocumentSnapshot

class AdminSearchPage : AppCompatActivity() {

    private val bookRepository = BookRepository()
    private lateinit var adapter: SearchBookAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var loadMoreContainer: LinearLayout
    private lateinit var btnLoadMore: Button
    private lateinit var progressBarLoadMore: ProgressBar

    private val allBooks = mutableListOf<Book>()
    private var lastDocument: DocumentSnapshot? = null
    private var isLoading = false
    private var hasMoreBooks = true
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_search_page)

        AdminNavBarHelper.setup(this, AdminNavTab.SEARCH)

        initViews()
        setupRecyclerView()
        setupSearchListener()
        setupLoadMore()
        loadFirstPage()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewBooks)
        editTextSearch = findViewById(R.id.EditTextSearch)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        loadMoreContainer = findViewById(R.id.loadMoreContainer)
        btnLoadMore = findViewById(R.id.btnLoadMore)
        progressBarLoadMore = findViewById(R.id.progressBarLoadMore)
    }

    private fun setupRecyclerView() {
        adapter = SearchBookAdapter(emptyList()) { book ->
            irParaDetalhes(book.id, book.title)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && hasMoreBooks && currentQuery.isEmpty()) {
                        if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 3) {
                            loadNextPage()
                        }
                    }
                }
            }
        })
    }

    private fun setupSearchListener() {
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString()?.trim() ?: ""
                filterBooks()
            }
        })
    }

    private fun setupLoadMore() {
        btnLoadMore.setOnClickListener {
            loadNextPage()
        }
    }

    private fun loadFirstPage() {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        bookRepository.getAllBooks(
            limit = 20,
            onSuccess = { books, lastDoc ->
                allBooks.clear()
                allBooks.addAll(books)
                lastDocument = lastDoc
                hasMoreBooks = books.size >= 20
                isLoading = false
                showLoading(false)
                filterBooks()
                updateLoadMoreVisibility()
            },
            onError = { exception ->
                isLoading = false
                showLoading(false)
                Toast.makeText(this, "Erro ao carregar livros: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadNextPage() {
        if (isLoading || lastDocument == null || !hasMoreBooks) return
        isLoading = true
        progressBarLoadMore.visibility = View.VISIBLE
        btnLoadMore.visibility = View.GONE

        bookRepository.getBooksAfter(
            lastDocument = lastDocument!!,
            limit = 20,
            onSuccess = { books, lastDoc ->
                allBooks.addAll(books)
                lastDocument = lastDoc
                hasMoreBooks = books.size >= 20
                isLoading = false
                progressBarLoadMore.visibility = View.GONE
                btnLoadMore.visibility = View.VISIBLE
                filterBooks()
                updateLoadMoreVisibility()
            },
            onError = { exception ->
                isLoading = false
                progressBarLoadMore.visibility = View.GONE
                btnLoadMore.visibility = View.VISIBLE
                Toast.makeText(this, "Erro ao carregar mais livros: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun filterBooks() {
        val filtered = if (currentQuery.isEmpty()) {
            allBooks
        } else {
            allBooks.filter { book ->
                book.title.startsWith(currentQuery, ignoreCase = true) ||
                book.isbn.contains(currentQuery, ignoreCase = true)
            }
        }

        adapter.updateBooks(filtered)

        if (filtered.isEmpty() && !isLoading) {
            tvEmptyState.visibility = View.VISIBLE
            if (currentQuery.isEmpty()) {
                tvEmptyState.text = "Nenhum livro cadastrado ainda"
            } else {
                tvEmptyState.text = "Nenhum livro encontrado para \"$currentQuery\""
            }
        } else {
            tvEmptyState.visibility = View.GONE
        }

        updateLoadMoreVisibility()
    }

    private fun updateLoadMoreVisibility() {
        if (currentQuery.isEmpty() && hasMoreBooks && allBooks.isNotEmpty()) {
            loadMoreContainer.visibility = View.VISIBLE
        } else {
            loadMoreContainer.visibility = View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            tvEmptyState.visibility = View.GONE
            loadMoreContainer.visibility = View.GONE
        }
    }

    private fun irParaDetalhes(bookId: String, titulo: String) {
        val intent = Intent(this, admin_book_details::class.java)
        intent.putExtra("BOOK_ID", bookId)
        intent.putExtra("TITULO_LIVRO", titulo)
        intent.putExtra("ADMIN_NAV_TAB", AdminNavTab.SEARCH.name)
        startActivity(intent)
    }
}
