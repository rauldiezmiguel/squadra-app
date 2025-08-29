package firebaseStorage

import com.google.firebase.storage.FirebaseStorage

fun eliminarImagenDeFirebase(url: String, onResult: (Boolean) -> Unit) {
    val storage = FirebaseStorage.getInstance()
    val ref = storage.getReferenceFromUrl(url)

    ref.delete().addOnSuccessListener {
        println("Imagen eliminada de Firebase correctamente.")
        onResult(true)
    }.addOnFailureListener { exception ->
        println("Error al eliminar la imagen de Firebase: ${exception.message}")
        onResult(false)
    }
}