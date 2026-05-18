package com.example.unilib.repository

import com.example.unilib.models.Book
import com.google.firebase.firestore.FirebaseFirestore

class BookRepository {
    private val db = FirebaseFirestore.getInstance()
    fun addBook(book: Book, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        book.available = book.quantity
        db.collection("books")
            .add(book)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}