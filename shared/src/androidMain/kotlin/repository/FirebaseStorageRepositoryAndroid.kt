package repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageRepositoryAndroid{
    private val storage = FirebaseStorage.getInstance()

    suspend fun subirImagen(uri: Uri, idEquipo: Int): String? {
        return try {
            // Se crea un nombre único para la imagen
            val nombreArchivo = "${idEquipo}/entrenamientos/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(nombreArchivo)
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString() // devuelve la URL
        } catch (e: Exception) {
            println("Error subiendo imagen: ${e.message}")
            null
        }
    }
}