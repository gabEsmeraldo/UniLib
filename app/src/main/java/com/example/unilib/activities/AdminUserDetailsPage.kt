package com.example.unilib.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.unilib.R
import com.example.unilib.repository.LoanRepository
import com.example.unilib.repository.ReservationRepository
import com.example.unilib.repository.UserRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.yalantis.ucrop.UCrop

class AdminUserDetailsPage : AppCompatActivity() {

    private var currentUserId: String = ""
    private var currentPhotoBase64: String = ""
    private var currentIsAdmin: Boolean = false
    private var cameraPhotoUri: Uri? = null
    private var pendingCropUri: Uri? = null

    private lateinit var tvHeroNome: TextView
    private lateinit var tvHeroEmail: TextView
    private lateinit var tvHeroCpf: TextView
    private lateinit var tvAdminBadge: TextView
    private lateinit var tvHeroIniciais: TextView
    private lateinit var ivHeroPhoto: ImageView
    private lateinit var llLoans: LinearLayout
    private lateinit var llReservations: LinearLayout

    private companion object {
        const val REQUEST_CAMERA = 1001
        const val REQUEST_GALLERY = 1002
        const val REQUEST_CAMERA_PERMISSION = 2001
    }

    override fun onResume() {
        super.onResume()
        pendingCropUri?.let { uri ->
            pendingCropUri = null
            launchCrop(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_user_details)

        currentUserId = intent.getStringExtra("USER_ID") ?: run { finish(); return }

        val activeTab = intent.getStringExtra("ADMIN_NAV_TAB")
            ?.let { runCatching { AdminNavTab.valueOf(it) }.getOrNull() }
            ?: AdminNavTab.USERS
        AdminNavBarHelper.setup(this, activeTab)

        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        tvHeroNome = findViewById(R.id.tvHeroNome)
        tvHeroEmail = findViewById(R.id.tvHeroEmail)
        tvHeroCpf = findViewById(R.id.tvHeroCpf)
        tvAdminBadge = findViewById(R.id.tvAdminBadge)
        tvHeroIniciais = findViewById(R.id.tvHeroIniciais)
        ivHeroPhoto = findViewById(R.id.ivHeroPhoto)
        llLoans = findViewById(R.id.llLoansContainer)
        llReservations = findViewById(R.id.llReservationsContainer)

        // Tap avatar to change photo
        findViewById<FrameLayout>(R.id.avatarContainer)?.setOnClickListener {
            showImageSourcePicker()
        }

        loadUserFromFirestore()
    }

    private fun loadUserFromFirestore() {
        UserRepository.getUserById(
            uid = currentUserId,
            onSuccess = { document ->
                if (document == null || !document.exists()) {
                    Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@getUserById
                }
                onUserLoaded(document)
            },
            onError = { e ->
                Toast.makeText(this, "Erro ao carregar: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }

    private fun onUserLoaded(document: DocumentSnapshot) {
        val nome = document.getString("nome") ?: ""
        val email = document.getString("email") ?: ""
        val cpfRaw = document.getString("cpf") ?: ""
        val isAdmin = document.getBoolean("admin") ?: false
        val photoUrl = document.getString("photoUrl") ?: ""

        currentPhotoBase64 = photoUrl
        currentIsAdmin = isAdmin

        renderUser(nome, email, cpfRaw, isAdmin, photoUrl)
        wireEditModals(nome, cpfRaw, isAdmin)
        loadActiveLoans()
        loadPendingReservations()
    }

    private fun renderUser(nome: String, email: String, cpfRaw: String, isAdmin: Boolean, photoUrl: String) {
        tvHeroNome.text = nome.ifEmpty { "Usuário" }
        tvHeroEmail.text = email
        tvHeroCpf.text = if (cpfRaw.isNotEmpty()) "CPF: ${formatarCpf(cpfRaw)}" else ""
        renderAdminBadge(isAdmin)
        applyUserAvatar(ivHeroPhoto, tvHeroIniciais, nome, photoUrl)
    }

    private fun renderAdminBadge(isAdmin: Boolean) {
        if (isAdmin) {
            tvAdminBadge.text = "Administrador"
            tvAdminBadge.setTextColor(Color.parseColor("#1474C4"))
        } else {
            tvAdminBadge.text = "Usuário comum"
            tvAdminBadge.setTextColor(Color.parseColor("#9BAAC0"))
        }
    }

    private fun wireEditModals(nome: String, cpfRaw: String, isAdmin: Boolean) {
        // Admin switch — set state without triggering listener, then wire listener
        val switchAdmin = findViewById<SwitchCompat>(R.id.switchAdmin)
        switchAdmin?.setOnCheckedChangeListener(null)
        switchAdmin?.isChecked = isAdmin
        switchAdmin?.setOnCheckedChangeListener { _, checked ->
            UserRepository.updateUserField(currentUserId, "admin", checked,
                onSuccess = {
                    currentIsAdmin = checked
                    renderAdminBadge(checked)
                    Toast.makeText(this, "Permissão atualizada!", Toast.LENGTH_SHORT).show()
                },
                onError = { e ->
                    // Revert switch on failure
                    switchAdmin.setOnCheckedChangeListener(null)
                    switchAdmin.isChecked = !checked
                    switchAdmin.setOnCheckedChangeListener { _, c ->
                        UserRepository.updateUserField(currentUserId, "admin", c,
                            onSuccess = { currentIsAdmin = c; renderAdminBadge(c) },
                            onError = {}
                        )
                    }
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }

        findViewById<View>(R.id.btnEditarNomeUsuario)?.setOnClickListener {
            EditarNomeUsuarioModalHelper.show(this, nome) { novoNome ->
                UserRepository.updateUserField(currentUserId, "nome", novoNome,
                    onSuccess = {
                        Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                        loadUserFromFirestore()
                    },
                    onError = { e -> Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
                )
            }
        }

        findViewById<View>(R.id.btnEditarCpfUsuario)?.setOnClickListener {
            EditarCpfUsuarioModalHelper.show(this, cpfRaw) { novoCpf ->
                UserRepository.updateUserField(currentUserId, "cpf", novoCpf,
                    onSuccess = {
                        Toast.makeText(this, "CPF atualizado!", Toast.LENGTH_SHORT).show()
                        loadUserFromFirestore()
                    },
                    onError = { e -> Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
                )
            }
        }

        findViewById<View>(R.id.btnExcluirUsuario)?.setOnClickListener {
            ConfirmarExclusaoUsuarioModalHelper.show(this, nome) {
                UserRepository.deleteUserWithCascade(currentUserId,
                    onSuccess = {
                        Toast.makeText(this, "Usuário excluído.", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onError = { e ->
                        Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    private fun loadActiveLoans() {
        llLoans.removeAllViews()
        addSectionPlaceholder(llLoans, "Carregando empréstimos...")

        UserRepository.getOpenLoansForUser(currentUserId,
            onSuccess = { loans ->
                llLoans.removeAllViews()
                if (loans.isEmpty()) {
                    addSectionPlaceholder(llLoans, "Nenhum empréstimo ativo")
                    return@getOpenLoansForUser
                }
                loans.forEachIndexed { index, loanDoc ->
                    LoanRepository.updateLoanStatusIfLate(loanDoc)
                    LoanRepository.getBookFromLoan(loanDoc,
                        onSuccess = { bookDoc ->
                            llLoans.addView(criarCardEmprestimo(loanDoc, bookDoc, index))
                        },
                        onError = {
                            llLoans.addView(criarCardEmprestimo(loanDoc, null, index))
                        }
                    )
                }
            },
            onError = { e ->
                llLoans.removeAllViews()
                addSectionPlaceholder(llLoans, "Erro ao carregar empréstimos")
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadPendingReservations() {
        llReservations.removeAllViews()
        addSectionPlaceholder(llReservations, "Carregando reservas...")

        UserRepository.getPendingReservationsForUser(currentUserId,
            onSuccess = { reservas ->
                llReservations.removeAllViews()
                if (reservas.isEmpty()) {
                    addSectionPlaceholder(llReservations, "Nenhuma reserva pendente")
                    return@getPendingReservationsForUser
                }
                reservas.forEach { reservaDoc ->
                    ReservationRepository.getBookFromReservation(reservaDoc,
                        onSuccess = { bookDoc ->
                            llReservations.addView(criarCardReserva(reservaDoc, bookDoc))
                        },
                        onError = {
                            llReservations.addView(criarCardReserva(reservaDoc, null))
                        }
                    )
                }
            },
            onError = { e ->
                llReservations.removeAllViews()
                addSectionPlaceholder(llReservations, "Erro ao carregar reservas")
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun criarCardEmprestimo(loanDoc: DocumentSnapshot, bookDoc: DocumentSnapshot?, index: Int): LinearLayout {
        val title = bookDoc?.getString("title") ?: "Livro"
        val author = bookDoc?.getString("author") ?: "Autor não informado"
        val imageUrl = bookDoc?.getString("imageUrl") ?: ""
        val endDate = loanDoc.getTimestamp("end_date")
        val lateDays = LoanRepository.calculateLateDays(endDate)
        val isLate = lateDays > 0L
        val fineValue = LoanRepository.calculateFineValue(lateDays)
        val fineText = LoanRepository.formatCurrency(fineValue)
        val status = if (isLate) EmprestimoStatus.ATRASADO else EmprestimoStatus.ATIVO
        val dataLabel = if (isLate) "$lateDays dias de atraso" else "Devolução: ${LoanRepository.formatDate(endDate)}"
        val userName = tvHeroNome.text.toString()

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundResource(if (isLate) R.drawable.card_border_red else R.drawable.card_border_green)
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
            setOnClickListener {
                DetalhesEmprestimoModalHelper.show(
                    activity = this@AdminUserDetailsPage,
                    status = status,
                    bookTitle = title,
                    bookAuthor = author,
                    alunoName = userName,
                    dataLabel = dataLabel,
                    taxaAtual = fineText,
                    loanId = loanDoc.id,
                    imageUrl = imageUrl
                )
            }
        }

        val capa = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(50), dp(66)).apply { marginEnd = dp(12) }
            setBackgroundResource(if (isLate) R.drawable.book_cover_red else R.drawable.book_cover_green)
        }
        val icon = ImageView(this).apply {
            setImageResource(R.drawable.lent_books_icon)
            setColorFilter(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER)
        }
        capa.addView(icon)
        ImageUtils.loadBookCoverImage(capa, imageUrl)

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvTitle = TextView(this).apply {
            text = title
            setTextColor(Color.parseColor("#1E2D3D"))
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
        }
        val tvAuthor = TextView(this).apply {
            text = author
            setTextColor(Color.parseColor("#9BAAC0"))
            textSize = 11f
            maxLines = 1
        }
        val tvStatus = TextView(this).apply {
            text = if (isLate) "Atrasado $lateDays dias" else "Devolução: ${LoanRepository.formatDate(endDate)}"
            setTextColor(if (isLate) Color.parseColor("#B91C1C") else Color.parseColor("#1474C4"))
            textSize = 11f
        }

        info.addView(tvTitle)
        info.addView(tvAuthor)
        info.addView(tvStatus)

        card.addView(capa)
        card.addView(info)
        return card
    }

    private fun criarCardReserva(reservaDoc: DocumentSnapshot, bookDoc: DocumentSnapshot?): LinearLayout {
        val title = bookDoc?.getString("title") ?: "Livro reservado"
        val author = bookDoc?.getString("author") ?: "Autor não informado"
        val imageUrl = bookDoc?.getString("imageUrl") ?: ""
        val reserveTimestamp = reservaDoc.getTimestamp("reserve_timestamp")
        val userName = tvHeroNome.text.toString()

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundResource(R.drawable.card_border_blue)
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
            setOnClickListener {
                DetalhesEmprestimoModalHelper.show(
                    activity = this@AdminUserDetailsPage,
                    status = EmprestimoStatus.PENDENTE,
                    bookTitle = title,
                    bookAuthor = author,
                    alunoName = userName,
                    dataLabel = if (reserveTimestamp != null) {
                        "Reservado: ${LoanRepository.formatDate(reserveTimestamp)}"
                    } else {
                        "Reserva pendente"
                    },
                    reservationId = reservaDoc.id,
                    imageUrl = imageUrl
                )
            }
        }

        val capa = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(50), dp(66)).apply { marginEnd = dp(12) }
            setBackgroundResource(R.drawable.bg_book_blue)
        }
        val icon = ImageView(this).apply {
            setImageResource(R.drawable.lent_books_icon)
            setColorFilter(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER)
        }
        capa.addView(icon)
        ImageUtils.loadBookCoverImage(capa, imageUrl)

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvTitle = TextView(this).apply {
            text = title
            setTextColor(Color.parseColor("#1E2D3D"))
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
        }
        val tvAuthor = TextView(this).apply {
            text = author
            setTextColor(Color.parseColor("#9BAAC0"))
            textSize = 11f
            maxLines = 1
        }
        val tvData = TextView(this).apply {
            text = if (reserveTimestamp != null) {
                "Reservado: ${LoanRepository.formatDate(reserveTimestamp)}"
            } else {
                "Reserva pendente"
            }
            setTextColor(Color.parseColor("#1474C4"))
            textSize = 11f
        }

        info.addView(tvTitle)
        info.addView(tvAuthor)
        info.addView(tvData)

        card.addView(capa)
        card.addView(info)
        return card
    }

    private fun addSectionPlaceholder(container: LinearLayout, msg: String) {
        val tv = TextView(this).apply {
            text = msg
            setTextColor(Color.parseColor("#9BAAC0"))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(tv)
    }

    // Camera / Gallery / UCrop

    private fun showImageSourcePicker() {
        AlertDialog.Builder(this)
            .setTitle("Selecionar imagem")
            .setItems(arrayOf("Câmera", "Galeria")) { _, which ->
                if (which == 0) checkCameraPermissionAndLaunch() else launchGallery()
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchCamera() {
        val photoFile = ImageUtils.createTempCameraFile(this)
        cameraPhotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
        }
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun launchCrop(sourceUri: Uri) {
        val destFile = ImageUtils.createTempCameraFile(this)
        val destUri = Uri.fromFile(destFile)
        UCrop.of(sourceUri, destUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(400, 400)
            .start(this)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (resultCode == RESULT_OK) pendingCropUri = cameraPhotoUri
            }
            REQUEST_GALLERY -> {
                if (resultCode == RESULT_OK) pendingCropUri = data?.data
            }
            UCrop.REQUEST_CROP -> {
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        val resultUri = UCrop.getOutput(data) ?: return
                        val bitmap = contentResolver.openInputStream(resultUri)
                            ?.use { BitmapFactory.decodeStream(it) } ?: return
                        val base64 = ImageUtils.compressBitmapToBase64(bitmap)
                        bitmap.recycle()
                        savePhoto(base64)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Falha ao processar imagem", Toast.LENGTH_SHORT).show()
                    } catch (e: OutOfMemoryError) {
                        Toast.makeText(this, "Imagem muito grande", Toast.LENGTH_SHORT).show()
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    val err = UCrop.getError(data ?: return)
                    Toast.makeText(this, "Erro ao recortar: ${err?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun savePhoto(base64: String) {
        UserRepository.updateUserField(currentUserId, "photoUrl", base64,
            onSuccess = {
                currentPhotoBase64 = base64
                applyUserAvatar(ivHeroPhoto, tvHeroIniciais, tvHeroNome.text.toString(), base64)
                Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show()
            },
            onError = { e -> Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
