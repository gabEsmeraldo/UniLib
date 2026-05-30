package com.example.unilib.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object UserRepository {

    private val db = FirebaseFirestore.getInstance()

    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_LENTS = "user_lents_book"
    private const val COLLECTION_RESERVES = "user_reserves_book"

    fun getAllUsers(
        limit: Long = 20,
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_USERS)
            .limit(limit)
            .get()
            .addOnSuccessListener { result -> onSuccess(result.documents) }
            .addOnFailureListener { onError(it) }
    }

    fun getAllUsersAfter(
        lastDoc: DocumentSnapshot,
        limit: Long = 20,
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_USERS)
            .startAfter(lastDoc)
            .limit(limit)
            .get()
            .addOnSuccessListener { result -> onSuccess(result.documents) }
            .addOnFailureListener { onError(it) }
    }

    fun getUserById(
        uid: String,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_USERS).document(uid)
            .get()
            .addOnSuccessListener { onSuccess(it) }
            .addOnFailureListener { onError(it) }
    }

    fun updateUserField(
        uid: String,
        fieldName: String,
        newValue: Any,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_USERS).document(uid)
            .update(fieldName, newValue)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getOpenLoansForUser(
        uid: String,
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = db.collection(COLLECTION_USERS).document(uid)
        db.collection(COLLECTION_LENTS)
            .whereEqualTo("user_id", userRef)
            .whereIn("status", listOf("ACTIVE", "LATE"))
            .get()
            .addOnSuccessListener { result -> onSuccess(result.documents) }
            .addOnFailureListener { onError(it) }
    }

    fun getPendingReservationsForUser(
        uid: String,
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = db.collection(COLLECTION_USERS).document(uid)
        db.collection(COLLECTION_RESERVES)
            .whereEqualTo("user_id", userRef)
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener { result -> onSuccess(result.documents) }
            .addOnFailureListener { onError(it) }
    }

    fun deleteUserWithCascade(
        uid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = db.collection(COLLECTION_USERS).document(uid)

        // Step 1: cancel PENDING reservations (restores book.available via existing logic)
        db.collection(COLLECTION_RESERVES)
            .whereEqualTo("user_id", userRef)
            .whereIn("status", listOf("PENDING", "APPROVED"))
            .get()
            .addOnSuccessListener { reservas ->
                val pendingIds = reservas.documents
                    .filter { it.getString("status") == "PENDING" }
                    .map { it.id }
                val approvedDocs = reservas.documents
                    .filter { it.getString("status") == "APPROVED" }

                // Mark APPROVED reservations cancelled (their inventory is tracked by the loan)
                val approvedBatch = db.batch()
                for (doc in approvedDocs) {
                    approvedBatch.update(doc.reference, "status", "CANCELLED")
                }
                approvedBatch.commit()

                cancelPendingReservationsThenLoans(pendingIds, 0, userRef, onSuccess, onError)
            }
            .addOnFailureListener { onError(it) }
    }

    private fun cancelPendingReservationsThenLoans(
        pendingIds: List<String>,
        index: Int,
        userRef: com.google.firebase.firestore.DocumentReference,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (index >= pendingIds.size) {
            // All pending reservations cancelled — now handle active loans
            returnActiveLoansForUser(userRef, onSuccess, onError)
            return
        }
        val reservationId = pendingIds[index]
        db.collection(COLLECTION_RESERVES).document(reservationId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists() || doc.getString("status") != "PENDING") {
                    // Already processed; skip
                    cancelPendingReservationsThenLoans(pendingIds, index + 1, userRef, onSuccess, onError)
                    return@addOnSuccessListener
                }
                val bookRef = doc.getDocumentReference("book_id")
                if (bookRef == null) {
                    cancelPendingReservationsThenLoans(pendingIds, index + 1, userRef, onSuccess, onError)
                    return@addOnSuccessListener
                }
                db.runTransaction { transaction ->
                    val bookSnap = transaction.get(bookRef)
                    val available = bookSnap.getLong("available") ?: 0L
                    transaction.update(doc.reference, mapOf("status" to "CANCELLED"))
                    transaction.update(bookRef, mapOf(
                        "available" to available + 1L,
                        "lentCount" to com.google.firebase.firestore.FieldValue.increment(-1L)
                    ))
                    true
                }.addOnSuccessListener {
                    cancelPendingReservationsThenLoans(pendingIds, index + 1, userRef, onSuccess, onError)
                }.addOnFailureListener {
                    // Log and continue — don't abort the whole delete for one reservation
                    cancelPendingReservationsThenLoans(pendingIds, index + 1, userRef, onSuccess, onError)
                }
            }
            .addOnFailureListener {
                cancelPendingReservationsThenLoans(pendingIds, index + 1, userRef, onSuccess, onError)
            }
    }

    private fun returnActiveLoansForUser(
        userRef: com.google.firebase.firestore.DocumentReference,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_LENTS)
            .whereEqualTo("user_id", userRef)
            .whereIn("status", listOf("ACTIVE", "LATE"))
            .get()
            .addOnSuccessListener { loans ->
                returnLoansSequentially(loans.documents, 0, userRef, onSuccess, onError)
            }
            .addOnFailureListener { onError(it) }
    }

    private fun returnLoansSequentially(
        loans: List<com.google.firebase.firestore.DocumentSnapshot>,
        index: Int,
        userRef: com.google.firebase.firestore.DocumentReference,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (index >= loans.size) {
            // All loans returned — delete user document
            userRef.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }
            return
        }
        val loanDoc = loans[index]
        val bookRef = loanDoc.getDocumentReference("book_id")
        if (bookRef == null) {
            returnLoansSequentially(loans, index + 1, userRef, onSuccess, onError)
            return
        }
        db.runTransaction { transaction ->
            val bookSnap = transaction.get(bookRef)
            val available = bookSnap.getLong("available") ?: 0L
            transaction.update(loanDoc.reference, mapOf(
                "status" to "RETURNED",
                "returned_at" to Timestamp.now()
            ))
            transaction.update(bookRef, mapOf(
                "available" to available + 1L,
                "lentCount" to com.google.firebase.firestore.FieldValue.increment(-1L)
            ))
            true
        }.addOnSuccessListener {
            returnLoansSequentially(loans, index + 1, userRef, onSuccess, onError)
        }.addOnFailureListener {
            // Log and continue — don't abort the whole delete for one loan
            returnLoansSequentially(loans, index + 1, userRef, onSuccess, onError)
        }
    }
}
