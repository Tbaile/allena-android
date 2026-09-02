package it.bailettitommaso.allena.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Owns the cache files the avatar flow needs: the destination the camera writes into, and the
 * local copy of a gallery picture (the picked [Uri] is only readable while the grant lasts).
 */
class ImageFileStore(private val context: Context) {

    fun newCaptureFile(): File = File(imagesDir(), "avatar_${System.currentTimeMillis()}.jpg")

    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    suspend fun copyToCache(uri: Uri): File? = withContext(Dispatchers.IO) {
        val target = File(imagesDir(), "picked_${System.currentTimeMillis()}.jpg")
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                if (input == null) return@withContext null
                target.outputStream().use(input::copyTo)
            }
            target
        } catch (e: Exception) {
            Timber.d(e, "imageFileStore: could not copy %s", uri)
            target.delete()
            null
        }
    }

    private fun imagesDir(): File = File(context.cacheDir, "images").apply { mkdirs() }
}
