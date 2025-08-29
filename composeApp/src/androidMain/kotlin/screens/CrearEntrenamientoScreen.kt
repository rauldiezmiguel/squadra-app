package screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.toKotlinLocalDate
import network.CrearEntrenamientoRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun CrearEntrenamientoScreen(
    onDismiss: () -> Unit,
    onCreate: (CrearEntrenamientoRequest) -> Unit,
    idEquipo: Int,
    fechaSeleccionada: LocalDate
) {
    var descripcion by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri2 by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var selectedImageSlot by remember { mutableStateOf(1) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (selectedImageSlot == 1) {
            imageUri = uri
        } else {
            imageUri2 = uri
        }
    }

    val uploadImageToFirebase: suspend (Uri) -> String? = { uri ->
        try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = "entrenamientos/${UUID.randomUUID()}"
            val imageRef = storageRef.child(fileName)
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Column(
        modifier = Modifier
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 🔹 HEADER FIJO
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material.Text(
                        text = "SQUADRA",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Text(
                "Nuevo Entrenamiento",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 🔹 CONTENIDO SCROLLABLE
        Column(
            modifier = Modifier
                .weight(1f) // ocupa el espacio disponible
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción", color = secondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Fecha seleccionada: ${fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = secondaryColor)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedImageSlot = 1
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                    ) {
                        Text("Seleccionar Imagen 1", color = Color.White)
                    }

                    imageUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Imagen 1 seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            selectedImageSlot = 2
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                    ) {
                        Text("Seleccionar Imagen 2", color = Color.White)
                    }

                    imageUri2?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Imagen 2 seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // espacio para que no tape el botón fijo
                }
            }
        }

        // 🔹 BOTÓN FIJO ABAJO
        Button(
            onClick = {
                if (imageUri != null || imageUri2 != null) {
                    isUploading = true
                } else {
                    val entrenamiento = CrearEntrenamientoRequest(
                        idEquipo = idEquipo,
                        fecha = fechaSeleccionada.toKotlinLocalDate(),
                        descripcion = descripcion,
                        entrenamientoUrl = ""
                    )
                    onCreate(entrenamiento)
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
        ) {
            Text(if (isUploading) "Subiendo..." else "Crear Entrenamiento")
        }
    }

    // Lanzamiento del efecto para subir imagen si es necesario
    if (isUploading && (imageUri != null || imageUri2 != null)) {
        LaunchedEffect(imageUri, imageUri2) {
            val url1 = imageUri?.let { uploadImageToFirebase(it) } ?: ""
            val url2 = imageUri2?.let { uploadImageToFirebase(it) } ?: ""
            val combinedUrls = listOf(url1, url2).filter { it.isNotBlank() }.joinToString(",")

            val entrenamiento = CrearEntrenamientoRequest(
                idEquipo = idEquipo,
                fecha = fechaSeleccionada.toKotlinLocalDate(),
                descripcion = descripcion,
                entrenamientoUrl = combinedUrls
            )

            onCreate(entrenamiento)
            onDismiss()
            isUploading = false
        }
    }
}
