package com.example.unilib.repository

import com.example.unilib.models.Book
import com.google.firebase.firestore.FirebaseFirestore

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
}