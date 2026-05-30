package com.example.unilib.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.unilib.R
import com.example.unilib.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop

class UserEditPage : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    private var currentUserId: String = ""
    private var currentPhotoBase64: String = ""
    private var currentNome: String = ""
    private var currentCpf: String = ""
    private var cameraPhotoUri: Uri? = null
    private var pendingCropUri: Uri? = null

    private lateinit var tvIniciais: TextView
    private lateinit var ivPhoto: ImageView
    private lateinit var tvNomeHero: TextView
    private lateinit var tvEmailHero: TextView

    private companion object {
        const val REQUEST_CAMERA = 1001
        const val REQUEST_GALLERY = 1002
        const val REQUEST_CAMERA_PERMISSION = 2001
    }

    override fun onResume() {
        super.onResume()
        val cropUri = pendingCropUri
        if (cropUri != null) {
            pendingCropUri = null
            launchCrop(cropUri)
            // Skip loadUserData when launching crop — the photo isn't saved yet
            return
        }
        if (currentUserId.isNotEmpty()) loadUserData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit_page)

        currentUserId = auth.currentUser?.uid ?: run { finish(); return }

        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        tvIniciais = findViewById(R.id.tvIniciais)
        ivPhoto = findViewById(R.id.ivPhoto)
        tvNomeHero = findViewById(R.id.tvNomeHero)
        tvEmailHero = findViewById(R.id.tvEmailHero)

        findViewById<FrameLayout>(R.id.avatarContainer)?.setOnClickListener {
            showImageSourcePicker()
        }

        loadUserData()
        wireEditModals()
    }

    private fun loadUserData() {
        UserRepository.getUserById(currentUserId,
            onSuccess = { document ->
                if (document == null || !document.exists()) return@getUserById
                val nome = document.getString("nome") ?: ""
                val photoUrl = document.getString("photoUrl") ?: ""

                currentNome = nome
                currentCpf = document.getString("cpf") ?: ""
                currentPhotoBase64 = photoUrl

                tvNomeHero.text = nome.ifEmpty { "Usuário" }
                tvEmailHero.text = document.getString("email") ?: ""

                applyUserAvatar(ivPhoto, tvIniciais, nome, photoUrl)
            },
            onError = { e ->
                Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun wireEditModals() {
        findViewById<View>(R.id.btnEditarNomePessoal)?.setOnClickListener {
            EditarNomeUsuarioModalHelper.show(this, currentNome) { novoNome ->
                UserRepository.updateUserField(currentUserId, "nome", novoNome,
                    onSuccess = {
                        Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                        loadUserData()
                    },
                    onError = { e -> Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
                )
            }
        }

        findViewById<View>(R.id.btnEditarCpfPessoal)?.setOnClickListener {
            EditarCpfUsuarioModalHelper.show(this, currentCpf) { novoCpf ->
                UserRepository.updateUserField(currentUserId, "cpf", novoCpf,
                    onSuccess = {
                        Toast.makeText(this, "CPF atualizado!", Toast.LENGTH_SHORT).show()
                        loadUserData()
                    },
                    onError = { e -> Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
                )
            }
        }

        findViewById<View>(R.id.btnExcluirConta)?.setOnClickListener {
            ConfirmarExclusaoUsuarioModalHelper.show(this, currentNome) {
                deleteOwnAccount()
            }
        }
    }

    private fun deleteOwnAccount() {
        UserRepository.deleteUserWithCascade(currentUserId,
            onSuccess = {
                // Firestore data gone — now delete the Firebase Auth record
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        val intent = Intent(this, StartPage::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                    ?.addOnFailureListener { e ->
                        // Auth deletion failed (e.g. needs re-login); Firestore data is already removed.
                        // Sign out so the user can't access the app with a broken account.
                        auth.signOut()
                        val intent = Intent(this, StartPage::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
            },
            onError = { e ->
                Toast.makeText(this, "Erro ao excluir conta: ${e.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun savePhoto(base64: String) {
        UserRepository.updateUserField(currentUserId, "photoUrl", base64,
            onSuccess = {
                currentPhotoBase64 = base64
                applyUserAvatar(ivPhoto, tvIniciais, currentNome, base64)
                Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show()
            },
            onError = { e -> Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
        )
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
}
