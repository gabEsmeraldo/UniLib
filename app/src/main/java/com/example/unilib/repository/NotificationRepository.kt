package com.example.unilib.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object NotificationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_NOTIFICATIONS = "notifications"

    /**
     * Cria uma notificação dentro de users/{userId}/notifications.
     * Esse padrão foi mantido porque o banco já possui notificações como subcoleção do usuário.
     */
    fun createNotificationForUser(
        userRef: DocumentReference,
        content: String,
        type: String,
        relatedRef: DocumentReference? = null,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val notificationData = hashMapOf<String, Any?>(
            "content" to content,
            "time_sent" to Timestamp.now(),
            "read" to false,
            "type" to type
        )

        if (relatedRef != null) {
            notificationData["related_ref"] = relatedRef
        }

        userRef.collection(COLLECTION_NOTIFICATIONS)
            .add(notificationData)
            .addOnSuccessListener {
                onSuccess?.invoke()
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }


    /**
     * Cria uma notificação para o usuário atualmente logado.
     * Útil quando a própria tela do usuário precisa registrar um aviso.
     */
    fun createNotificationForCurrentUser(
        content: String,
        type: String,
        relatedRef: DocumentReference? = null,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError?.invoke(Exception("Usuário não autenticado."))
            return
        }

        val userRef = db.collection(COLLECTION_USERS).document(uid)

        createNotificationForUser(
            userRef = userRef,
            content = content,
            type = type,
            relatedRef = relatedRef,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Busca as notificações do usuário logado.
     * A tela de notificações poderá usar essa função depois para exibir dados reais.
     */
    fun getCurrentUserNotifications(
        onSuccess: (List<DocumentSnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuário não autenticado."))
            return
        }

        db.collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_NOTIFICATIONS)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.documents)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Marca todas as notificações do usuário logado como lidas.
     * Usa batch para atualizar vários documentos de uma vez.
     */
    fun markCurrentUserNotificationsAsRead(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuário não autenticado."))
            return
        }

        val notificationsRef = db.collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_NOTIFICATIONS)

        notificationsRef
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()

                result.documents.forEach { document ->
                    batch.update(document.reference, "read", true)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun getCurrentUserUnreadNotificationsCount(
        OnSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuário não autenticado."))
            return
        }

        db.collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("read" , false)
            .get()
            .addOnSuccessListener { result ->
                OnSuccess(result.size())
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}