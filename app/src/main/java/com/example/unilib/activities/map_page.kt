package com.example.unilib.activities

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.unilib.R
import com.example.unilib.models.LibraryLocationConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.example.unilib.repository.NotificationRepository

class map_page : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvMapBookTitle: TextView
    private lateinit var tvMapShortInstructions: TextView
    private lateinit var tvMapLocation: TextView
    private lateinit var tvHowToFind: TextView
    private lateinit var bookMarker: LinearLayout

    private lateinit var notificationBadgeDot: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_page)

        tvMapBookTitle = findViewById(R.id.tvMapBookTitle)
        tvMapShortInstructions = findViewById(R.id.tvMapShortInstructions)
        tvMapLocation = findViewById(R.id.tvMapLocation)
        tvHowToFind = findViewById(R.id.tvHowToFind)
        bookMarker = findViewById(R.id.bookMarker)
        notificationBadgeDot = findViewById(R.id.notificationBadgeDot)

        val activeTab = intent.getStringExtra("NAV_TAB")
            ?.let { runCatching { NavTab.valueOf(it) }.getOrNull() }
            ?: NavTab.SEARCH

        NavBarHelper.setup(this, activeTab)

        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        setupNotificationsButton()
        loadBookLocation()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationsBadge()
    }

    private fun updateNotificationsBadge() {
        NotificationRepository.getCurrentUserUnreadNotificationsCount(
            onSuccess = { unreadCount ->
                notificationBadgeDot.visibility = if (unreadCount > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            },
            onError = {
                notificationBadgeDot.visibility = View.GONE
            }
        )
    }

    private fun setupNotificationsButton() {
        findViewById<FrameLayout>(R.id.btnNotifications).setOnClickListener {
            NotificationsModalHelper.show(this)
        }
    }

    private fun loadBookLocation() {
        val bookId = intent.getStringExtra("BOOK_ID")

        if (bookId.isNullOrBlank()) {
            showLocationUnavailable("Livro não informado.")
            return
        }

        db.collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    showLocationUnavailable("Livro não encontrado.")
                    return@addOnSuccessListener
                }

                val title = document.getString("title") ?: "Livro"
                val sectorCode = document.getString("sectorCode")
                val shelfCode = document.getString("shelfCode")
                val shelfLevel = document.getString("shelfLevel")

                if (
                    sectorCode.isNullOrBlank() ||
                    shelfCode.isNullOrBlank() ||
                    shelfLevel.isNullOrBlank()
                ) {
                    showLocationUnavailable("Localização não cadastrada para este livro.")
                    tvMapBookTitle.text = title
                    return@addOnSuccessListener
                }

                renderBookLocation(
                    title = title,
                    sectorCode = sectorCode,
                    shelfCode = shelfCode,
                    shelfLevel = shelfLevel
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Erro ao carregar localização: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()

                showLocationUnavailable("Não foi possível carregar a localização.")
            }
    }

    private fun renderBookLocation(
        title: String,
        sectorCode: String,
        shelfCode: String,
        shelfLevel: String
    ) {
        val sector = LibraryLocationConfig.getSectorByCode(sectorCode)
        val sectorName = sector?.displayName ?: "Setor não informado"

        val locationText = LibraryLocationConfig.buildLocationText(
            sectorCode = sectorCode,
            shelfCode = shelfCode,
            shelfLevel = shelfLevel
        )

        tvMapBookTitle.text = title

        tvMapShortInstructions.text =
            "1. Entre pela entrada principal indicada no mapa.\n" +
                    "2. Siga até o Setor $sectorName.\n" +
                    "3. Localize a Estante $shelfCode no mapa.\n" +
                    "4. Procure a Prateleira $shelfLevel."

        tvMapLocation.text =
            "📍 O marcador vermelho indica a localização aproximada do livro: $locationText."

        tvHowToFind.text =
            "1. Entre pela área principal da biblioteca.\n" +
                    "2. Siga até o Setor $sectorName.\n" +
                    "3. Procure a Estante $shelfCode.\n" +
                    "4. Verifique a Prateleira $shelfLevel."

        moveBookMarkerToShelf(shelfCode)
    }

    private fun moveBookMarkerToShelf(shelfCode: String) {
        val coordinate = LibraryLocationConfig.getCoordinateByShelfCode(shelfCode)

        if (coordinate == null) {
            Toast.makeText(
                this,
                "Coordenada da estante não configurada.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val params = bookMarker.layoutParams as ConstraintLayout.LayoutParams
        params.marginStart = dpToPx(coordinate.startDp)
        params.topMargin = dpToPx(coordinate.topDp)
        bookMarker.layoutParams = params
        bookMarker.visibility = View.VISIBLE
    }

    private fun showLocationUnavailable(message: String) {
        tvMapBookTitle.text = "Localização indisponível"
        tvMapShortInstructions.text = message
        tvMapLocation.text = "📍 Não foi possível localizar o exemplar no mapa."
        tvHowToFind.text =
            "A localização deste livro ainda não foi cadastrada corretamente pelo administrador."

        bookMarker.visibility = View.GONE
    }

    private fun dpToPx(valueDp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            valueDp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}