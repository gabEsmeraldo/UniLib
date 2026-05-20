package com.example.unilib.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unilib.R
import com.example.unilib.models.Book
import com.example.unilib.repository.BookRepository
import android.content.Intent



class AdminAddBook : AppCompatActivity() {
    private lateinit var repository: BookRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_add_book)
        repository = BookRepository()
        val editBookTitle = findViewById<EditText>(R.id.editBookTitle)
        val editAuthor = findViewById<EditText>(R.id.editAuthor)
        val editIsbn = findViewById<EditText>(R.id.editIsbn)
        editIsbn.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val digits = s.toString().filter { it.isDigit() }.take(13)
                val formatted = buildString {
                    for (i in digits.indices) {
                        if (i == 3 || i == 4 || i == 6 || i == 12) append('-')
                        append(digits[i])
                    }
                }
                if (formatted != s.toString()) {
                    editIsbn.setText(formatted)
                    editIsbn.setSelection(formatted.length)
                }
                isFormatting = false
            }
        })
        val editTags = findViewById<EditText>(R.id.editTags)
        val editSynopsis = findViewById<EditText>(R.id.editSynopsis)
        val editQuantity = findViewById<EditText>(R.id.editQuantity)
        val btnEnter = findViewById<Button>(R.id.btnEnter)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        btnBack.setOnClickListener {
            navigateBack()
        }
        btnCancel.setOnClickListener {
            navigateBack()
        }
        btnEnter.setOnClickListener {
            val title = editBookTitle.text.toString().trim()
            val author = editAuthor.text.toString().trim()
            val isbn = editIsbn.text.toString().filter { it.isDigit() }
            val tagsText = editTags.text.toString().trim()
            val synopsis = editSynopsis.text.toString().trim()
            val quantityText = editQuantity.text.toString().trim()
            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || quantityText.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val quantity = quantityText.toLongOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(this, "Quantidade de exemplares inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val tags = if (tagsText.isNotEmpty()) {
                tagsText.split(",", " ").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
            val book = Book(
                title = title,
                author = author,
                isbn = isbn,
                tags = tags,
                synopsis = synopsis,
                quantity = quantity
            )
            btnEnter.isEnabled = false
            btnEnter.text = "Salvando..."
            repository.addBook(
                book = book,
                onSuccess = {
                    Toast.makeText(this, "Livro cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    navigateBack()
                },
                onError = { exception ->
                    Toast.makeText(this, "Erro ao salvar: ${exception.message}", Toast.LENGTH_LONG).show()
                    btnEnter.isEnabled = true
                    btnEnter.text = "Confirmar e Adicionar"
                }
            )
        }
    }
    private fun navigateBack() {
        val intent = Intent(this, AdminHomePage::class.java)
        startActivity(intent)
        finish()
    }
}