package com.example.unilib.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale
import java.util.UUID

object ReservationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private const val COLLECTION_BOOKS = "books"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_RESERVES = "user_reserves_book"
    private const val COLLECTION_LENTS = "user_lents_book"

    const val STATUS_PENDING = "PENDING"
    const val STATUS_APPROVED = "APPROVED"
    const val STATUS_CANCELLED = "CANCELLED"
    const val STATUS_EXPIRED = "EXPIRED"

    private const val RESERVATION_EXPIRATION_MINUTES = 30L
    private const val LOAN_DAYS = 7L
    private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

    data class ReservationCreatedResult(
        val reservationId: String,
        val reservationCode: String,
        val bookTitle: String,
        val expiresAt: Timestamp
    )

    /**
     * Cria uma reserva buscando o livro pelo título.
     * Essa função combina com o BookDetails.kt atual, que recebe o livro por TITULO_LIVRO.
     */
    fun createReservationByBookTitle(
        bookTitle: String,
        onSuccess: (ReservationCreatedResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_BOOKS)
            .whereEqualTo("title", bookTitle)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val bookDocument = result.documents.firstOrNull()

                if (bookDocument == null) {
                    onError(Exception("Livro não encontrado no banco."))
                    return@addOnSuccessListener
                }

                createReservationByBookReference(
                    bookRef = bookDocument.reference,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Cria uma reserva buscando o livro diretamente pelo ID do documento.
     * Esse é o fluxo ideal quando a tela anterior já conhece o bookId real do Firestore.
     */
    fun createReservationByBookId(
        bookId: String,
        onSuccess: (ReservationCreatedResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (bookId.isBlank()) {
            onError(Exception("ID do livro inválido."))
            return
        }

        val bookRef = db.collection(COLLECTION_BOOKS).document(bookId)

        createReservationByBookReference(
            bookRef = bookRef,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Cria uma reserva a partir da referência do livro.
     * Usa transaction para evitar que dois usuários reservem o mesmo exemplar ao mesmo tempo.
     */
    fun createReservationByBookReference(
        bookRef: DocumentReference,
        onSuccess: (ReservationCreatedResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuário não autenticado. Faça login novamente."))
            return
        }

        val userRef = db.collection(COLLECTION_USERS).document(uid)
        val reserveRef = db.collection(COLLECTION_RESERVES).document()

        val reservationCode = generateReservationCode()
        val expiresAt = Timestamp(
            Date(System.currentTimeMillis() + RESERVATION_EXPIRATION_MINUTES * 60L * 1000L)
        )

        db.runTransaction { transaction ->
            val bookSnapshot = transaction.get(bookRef)

            if (!bookSnapshot.exists()) {
                throw Exception("Livro não encontrado.")
            }

            val available = getLongField(bookSnapshot, "available")
                ?: getLongField(bookSnapshot, "quantity")
                ?: 0L

            if (available <= 1L) {
                throw Exception("Reserva bloqueada. É necessário manter pelo menos 1 exemplar disponível.")
            }

            val realBookTitle = bookSnapshot.getString("title") ?: "Livro reservado"

            val reserveData = mapOf(
                "book_id" to bookRef,
                "user_id" to userRef,
                "reserve_timestamp" to Timestamp.now(),
                "createdAt" to FieldValue.serverTimestamp(),
                "expires_at" to expiresAt,
                "reservation_code" to reservationCode,
                "status" to STATUS_PENDING
            )

            transaction.set(reserveRef, reserveData)
            transaction.update(bookRef, "available", available - 1L)

            ReservationCreatedResult(
                reservationId = reserveRef.id,
                reservationCode = reservationCode,
                bookTitle = realBookTitle,
                expiresAt = expiresAt
            )
        }.addOnSuccessListener { result ->
            NotificationRepository.createNotificationForUser(
                userRef = userRef,
                content = "Reserva criada para o livro ${result.bookTitle}. Código de retirada: ${result.reservationCode}",
                type = "RESERVATION_CREATED",
                relatedRef = reserveRef
            )

            onSuccess(result)
        }.addOnFailureListener { exception ->
            onError(Exception(exception.message ?: "Erro ao criar reserva."))
        }
    }

    /**
     * Busca reservas pendentes do usuário logado.
     * A tela Conta e a tela Meus Empréstimos podem usar essa função.
     */
    fun getCurrentUserPendingReservations(
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuário não autenticado."))
            return
        }

        val userRef = db.collection(COLLECTION_USERS).document(uid)

        db.collection(COLLECTION_RESERVES)
            .whereEqualTo("user_id", userRef)
            .whereEqualTo("status", STATUS_PENDING)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.documents)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Busca todas as reservas pendentes.
     * Essa função é usada pelo administrador na aba "Pendentes".
     */
    fun getPendingReservationsForAdmin(
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_RESERVES)
            .whereEqualTo("status", STATUS_PENDING)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.documents)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Cancela uma reserva pendente.
     * Também devolve 1 unidade para o campo available do livro.
     */
    fun cancelReservation(
        reservationId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val reserveRef = db.collection(COLLECTION_RESERVES).document(reservationId)

        db.runTransaction { transaction ->
            val reserveSnapshot = transaction.get(reserveRef)

            if (!reserveSnapshot.exists()) {
                throw Exception("Reserva não encontrada.")
            }

            val status = reserveSnapshot.getString("status") ?: STATUS_PENDING

            if (status != STATUS_PENDING) {
                throw Exception("Apenas reservas pendentes podem ser canceladas.")
            }

            val bookRef = reserveSnapshot.getDocumentReference("book_id")
                ?: throw Exception("Reserva sem referência de livro.")

            val bookSnapshot = transaction.get(bookRef)
            val available = getLongField(bookSnapshot, "available") ?: 0L

            transaction.update(
                reserveRef,
                mapOf(
                    "status" to STATUS_CANCELLED,
                    "cancelled_at" to Timestamp.now()
                )
            )

            transaction.update(bookRef, "available", available + 1L)

            true
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onError(Exception(exception.message ?: "Erro ao cancelar reserva."))
        }
    }

    /**
     * Aprova a retirada de uma reserva usando o código informado pelo usuário.
     * Se o código for válido, cria um empréstimo em user_lents_book.
     */
    fun approveReservationWithCode(
        reservationId: String,
        typedCode: String,
        onSuccess: (loanId: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val reserveRef = db.collection(COLLECTION_RESERVES).document(reservationId)
        val loanRef = db.collection(COLLECTION_LENTS).document()

        db.runTransaction { transaction ->
            val reserveSnapshot = transaction.get(reserveRef)

            if (!reserveSnapshot.exists()) {
                throw Exception("Reserva não encontrada.")
            }

            val status = reserveSnapshot.getString("status") ?: STATUS_PENDING

            if (status != STATUS_PENDING) {
                throw Exception("Esta reserva não está pendente.")
            }

            val savedCode = reserveSnapshot.getString("reservation_code") ?: ""

            if (normalizeCode(savedCode) != normalizeCode(typedCode)) {
                throw Exception("Código inválido ou expirado, insira novamente.")
            }

            val expiresAt = reserveSnapshot.getTimestamp("expires_at")
                ?: throw Exception("Reserva sem data de expiração.")

            if (expiresAt.toDate().time < System.currentTimeMillis()) {
                transaction.update(
                    reserveRef,
                    mapOf(
                        "status" to STATUS_EXPIRED,
                        "expired_at" to Timestamp.now()
                    )
                )

                throw Exception("Código inválido ou expirado, insira novamente.")
            }

            val bookRef = reserveSnapshot.getDocumentReference("book_id")
                ?: throw Exception("Reserva sem referência de livro.")

            val userRef = reserveSnapshot.getDocumentReference("user_id")
                ?: throw Exception("Reserva sem referência de usuário.")

            val startDate = Timestamp.now()
            val endDate = Timestamp(Date(System.currentTimeMillis() + LOAN_DAYS * ONE_DAY_MILLIS))

            val loanData = mapOf<String, Any?>(
                "book_id" to bookRef,
                "user_id" to userRef,
                "start_date" to startDate,
                "end_date" to endDate,
                "returned_at" to null,
                "status" to LoanRepository.STATUS_ACTIVE,
                "fine_value" to 0.0,
                "late_days" to 0L,
                "reservation_id" to reserveRef,
                "createdAt" to FieldValue.serverTimestamp()
            )

            transaction.set(loanRef, loanData)

            transaction.update(
                reserveRef,
                mapOf(
                    "status" to STATUS_APPROVED,
                    "approved_at" to Timestamp.now(),
                    "loan_id" to loanRef
                )
            )

            loanRef.id
        }.addOnSuccessListener { loanId ->
            onSuccess(loanId)
        }.addOnFailureListener { exception ->
            onError(Exception(exception.message ?: "Erro ao aprovar retirada."))
        }
    }

    /**
     * Busca o documento do livro associado a uma reserva.
     * Mantemos simples para a Activity preencher os cards com title, author etc.
     */
    fun getBookFromReservation(
        reservationDocument: DocumentSnapshot,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val bookRef = reservationDocument.getDocumentReference("book_id")

        if (bookRef == null) {
            onSuccess(null)
            return
        }

        bookRef.get()
            .addOnSuccessListener { bookDocument ->
                onSuccess(bookDocument)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Busca o documento do usuário associado a uma reserva.
     * Usado principalmente na tela do admin para exibir nome e CPF.
     */
    fun getUserFromReservation(
        reservationDocument: DocumentSnapshot,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = reservationDocument.getDocumentReference("user_id")

        if (userRef == null) {
            onSuccess(null)
            return
        }

        userRef.get()
            .addOnSuccessListener { userDocument ->
                onSuccess(userDocument)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    private fun generateReservationCode(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        return (1..6)
            .map { alphabet.random() }
            .joinToString("")
    }

    private fun normalizeCode(code: String): String {
        return code
            .trim()
            .replace("-", "")
            .replace(" ", "")
            .uppercase(Locale.ROOT)
    }

    private fun getLongField(document: DocumentSnapshot, field: String): Long? {
        val value = document.get(field) ?: return null

        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Float -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
}