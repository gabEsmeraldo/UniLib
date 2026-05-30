package com.example.unilib.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.unilib.R
import com.example.unilib.models.Book
import com.example.unilib.repository.BookRepository
import com.yalantis.ucrop.UCrop

class AdminAddBook : AppCompatActivity() {

    private lateinit var repository: BookRepository
    private var selectedImageBase64: String = ""
    private var cameraPhotoUri: Uri? = null
    private var pendingCropUri: Uri? = null

    companion object {
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002
        private const val REQUEST_CAMERA_PERMISSION = 1003
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
        setContentView(R.layout.admin_add_book)
        repository = BookRepository()

        val editBookTitle = findViewById<EditText>(R.id.editBookTitle)
        val editAuthor = findViewById<EditText>(R.id.editAuthor)
        val editIsbn = findViewById<EditText>(R.id.editIsbn)
        editIsbn.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val digits = s.toString().filter { it.isDigit() }.take(13)
                val formatted = buildString {
                    for (i in digits.indices) {
                        if (i == 3 || i == 4 || i == 6 || i == 12) append('-')
                        append(digits[i])
                    }
                }
                if (formatted != s.toString()) {
                    editIsbn.setText(formatted)
                    editIsbn.setSelection(formatted.length)
                }
                isFormatting = false
            }
        })
        val editTags = findViewById<EditText>(R.id.editTags)
        val editSynopsis = findViewById<EditText>(R.id.editSynopsis)
        val editQuantity = findViewById<EditText>(R.id.editQuantity)
        val btnEnter = findViewById<Button>(R.id.btnEnter)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val layoutAddImage = findViewById<FrameLayout>(R.id.layoutAddImage)

        layoutAddImage.setOnClickListener { showImageSourcePicker() }

        btnBack.setOnClickListener { navigateBack() }
        btnCancel.setOnClickListener { navigateBack() }

        btnEnter.setOnClickListener {
            val title = editBookTitle.text.toString().trim()
            val author = editAuthor.text.toString().trim()
            val isbn = editIsbn.text.toString().filter { it.isDigit() }
            val tagsText = editTags.text.toString().trim()
            val synopsis = editSynopsis.text.toString().trim()
            val quantityText = editQuantity.text.toString().trim()
            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || quantityText.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val quantity = quantityText.toLongOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(this, "Quantidade de exemplares inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val tags = if (tagsText.isNotEmpty()) {
                tagsText.split(",", " ").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
            val book = Book(
                title = title,
                author = author,
                isbn = isbn,
                tags = tags,
                synopsis = synopsis,
                quantity = quantity,
                imageUrl = selectedImageBase64
            )
            btnEnter.isEnabled = false
            btnEnter.text = "Salvando..."
            repository.addBook(
                book = book,
                onSuccess = {
                    Toast.makeText(this, "Livro cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    navigateBack()
                },
                onError = { exception ->
                    Toast.makeText(this, "Erro ao salvar: ${exception.message}", Toast.LENGTH_LONG).show()
                    btnEnter.isEnabled = true
                    btnEnter.text = "Confirmar e Adicionar"
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
                        selectedImageBase64 = ImageUtils.compressBitmapToBase64(bitmap)
                        bitmap.recycle()
                        showAddFormPreview(selectedImageBase64)
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

    private fun showAddFormPreview(base64: String) {
        val bitmap = ImageUtils.base64ToBitmap(base64) ?: return
        findViewById<View>(R.id.addImageHint)?.visibility = View.GONE
        findViewById<ImageView>(R.id.ivAddBookPreview)?.apply {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
        }
        findViewById<View>(R.id.tvChangeImageHint)?.visibility = View.VISIBLE
    }

    private fun navigateBack() {
        val intent = Intent(this, AdminHomePage::class.java)
        startActivity(intent)
        finish()
    }
}
