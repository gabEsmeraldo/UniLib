package com.example.unilib.repository

import com.example.unilib.models.Book
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class BookRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addBook(book: Book, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        book.available = book.quantity
        val docRef = db.collection("books").document()
        book.id = docRef.id
        docRef.set(book)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getTopLentBooks(
        limit: Int = 10,
        onSuccess: (List<Book>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("books")
            .orderBy("lentCount", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { result ->
                val books = result.documents.mapNotNull { doc ->
                    doc.toObject(Book::class.java)?.also { it.id = doc.id }
                }
                onSuccess(books)
            }
            .addOnFailureListener { onError(it) }
    }
}