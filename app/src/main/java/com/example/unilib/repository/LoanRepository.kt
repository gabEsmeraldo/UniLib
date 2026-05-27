package com.example.unilib.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil

object LoanRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private const val COLLECTION_BOOKS = "books"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_LENTS = "user_lents_book"

    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_LATE = "LATE"
    const val STATUS_RETURNED = "RETURNED"

    private const val FINE_PER_DAY = 0.50
    private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

    /**
     * Busca empréstimos ativos ou atrasados do usuário logado.
     * A tela "Meus Empréstimos" usa essa função para listar os livros em aberto.
     */
    fun getCurrentUserOpenLoans(
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuário não autenticado."))
            return
        }

        val userRef = db.collection(COLLECTION_USERS).document(uid)

        db.collection(COLLECTION_LENTS)
            .whereEqualTo("user_id", userRef)
            .whereIn("status", listOf(STATUS_ACTIVE, STATUS_LATE))
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.documents)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Busca o histórico de empréstimos devolvidos do usuário logado.
     * Pode ser usado na tela Conta, na seção "Últimos empréstimos".
     */
    fun getCurrentUserReturnedLoans(
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuário não autenticado."))
            return
        }

        val userRef = db.collection(COLLECTION_USERS).document(uid)

        db.collection(COLLECTION_LENTS)
            .whereEqualTo("user_id", userRef)
            .whereEqualTo("status", STATUS_RETURNED)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.documents)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Busca todos os empréstimos ativos ou atrasados.
     * Usado pelo administrador na aba "Ativos".
     */
    fun getOpenLoansForAdmin(
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_LENTS)
            .whereIn("status", listOf(STATUS_ACTIVE, STATUS_LATE))
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.documents)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Confirma a devolução de um livro.
     * Atualiza o empréstimo como RETURNED e devolve 1 unidade ao campo available do livro.
     */
    fun confirmReturn(
        loanId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val loanRef = db.collection(COLLECTION_LENTS).document(loanId)

        db.runTransaction { transaction ->
            val loanSnapshot = transaction.get(loanRef)

            if (!loanSnapshot.exists()) {
                throw Exception("Empréstimo não encontrado.")
            }

            val currentStatus = loanSnapshot.getString("status") ?: STATUS_ACTIVE

            if (currentStatus == STATUS_RETURNED) {
                throw Exception("Este empréstimo já foi devolvido.")
            }

            val bookRef = loanSnapshot.getDocumentReference("book_id")
                ?: throw Exception("Empréstimo sem referência de livro.")

            val bookSnapshot = transaction.get(bookRef)
            val currentAvailable = getLongField(bookSnapshot, "available") ?: 0L

            val endDate = loanSnapshot.getTimestamp("end_date")
            val lateDays = calculateLateDays(endDate)
            val fineValue = calculateFineValue(lateDays)

            transaction.update(
                loanRef,
                mapOf(
                    "status" to STATUS_RETURNED,
                    "returned_at" to Timestamp.now(),
                    "late_days" to lateDays,
                    "fine_value" to fineValue
                )
            )

            transaction.update(
                bookRef,
                mapOf(
                    "available" to currentAvailable + 1L,
                    "lentCount" to FieldValue.increment(-1L)
                )
            )

            true
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onError(Exception(exception.message ?: "Erro ao confirmar devolução."))
        }
    }

    /**
     * Atualiza um empréstimo para LATE se a data de devolução já passou.
     * Essa função pode ser chamada quando a tela de empréstimos carregar.
     */
    fun updateLoanStatusIfLate(
        loanDocument: DocumentSnapshot
    ) {
        val currentStatus = loanDocument.getString("status") ?: STATUS_ACTIVE

        if (currentStatus == STATUS_RETURNED) {
            return
        }

        val endDate = loanDocument.getTimestamp("end_date")
        val lateDays = calculateLateDays(endDate)

        if (lateDays <= 0L) {
            return
        }

        val fineValue = calculateFineValue(lateDays)

        loanDocument.reference.update(
            mapOf(
                "status" to STATUS_LATE,
                "late_days" to lateDays,
                "fine_value" to fineValue
            )
        )
    }

    /**
     * Cria notificações para empréstimos próximos da data de devolução.
     * Para simplificar, considera "próximo" quando faltam 2 dias ou menos.
     */
    fun createDueSoonNotificationsForCurrentUser(
        daysBeforeDue: Long = 2L
    ) {
        getCurrentUserOpenLoans(
            onSuccess = { loans ->
                val uid = auth.currentUser?.uid ?: return@getCurrentUserOpenLoans
                val userRef = db.collection(COLLECTION_USERS).document(uid)

                loans.forEach { loanDocument ->
                    val endDate = loanDocument.getTimestamp("end_date") ?: return@forEach
                    val remainingDays = calculateRemainingDays(endDate)

                    if (remainingDays in 0..daysBeforeDue) {
                        val bookRef = loanDocument.getDocumentReference("book_id")

                        if (bookRef != null) {
                            bookRef.get().addOnSuccessListener { bookDocument ->
                                val title = bookDocument.getString("title") ?: "Livro"

                                NotificationRepository.createNotificationForUser(
                                    userRef = userRef,
                                    content = "O empréstimo do livro $title acabará nesta semana",
                                    type = "LOAN_DUE_SOON",
                                    relatedRef = loanDocument.reference
                                )
                            }
                        }
                    }
                }
            },
            onError = {}
        )
    }

    /**
     * Busca o documento do livro associado a um empréstimo.
     * A Activity usa isso para preencher título, autor e outros dados no card.
     */
    fun getBookFromLoan(
        loanDocument: DocumentSnapshot,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val bookRef = loanDocument.getDocumentReference("book_id")

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
     * Busca o documento do usuário associado a um empréstimo.
     * Usado principalmente pelo admin para exibir nome e CPF.
     */
    fun getUserFromLoan(
        loanDocument: DocumentSnapshot,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = loanDocument.getDocumentReference("user_id")

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

    fun calculateLateDays(endDate: Timestamp?): Long {
        val dueMillis = endDate?.toDate()?.time ?: return 0L
        val nowMillis = System.currentTimeMillis()

        if (nowMillis <= dueMillis) {
            return 0L
        }

        val diffMillis = nowMillis - dueMillis
        return ceil(diffMillis / ONE_DAY_MILLIS.toDouble()).toLong()
    }

    fun calculateRemainingDays(endDate: Timestamp?): Long {
        val dueMillis = endDate?.toDate()?.time ?: return 0L
        val nowMillis = System.currentTimeMillis()

        if (dueMillis <= nowMillis) {
            return 0L
        }

        val diffMillis = dueMillis - nowMillis
        return ceil(diffMillis / ONE_DAY_MILLIS.toDouble()).toLong()
    }

    fun calculateFineValue(lateDays: Long): Double {
        return lateDays * FINE_PER_DAY
    }

    fun formatDate(timestamp: Timestamp?): String {
        if (timestamp == null) return "--/--/----"

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return formatter.format(timestamp.toDate())
    }

    fun formatCurrency(value: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatter.format(value)
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