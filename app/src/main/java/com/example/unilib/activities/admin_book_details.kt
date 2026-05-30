package com.example.unilib.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.palette.graphics.Palette
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.unilib.R
import com.example.unilib.repository.BookRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.yalantis.ucrop.UCrop

class admin_book_details : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var bookColor: String = "blue"
    private val bookRepository = BookRepository()
    private var currentBookId: String = ""
    private var currentImageBase64: String = ""
    private var cameraPhotoUri: Uri? = null
    private var pendingCropUri: Uri? = null

    override fun onResume() {
        super.onResume()
        pendingCropUri?.let { uri ->
            pendingCropUri = null
            launchCrop(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_book_details)

        val activeTab = intent.getStringExtra("ADMIN_NAV_TAB")
            ?.let { runCatching { AdminNavTab.valueOf(it) }.getOrNull() }
            ?: AdminNavTab.HOME
        AdminNavBarHelper.setup(this, activeTab)
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        bookColor = intent.getStringExtra("BOOK_COLOR") ?: "blue"

        loadBookFromFirestore()
    }

    private fun loadBookFromFirestore() {
        val bookId = intent.getStringExtra("BOOK_ID")
        val title = intent.getStringExtra("TITULO_LIVRO")

        when {
            !bookId.isNullOrBlank() -> {
                currentBookId = bookId
                db.collection("books").document(bookId).get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            showErrorAndClose("Livro não encontrado.")
                            return@addOnSuccessListener
                        }
                        onBookLoaded(document)
                    }
                    .addOnFailureListener { showErrorAndClose(it.message ?: "Erro ao carregar livro.") }
            }

            !title.isNullOrBlank() -> {
                db.collection("books").whereEqualTo("title", title).limit(1).get()
                    .addOnSuccessListener { result ->
                        val document = result.documents.firstOrNull()
                        if (document == null) {
                            showErrorAndClose("Livro não encontrado.")
                            return@addOnSuccessListener
                        }
                        onBookLoaded(document)
                    }
                    .addOnFailureListener { showErrorAndClose(it.message ?: "Erro ao carregar livro.") }
            }

            else -> showErrorAndClose("Nenhum livro foi informado.")
        }
    }

    private fun onBookLoaded(document: DocumentSnapshot) {
        val title = document.getString("title") ?: ""
        val author = document.getString("author") ?: ""
        val isbn = document.get("isbn")?.toString() ?: ""
        val synopsis = document.getString("synopsis") ?: ""
        val quantity = getLong(document, "quantity")
        val available = getLong(document, "available")
        val borrowed = (quantity - available).coerceAtLeast(0L)
        val reserved = getLong(document, "reserved")
        currentImageBase64 = document.getString("imageUrl") ?: ""

        renderBook(title, author, isbn, quantity, available, borrowed, reserved, synopsis)
        applyBookColor(bookColor)
        displayBookCoverImage(currentImageBase64)
        wireEditModals(title, author, synopsis, quantity, available)
    }

    private fun renderBook(
        title: String, author: String, isbn: String,
        total: Long, available: Long, borrowed: Long, reserved: Long, synopsis: String
    ) {
        findViewById<TextView>(R.id.tvBookTitle)?.text = title
        findViewById<TextView>(R.id.tvBookAuthor)?.text = author
        findViewById<TextView>(R.id.tvIsbn)?.text = "ISBN: $isbn"
        findViewById<TextView>(R.id.tvTotal)?.text = total.toString()
        findViewById<TextView>(R.id.tvDisponiveis)?.text = available.toString()
        findViewById<TextView>(R.id.tvEmprestados)?.text = borrowed.toString()
        findViewById<TextView>(R.id.tvReservados)?.text = reserved.toString()
        findViewById<TextView>(R.id.tvSinopseDisplay)?.text = synopsis
    }

    private fun wireEditModals(
        title: String, author: String, synopsis: String,
        total: Long, available: Long
    ) {
        findViewById<View>(R.id.btnEditarNome)?.setOnClickListener {
            EditarNomeModalHelper.show(this, title) { novoNome ->
                bookRepository.updateBookField(
                    bookId = currentBookId,
                    fieldName = "title",
                    newValue = novoNome,
                    onSuccess = {
                        Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                        loadBookFromFirestore()
                    },
                    onError = { e ->
                        Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        findViewById<View>(R.id.btnEditarAutor)?.setOnClickListener {
            EditarAutorModalHelper.show(this, author) { novoAutor ->
                bookRepository.updateBookField(
                    bookId = currentBookId,
                    fieldName = "author",
                    newValue = novoAutor,
                    onSuccess = {
                        Toast.makeText(this, "Autor atualizado!", Toast.LENGTH_SHORT).show()
                        loadBookFromFirestore()
                    },
                    onError = { e ->
                        Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        findViewById<View>(R.id.btnEditarImagem)?.setOnClickListener {
            EditarImagemModalHelper.show(
                activity = this,
                initialBase64 = currentImageBase64,
                onPickImage = { showImageSourcePicker() },
                onConfirm = { base64 ->
                    bookRepository.updateBookField(
                        bookId = currentBookId,
                        fieldName = "imageUrl",
                        newValue = base64,
                        onSuccess = {
                            currentImageBase64 = base64
                            displayBookCoverImage(base64)
                            Toast.makeText(this, "Imagem atualizada!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { e ->
                            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
        findViewById<View>(R.id.btnEditarQuantidade)?.setOnClickListener {
            EditarQuantidadeModalHelper.show(
                this,
                currentQuantity = total.toString(),
                availableLabel = "$available Disponíveis"
            ) { novaQuantidade ->
                bookRepository.updateBookField(
                    bookId = currentBookId,
                    fieldName = "quantity",
                    newValue = novaQuantidade,
                    onSuccess = {
                        Toast.makeText(this, "Quantidade atualizada!", Toast.LENGTH_SHORT).show()
                        loadBookFromFirestore()
                    },
                    onError = { e ->
                        Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        findViewById<View>(R.id.btnEditarSinopse)?.setOnClickListener {
            EditarSinopseModalHelper.show(this, synopsis) { novaSinopse ->
                bookRepository.updateBookField(
                    bookId = currentBookId,
                    fieldName = "synopsis",
                    newValue = novaSinopse,
                    onSuccess = {
                        Toast.makeText(this, "Sinopse atualizada!", Toast.LENGTH_SHORT).show()
                        loadBookFromFirestore()
                    },
                    onError = { e ->
                        Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        findViewById<View>(R.id.btnEditarTags)?.setOnClickListener {
            EditarTagsModalHelper.show(this)
        }
        findViewById<View>(R.id.btnExcluirLivro)?.setOnClickListener {
            ConfirmarExclusaoModalHelper.show(
                this,
                title,
                onConfirm = {
                    bookRepository.deleteBook(
                        bookId = currentBookId,
                        onSuccess = {
                            Toast.makeText(this, "Livro excluído com sucesso!", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onError = { exception ->
                            Toast.makeText(this, "Erro ao excluir: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }

    private fun showImageSourcePicker() {
        AlertDialog.Builder(this)
            .setTitle("Selecionar imagem")
            .setItems(arrayOf("Câmera", "Galeria")) { _, which ->
                if (which == 0) checkCameraPermissionAndLaunch() else launchGallery()
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
            putExtra("android.intent.extras.CAMERA_FACING", 0) // 0 = back camera
            putExtra("android.intent.extras.LENS_FACING_FRONT", 0)
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
            .withAspectRatio(3f, 4f)
            .withMaxResultSize(600, 800)
            .start(this)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (resultCode == RESULT_OK) {
                    pendingCropUri = cameraPhotoUri
                }
            }
            REQUEST_GALLERY -> {
                if (resultCode == RESULT_OK) {
                    pendingCropUri = data?.data
                }
            }
            UCrop.REQUEST_CROP -> {
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        val resultUri = UCrop.getOutput(data) ?: return
                        val bitmap = contentResolver.openInputStream(resultUri)
                            ?.use { BitmapFactory.decodeStream(it) } ?: return
                        val base64 = ImageUtils.compressBitmapToBase64(bitmap)
                        bitmap.recycle()
                        EditarImagemModalHelper.setSelectedImage(base64)
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

    private fun displayBookCoverImage(base64: String) {
        if (base64.isEmpty()) return
        val bitmap = ImageUtils.base64ToBitmap(base64) ?: return
        findViewById<TextView>(R.id.tvBookEmoji)?.visibility = View.GONE
        findViewById<ImageView>(R.id.ivBookCoverImage)?.apply {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
        }
        Palette.from(bitmap).generate { palette ->
            val dominant = palette?.getDominantColor(Color.parseColor("#1565C0"))
                ?: Color.parseColor("#1565C0")
            val dark = ImageUtils.forceDark(dominant)
            val darker = ImageUtils.darkenColor(dark, 0.80f)
            findViewById<View>(R.id.bookCover)?.setBackgroundColor(dark)
            findViewById<View>(R.id.heroSection)?.setBackgroundColor(darker)
        }
    }

    private fun applyBookColor(color: String) {
        val theme = colorThemes[color] ?: colorThemes.getValue("blue")
        findViewById<View>(R.id.bookCover)?.setBackgroundResource(theme.coverDrawable)
        findViewById<View>(R.id.heroSection)?.setBackgroundColor(Color.parseColor(theme.heroColor))
    }

    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun getLong(document: DocumentSnapshot, field: String): Long {
        val value = document.get(field) ?: return 0L
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Float -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private data class BookColorTheme(
        val coverDrawable: Int,
        val darkColor: String,
        val heroColor: String
    )

    private companion object {
        const val REQUEST_CAMERA = 1001
        const val REQUEST_GALLERY = 1002
        const val REQUEST_CAMERA_PERMISSION = 1003

        val colorThemes = mapOf(
            "blue" to BookColorTheme(R.drawable.bg_book_blue, "#0D47A1", "#1565C0"),
            "green" to BookColorTheme(R.drawable.bg_book_green, "#004D40", "#00695C"),
            "purple" to BookColorTheme(R.drawable.bg_book_purple, "#4A148C", "#6A1B9A"),
            "red" to BookColorTheme(R.drawable.bg_book_red, "#B71C1C", "#C62828"),
            "gray" to BookColorTheme(R.drawable.bg_book_gray, "#37474F", "#546E7A")
        )
    }
}
