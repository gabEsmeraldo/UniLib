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

    fun getNewestBooks(
        limit: Int = 10,
        onSuccess: (List<Book>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("books")
            .orderBy("createdAt", Query.Direction.DESCENDING)
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

    fun deleteBook(bookId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("books")
            .document(bookId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun updateBookField(
        bookId: String,
        fieldName: String,
        newValue: Any,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("books")
            .document(bookId)
            .update(fieldName, newValue)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getAllBooks(
        limit: Int = 20,
        onSuccess: (List<Book>, com.google.firebase.firestore.DocumentSnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("books")
            .orderBy("title", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { result ->
                val books = result.documents.mapNotNull { doc ->
                    doc.toObject(Book::class.java)?.also { it.id = doc.id }
                }
                val lastDoc = result.documents.lastOrNull()
                onSuccess(books, lastDoc)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getBooksAfter(
        lastDocument: com.google.firebase.firestore.DocumentSnapshot,
        limit: Int = 20,
        onSuccess: (List<Book>, com.google.firebase.firestore.DocumentSnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("books")
            .orderBy("title", Query.Direction.ASCENDING)
            .startAfter(lastDocument)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { result ->
                val books = result.documents.mapNotNull { doc ->
                    doc.toObject(Book::class.java)?.also { it.id = doc.id }
                }
                val lastDoc = result.documents.lastOrNull()
                onSuccess(books, lastDoc)
            }
            .addOnFailureListener { onError(it) }
    }
}