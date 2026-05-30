package com.example.unilib.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object ImageUtils {

    fun createTempCameraFile(context: Context): File {
        val cameraDir = File(context.cacheDir, "camera").also { it.mkdirs() }
        return File(cameraDir, "${UUID.randomUUID()}.jpg")
    }

    fun compressBitmapToBase64(bitmap: Bitmap, maxWidth: Int = 600, quality: Int = 70): String {
        val scaled = scaleBitmap(bitmap, maxWidth)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    // Overlays a book-cover image on top of an existing capa FrameLayout (over the icon/emoji).
    // Returns true if image was applied, false if imageUrl was empty or decode failed.
    fun loadBookCoverImage(container: android.widget.FrameLayout, imageUrl: String): Boolean {
        if (imageUrl.isEmpty()) return false
        val bitmap = base64ToBitmap(imageUrl) ?: return false
        val imageView = android.widget.ImageView(container.context).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            setImageBitmap(bitmap)
        }
        container.addView(imageView)
        return true
    }

    fun darkenColor(color: Int, factor: Float = 0.72f): Int {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
        return android.graphics.Color.HSVToColor(hsv)
    }

    // Ensures the color is dark enough for white text to be readable (max brightness 0.45).
    fun forceDark(color: Int, maxBrightness: Float = 0.45f): Int {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        if (hsv[2] > maxBrightness) hsv[2] = maxBrightness
        return android.graphics.Color.HSVToColor(hsv)
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int): Bitmap {
        if (bitmap.width <= maxWidth) return bitmap
        val ratio = maxWidth.toFloat() / bitmap.width
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
    }
}
